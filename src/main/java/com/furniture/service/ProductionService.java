package com.furniture.service;

import com.furniture.entity.*;
import com.furniture.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductionService {

    private final ProductionOrderRepository orderRepository;
    private final ConsumptionRateRepository consumptionRateRepository;
    private final MaterialConsumptionRepository materialConsumptionRepository;
    private final FinishedProductService productService;
    private final MaterialService materialService;

    public List<ProductionOrder> findAllOrders() {
        return orderRepository.findAll();
    }

    public ProductionOrder findOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
    }

    // Добавленный метод для дашборда
    public long countActiveOrders() {
        return orderRepository.findByStatus("in_progress").size();
    }

    @Transactional
    public ProductionOrder createOrder(ProductionOrder order) {
        String orderNumber = "ПЗ-" + String.format("%04d", System.currentTimeMillis() % 10000);
        order.setOrderNumber(orderNumber);
        order.setStatus("planned");
        order.setPlannedDate(LocalDate.now());
        ProductionOrder savedOrder = orderRepository.save(order);

        List<ConsumptionRate> rates = consumptionRateRepository.findByProduct(order.getProduct());
        for (ConsumptionRate rate : rates) {
            MaterialConsumption consumption = new MaterialConsumption();
            consumption.setOrder(savedOrder);
            consumption.setMaterial(rate.getMaterial());
            consumption.setPlannedQuantity(rate.getQuantity().multiply(order.getPlannedQuantity()));
            consumption.setWrittenOff(false);
            materialConsumptionRepository.save(consumption);
        }

        return savedOrder;
    }

    @Transactional
    public void startProduction(Long orderId) {
        ProductionOrder order = findOrderById(orderId);
        if (!"planned".equals(order.getStatus())) {
            throw new RuntimeException("Заказ нельзя запустить. Текущий статус: " + order.getStatus());
        }
        order.setStatus("in_progress");
        orderRepository.save(order);
    }

    @Transactional
    public void writeOffMaterials(Long orderId) {
        ProductionOrder order = findOrderById(orderId);
        if (!"in_progress".equals(order.getStatus())) {
            throw new RuntimeException("Материалы можно списать только для заказа в работе");
        }

        List<MaterialConsumption> consumptions = materialConsumptionRepository.findByOrder(order);
        for (MaterialConsumption consumption : consumptions) {
            if (!consumption.getWrittenOff()) {
                materialService.updateBalance(consumption.getMaterial().getId(),
                        consumption.getPlannedQuantity().negate());

                consumption.setActualQuantity(consumption.getPlannedQuantity());
                consumption.setWrittenOff(true);
                consumption.setWrittenOffAt(LocalDate.now().atStartOfDay());
                materialConsumptionRepository.save(consumption);
            }
        }
    }

    @Transactional
    public void completeOrder(Long orderId) {
        ProductionOrder order = findOrderById(orderId);
        if (!"in_progress".equals(order.getStatus())) {
            throw new RuntimeException("Заказ можно завершить только если он в работе");
        }

        List<MaterialConsumption> consumptions = materialConsumptionRepository.findByOrder(order);
        boolean allWrittenOff = consumptions.stream().allMatch(MaterialConsumption::getWrittenOff);
        if (!allWrittenOff) {
            throw new RuntimeException("Не все материалы списаны. Сначала выполните списание.");
        }

        order.setActualQuantity(order.getPlannedQuantity());
        order.setStatus("completed");
        order.setCompletedDate(LocalDate.now());
        orderRepository.save(order);

        productService.updateBalance(order.getProduct().getId(), order.getPlannedQuantity());
        calculateCostPrice(orderId);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        ProductionOrder order = findOrderById(orderId);
        if ("completed".equals(order.getStatus())) {
            throw new RuntimeException("Нельзя отменить завершённый заказ");
        }
        order.setStatus("cancelled");
        orderRepository.save(order);
    }

    private void calculateCostPrice(Long orderId) {
        ProductionOrder order = findOrderById(orderId);
        List<MaterialConsumption> consumptions = materialConsumptionRepository.findByOrder(order);

        BigDecimal totalMaterialCost = consumptions.stream()
                .map(mc -> mc.getActualQuantity().multiply(mc.getMaterial().getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal costPerUnit = totalMaterialCost.divide(order.getActualQuantity(), 2, BigDecimal.ROUND_HALF_UP);

        productService.updateCostPrice(order.getProduct().getId(), costPerUnit);
    }
}
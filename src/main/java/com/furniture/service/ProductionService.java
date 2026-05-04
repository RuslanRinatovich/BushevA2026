package com.furniture.service;

import com.furniture.dto.ProductionOrderDto;
import com.furniture.entity.*;
import com.furniture.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductionService {

    private final ProductionOrderRepository orderRepository;
    private final ConsumptionRateRepository consumptionRateRepository;
    private final MaterialConsumptionRepository materialConsumptionRepository;
    private final FinishedProductService productService;
    private final MaterialService materialService;
    private final UserService userService;

    // Добавьте в ProductionService эти методы

    public List<ProductionOrder> findAllOrders() {
        return orderRepository.findAll();
    }

    public List<ProductionOrder> filterOrders(String search, String status) {
        List<ProductionOrder> allOrders = orderRepository.findAll();

        return allOrders.stream()
                .filter(order -> {
                    // Фильтр по статусу
                    if (status != null && !status.isBlank()) {
                        if (!order.getStatus().equals(status)) {
                            return false;
                        }
                    }
                    // Фильтр по поиску
                    if (search != null && !search.isBlank()) {
                        String searchLower = search.toLowerCase();
                        boolean matchesNumber = order.getOrderNumber().toLowerCase().contains(searchLower);
                        boolean matchesProduct = order.getProduct().getName().toLowerCase().contains(searchLower);
                        if (!matchesNumber && !matchesProduct) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }
    // Добавьте эти методы в ProductionService

    public List<ProductionOrder> searchOrders(String search) {
        if (search == null || search.isBlank()) {
            return orderRepository.findAll();
        }
        return orderRepository.searchOrders(search);
    }


    public ProductionOrder findOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
    }

    public List<ProductionOrder> getActiveOrders() {
        return orderRepository.findActiveOrders();
    }

    public long countActiveOrders() {
        return getActiveOrders().size();
    }

    @Transactional
    public ProductionOrder createOrder(ProductionOrderDto dto, String username) {
        User user = userService.findByUsername(username);
        FinishedProduct product = productService.findById(dto.getProductId());

        // Генерация номера заказа
        Long lastId = 0L;
        Optional<ProductionOrder> lastOrder = orderRepository.findTopByOrderByIdDesc();
        if (lastOrder.isPresent()) {
            lastId = lastOrder.get().getId();
        }
        String orderNumber = "ПЗ-" + String.format("%04d", lastId + 1);

        ProductionOrder order = new ProductionOrder();
        order.setOrderNumber(orderNumber);
        order.setProduct(product);
        order.setPlannedQuantity(dto.getPlannedQuantity());
        order.setStatus("planned");
        order.setPlannedDate(dto.getPlannedDate() != null ? dto.getPlannedDate() : LocalDate.now());
        order.setCreatedBy(user);

        ProductionOrder savedOrder = orderRepository.save(order);

        // Создание планового расхода материалов на основе норм
        List<ConsumptionRate> rates = consumptionRateRepository.findByProduct(product);
        for (ConsumptionRate rate : rates) {
            MaterialConsumption consumption = new MaterialConsumption();
            consumption.setOrder(savedOrder);
            consumption.setMaterial(rate.getMaterial());
            consumption.setPlannedQuantity(rate.getQuantity().multiply(dto.getPlannedQuantity()));
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
        StringBuilder errors = new StringBuilder();

        for (MaterialConsumption consumption : consumptions) {
            if (!consumption.getWrittenOff()) {
                try {
                    // Списание материалов со склада
                    materialService.updateBalance(consumption.getMaterial().getId(),
                            consumption.getPlannedQuantity().negate());

                    consumption.setActualQuantity(consumption.getPlannedQuantity());
                    consumption.setWrittenOff(true);
                    consumption.setWrittenOffAt(LocalDate.now().atStartOfDay());
                    materialConsumptionRepository.save(consumption);
                } catch (RuntimeException e) {
                    errors.append(e.getMessage()).append("; ");
                }
            }
        }

        if (errors.length() > 0) {
            throw new RuntimeException("Ошибка при списании материалов: " + errors.toString());
        }
    }

    @Transactional
    public void completeOrder(Long orderId) {
        ProductionOrder order = findOrderById(orderId);
        if (!"in_progress".equals(order.getStatus())) {
            throw new RuntimeException("Заказ можно завершить только если он в работе");
        }

        // Проверяем, что все материалы списаны
        List<MaterialConsumption> consumptions = materialConsumptionRepository.findByOrder(order);
        boolean allWrittenOff = consumptions.stream().allMatch(MaterialConsumption::getWrittenOff);
        if (!allWrittenOff) {
            throw new RuntimeException("Не все материалы списаны. Сначала выполните списание.");
        }

        // Выпуск готовой продукции
        order.setActualQuantity(order.getPlannedQuantity());
        order.setStatus("completed");
        order.setCompletedDate(LocalDate.now());
        orderRepository.save(order);

        // Увеличиваем остаток готовой продукции
        productService.updateBalance(order.getProduct().getId(), order.getPlannedQuantity());

        // Рассчитываем себестоимость
        calculateCostPrice(orderId);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        ProductionOrder order = findOrderById(orderId);
        if ("completed".equals(order.getStatus())) {
            throw new RuntimeException("Нельзя отменить завершённый заказ");
        }
        if ("in_progress".equals(order.getStatus())) {
            throw new RuntimeException("Сначала выполните сторно списания материалов");
        }
        order.setStatus("cancelled");
        orderRepository.save(order);
    }

    @Transactional
    public void calculateCostPrice(Long orderId) {
        ProductionOrder order = findOrderById(orderId);
        List<MaterialConsumption> consumptions = materialConsumptionRepository.findByOrder(order);

        BigDecimal totalMaterialCost = consumptions.stream()
                .filter(MaterialConsumption::getWrittenOff)
                .map(mc -> mc.getActualQuantity().multiply(mc.getMaterial().getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (order.getActualQuantity() != null && order.getActualQuantity().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal costPerUnit = totalMaterialCost.divide(order.getActualQuantity(), 2, RoundingMode.HALF_UP);
            productService.updateCostPrice(order.getProduct().getId(), costPerUnit);
        }
    }

    public String getStatusLabel(String status) {
        return switch (status) {
            case "planned" -> "Планируется";
            case "in_progress" -> "В работе";
            case "completed" -> "Завершён";
            case "cancelled" -> "Отменён";
            default -> status;
        };
    }
}
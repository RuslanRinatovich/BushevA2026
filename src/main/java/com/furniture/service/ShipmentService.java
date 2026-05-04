package com.furniture.service;

import com.furniture.entity.Client;
import com.furniture.entity.FinishedProduct;
import com.furniture.entity.Shipment;
import com.furniture.entity.User;
import com.furniture.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final FinishedProductService productService;

    public List<Shipment> findAll() {
        return shipmentRepository.findAll();
    }

    public Shipment findById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Отгрузка не найдена"));
    }

    public List<Shipment> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return shipmentRepository.findByShipmentDateBetween(startDate, endDate);
    }

    public List<Shipment> findByClient(Client client) {
        return shipmentRepository.findByClient(client);
    }

    public List<Shipment> findByProduct(FinishedProduct product) {
        return shipmentRepository.findByProduct(product);
    }

    @Transactional
    public Shipment createShipment(Shipment shipment) {
        // Генерация номера отгрузки
        String shipmentNumber = "ОГ-" + String.format("%04d", System.currentTimeMillis() % 10000);
        shipment.setShipmentNumber(shipmentNumber);

        // Рассчитываем итоговую сумму
        BigDecimal total = shipment.getQuantity().multiply(shipment.getPrice());
        shipment.setTotalAmount(total);

        // Сохраняем отгрузку
        Shipment savedShipment = shipmentRepository.save(shipment);

        // Уменьшаем остаток готовой продукции
        productService.updateBalance(shipment.getProduct().getId(), shipment.getQuantity().negate());

        return savedShipment;
    }

    @Transactional
    public void deleteShipment(Long id) {
        Shipment shipment = findById(id);
        // Возвращаем продукцию на склад
        productService.updateBalance(shipment.getProduct().getId(), shipment.getQuantity());
        shipmentRepository.deleteById(id);
    }

    public BigDecimal getTotalRevenueForPeriod(LocalDate startDate, LocalDate endDate) {
        return shipmentRepository.getTotalRevenueForPeriod(startDate, endDate);
    }

    public BigDecimal getMonthlyRevenue() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();
        BigDecimal revenue = getTotalRevenueForPeriod(startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public long getShipmentsCountForPeriod(LocalDate startDate, LocalDate endDate) {
        return shipmentRepository.findByShipmentDateBetween(startDate, endDate).size();
    }
}
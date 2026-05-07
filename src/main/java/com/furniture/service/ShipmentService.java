package com.furniture.service;

import com.furniture.dto.ShipmentDto;
import com.furniture.entity.Client;
import com.furniture.entity.FinishedProduct;
import com.furniture.entity.Shipment;
import com.furniture.entity.User;
import com.furniture.repository.ClientRepository;
import com.furniture.repository.FinishedProductRepository;
import com.furniture.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ClientRepository clientRepository;
    private final FinishedProductRepository productRepository;
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

    @Transactional
    public Shipment createShipment(ShipmentDto dto, User user) {
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Клиент не найден"));
        FinishedProduct product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Продукт не найден"));

        // Проверка наличия достаточного количества
        if (product.getCurrentBalance().compareTo(dto.getQuantity()) < 0) {
            throw new RuntimeException("Недостаточно продукции на складе. Доступно: " +
                    product.getCurrentBalance() + " " + product.getUnit());
        }

        // Генерация номера отгрузки
        String shipmentNumber = generateShipmentNumber();

        BigDecimal totalAmount = dto.getQuantity().multiply(dto.getPrice());

        Shipment shipment = new Shipment();
        shipment.setShipmentNumber(shipmentNumber);
        shipment.setClient(client);
        shipment.setProduct(product);
        shipment.setQuantity(dto.getQuantity());
        shipment.setPrice(dto.getPrice());
        shipment.setTotalAmount(totalAmount);
        shipment.setShipmentDate(dto.getShipmentDate() != null ? dto.getShipmentDate() : LocalDate.now());
        shipment.setCreatedBy(user);

        Shipment savedShipment = shipmentRepository.save(shipment);

        // Уменьшаем остаток продукции
        productService.updateBalance(product.getId(), dto.getQuantity().negate());

        return savedShipment;
    }

    private String generateShipmentNumber() {
        Optional<Shipment> lastShipment = shipmentRepository.findTopByOrderByIdDesc();
        Long lastId = lastShipment.map(Shipment::getId).orElse(0L);
        return "ОГ-" + String.format("%04d", lastId + 1);
    }

    @Transactional
    public void deleteShipment(Long id) {
        Shipment shipment = findById(id);
        // Возвращаем продукцию на склад
        productService.updateBalance(shipment.getProduct().getId(), shipment.getQuantity());
        shipmentRepository.deleteById(id);
    }

    public BigDecimal getTotalRevenueForPeriod(LocalDate startDate, LocalDate endDate) {
        BigDecimal revenue = shipmentRepository.getTotalRevenueForPeriod(startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public BigDecimal getMonthlyRevenue() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();
        return getTotalRevenueForPeriod(startDate, endDate);
    }

    public List<Shipment> searchShipments(String search) {
        if (search == null || search.isBlank()) {
            return findAll();
        }
        return findAll().stream()
                .filter(s -> s.getShipmentNumber().toLowerCase().contains(search.toLowerCase()) ||
                        s.getClient().getName().toLowerCase().contains(search.toLowerCase()) ||
                        s.getProduct().getName().toLowerCase().contains(search.toLowerCase()))
                .toList();
    }
}
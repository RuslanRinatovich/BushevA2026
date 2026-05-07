package com.furniture.service;

import com.furniture.dto.ReportDto;
import com.furniture.entity.*;
import com.furniture.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final MaterialRepository materialRepository;
    private final FinishedProductRepository productRepository;
    private final ProductionOrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;

    // 1. Остатки материалов
    public List<Material> getMaterialBalances() {
        return materialRepository.findAll();
    }

    public BigDecimal getTotalMaterialValue() {
        return materialRepository.findAll().stream()
                .map(m -> m.getCurrentBalance().multiply(m.getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getLowStockMaterialsCount() {
        return materialRepository.findLowStockMaterials().size();
    }

    // 2. Остатки готовой продукции
    public List<FinishedProduct> getProductBalances() {
        return productRepository.findAll();
    }

    public BigDecimal getTotalProductValue() {
        return productRepository.findAll().stream()
                .map(p -> p.getCurrentBalance().multiply(p.getSellingPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 3. Статистика по производственным заказам
    public Map<String, Long> getOrderStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("planned", orderRepository.countByStatus("planned"));
        stats.put("in_progress", orderRepository.countByStatus("in_progress"));
        stats.put("completed", orderRepository.countByStatus("completed"));
        stats.put("cancelled", orderRepository.countByStatus("cancelled"));
        return stats;
    }

    // 4. Выручка по месяцам (последние 12 месяцев)
    public List<ReportDto> getMonthlyRevenue() {
        List<ReportDto> revenues = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = 11; i >= 0; i--) {
            LocalDate startDate = now.minusMonths(i).withDayOfMonth(1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

            BigDecimal revenue = shipmentRepository.getTotalRevenueForPeriod(startDate, endDate);
            String monthName = startDate.format(DateTimeFormatter.ofPattern("MMM yyyy"));

            ReportDto dto = new ReportDto();
            dto.setName(monthName);
            dto.setValue(revenue);
            revenues.add(dto);
        }
        return revenues;
    }

    // 5. Выручка за текущий месяц
    public BigDecimal getCurrentMonthRevenue() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();
        return shipmentRepository.getTotalRevenueForPeriod(startDate, endDate);
    }

    // 6. Сумма отгрузок за сегодня
    public BigDecimal getTodayRevenue() {
        LocalDate today = LocalDate.now();
        return shipmentRepository.getTotalRevenueForPeriod(today, today);
    }

    // 7. Себестоимость продукции (средняя)
    public BigDecimal getAverageCostPrice() {
        List<FinishedProduct> productsWithCost = productRepository.findAll().stream()
                .filter(p -> p.getCostPrice() != null && p.getCostPrice().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        if (productsWithCost.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalCost = productsWithCost.stream()
                .map(FinishedProduct::getCostPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalCost.divide(BigDecimal.valueOf(productsWithCost.size()), 2, RoundingMode.HALF_UP);
    }

    // 8. Себестоимость по конкретному продукту
    public BigDecimal getProductCostPrice(Long productId) {
        FinishedProduct product = productRepository.findById(productId).orElse(null);
        return product != null ? product.getCostPrice() : BigDecimal.ZERO;
    }

    // Добавьте в ReportService

    public List<String> getLast6Months() {
        List<String> months = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            months.add(now.minusMonths(i).format(DateTimeFormatter.ofPattern("MMM yyyy")));
        }
        return months;
    }

    public List<BigDecimal> getLast6MonthsRevenue() {
        List<BigDecimal> revenues = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate startDate = now.minusMonths(i).withDayOfMonth(1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            BigDecimal revenue = shipmentRepository.getTotalRevenueForPeriod(startDate, endDate);
            revenues.add(revenue != null ? revenue : BigDecimal.ZERO);
        }
        return revenues;
    }

    public Map<String, Long> getProductionStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("Планируется", (long) orderRepository.findByStatus("planned").size());
        stats.put("В работе", (long) orderRepository.findByStatus("in_progress").size());
        stats.put("Завершены", (long) orderRepository.findByStatus("completed").size());
        stats.put("Отменены", (long) orderRepository.findByStatus("cancelled").size());
        return stats;
    }
}
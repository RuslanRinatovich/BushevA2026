package com.furniture.controller;

import com.furniture.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.math.BigDecimal;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/balances")
    public String stockBalances(Model model) {
        model.addAttribute("materials", reportService.getMaterialBalances());
        model.addAttribute("totalMaterialValue", reportService.getTotalMaterialValue());
        model.addAttribute("lowStockCount", reportService.getLowStockMaterialsCount());

        model.addAttribute("products", reportService.getProductBalances());
        model.addAttribute("totalProductValue", reportService.getTotalProductValue());

        return "reports/balances";
    }

    @GetMapping("/production")
    public String productionStats(Model model) {
        model.addAttribute("orderStats", reportService.getOrderStats());
        model.addAttribute("averageCostPrice", reportService.getAverageCostPrice());
        return "reports/production";
    }

    @GetMapping("/revenue")
    public String revenueStats(Model model) {
        var monthlyRevenue = reportService.getMonthlyRevenue();
        BigDecimal totalRevenue = monthlyRevenue.stream()
                .map(r -> r.getValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("currentMonthRevenue", reportService.getCurrentMonthRevenue());
        model.addAttribute("todayRevenue", reportService.getTodayRevenue());
        return "reports/revenue";
    }
}
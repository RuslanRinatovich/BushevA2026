package com.furniture.controller;

import com.furniture.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final ProductionService productionService;
    private final ShipmentService shipmentService;
    private final MaterialService materialService;  // Добавить
    private final ReportService reportService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        String username = auth.getName();
        var user = userService.findByUsername(username);

        model.addAttribute("fullName", user.getFullName());
        model.addAttribute("role", user.getRole());
        model.addAttribute("activeOrders", productionService.countActiveOrders());
        model.addAttribute("lowStockMaterials", materialService.countLowStock());
        model.addAttribute("monthlyRevenue", shipmentService.getMonthlyRevenue());

        // Данные для графиков (только для директора)
        if ("DIRECTOR".equals(user.getRole())) {
            model.addAttribute("chartMonths", reportService.getLast6Months());
            model.addAttribute("chartRevenues", reportService.getLast6MonthsRevenue());
            model.addAttribute("productionStats", reportService.getProductionStats());
        }

        String role = user.getRole();
        if ("DIRECTOR".equals(role)) {
            return "dashboard/director";
        } else if ("MASTER".equals(role)) {
            return "dashboard/master";
        } else {
            return "dashboard/storekeeper";
        }
    }
}
package com.furniture.controller;

import com.furniture.dto.ProductionOrderDto;
import com.furniture.entity.ProductionOrder;
import com.furniture.service.ExcelExportService;
import com.furniture.service.FinishedProductService;
import com.furniture.service.ProductionService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/production")
@RequiredArgsConstructor
public class ProductionOrderController {

    private final ProductionService productionService;
    private final FinishedProductService productService;
    private final ExcelExportService excelExportService;

    @GetMapping("/orders/export/excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        List<ProductionOrder> orders = productionService.findAllOrders();
        excelExportService.exportProductionOrdersToExcel(orders, response);
    }
    @GetMapping("/orders")
    public String listOrders(@RequestParam(required = false) String search,
                             @RequestParam(required = false) String status,
                             Model model) {
        List<com.furniture.entity.ProductionOrder> orders;

        // Если есть поиск или фильтр
        if ((search != null && !search.isBlank()) || (status != null && !status.isBlank())) {
            orders = productionService.filterOrders(search, status);
            model.addAttribute("search", search);
            model.addAttribute("status", status);
        } else {
            orders = productionService.findAllOrders();
        }

        model.addAttribute("orders", orders);
        return "production/orders";
    }

    @GetMapping("/order/create")
    public String createOrderForm(Model model) {
        model.addAttribute("orderDto", new ProductionOrderDto());
        model.addAttribute("products", productService.findAll());
        return "production/order-form";
    }

    @PostMapping("/order/save")
    public String saveOrder(@ModelAttribute ProductionOrderDto dto,
                            Authentication auth,
                            RedirectAttributes ra) {
        try {
            productionService.createOrder(dto, auth.getName());
            ra.addFlashAttribute("success", "Заказ на производство создан");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/production/orders";
    }

    @PostMapping("/order/start/{id}")
    public String startOrder(@PathVariable Long id, RedirectAttributes ra) {
        try {
            productionService.startProduction(id);
            ra.addFlashAttribute("success", "Производство запущено");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/production/orders";
    }

    @PostMapping("/order/writeoff/{id}")
    public String writeOffMaterials(@PathVariable Long id, RedirectAttributes ra) {
        try {
            productionService.writeOffMaterials(id);
            ra.addFlashAttribute("success", "Материалы списаны со склада");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/production/orders";
    }

    @PostMapping("/order/complete/{id}")
    public String completeOrder(@PathVariable Long id, RedirectAttributes ra) {
        try {
            productionService.completeOrder(id);
            ra.addFlashAttribute("success", "Заказ завершён, продукция добавлена на склад");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/production/orders";
    }

    @PostMapping("/order/cancel/{id}")
    public String cancelOrder(@PathVariable Long id, RedirectAttributes ra) {
        try {
            productionService.cancelOrder(id);
            ra.addFlashAttribute("success", "Заказ отменён");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/production/orders";
    }
}
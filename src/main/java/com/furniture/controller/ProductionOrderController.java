package com.furniture.controller;

import com.furniture.dto.OrderDto;
import com.furniture.entity.FinishedProduct;
import com.furniture.entity.ProductionOrder;
import com.furniture.entity.User;
import com.furniture.service.FinishedProductService;
import com.furniture.service.ProductionService;
import com.furniture.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/production")
@RequiredArgsConstructor
public class ProductionOrderController {

    private final ProductionService productionService;
    private final FinishedProductService productService;
    private final UserService userService;

    @GetMapping("/orders")
    public String listOrders(Model model) {
        model.addAttribute("orders", productionService.findAllOrders());
        return "production/orders";
    }

    @GetMapping("/order/create")
    public String createOrderForm(Model model) {
        model.addAttribute("order", new OrderDto());
        model.addAttribute("products", productService.findAll());
        return "production/order-form";
    }

    @PostMapping("/order/save")
    public String saveOrder(@ModelAttribute OrderDto dto,
                            Authentication auth,
                            RedirectAttributes ra) {
        try {
            User user = userService.findByUsername(auth.getName());
            FinishedProduct product = productService.findById(dto.getProductId());

            ProductionOrder order = new ProductionOrder();
            order.setProduct(product);
            order.setPlannedQuantity(dto.getPlannedQuantity());
            order.setCreatedBy(user);

            productionService.createOrder(order);
            ra.addFlashAttribute("success", "Заказ создан");
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
            ra.addFlashAttribute("success", "Материалы списаны");
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
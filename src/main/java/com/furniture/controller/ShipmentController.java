package com.furniture.controller;

import com.furniture.dto.ShipmentDto;
import com.furniture.entity.Client;
import com.furniture.entity.FinishedProduct;
import com.furniture.entity.Shipment;
import com.furniture.entity.User;
import com.furniture.service.ClientService;
import com.furniture.service.FinishedProductService;
import com.furniture.service.ShipmentService;
import com.furniture.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final ClientService clientService;
    private final FinishedProductService productService;
    private final UserService userService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("shipments", shipmentService.findAll());
        return "shipments/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("shipment", new ShipmentDto());
        model.addAttribute("clients", clientService.findAll());
        model.addAttribute("products", productService.findAll());
        return "shipments/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ShipmentDto dto,
                       Authentication auth,
                       RedirectAttributes ra) {
        try {
            User user = userService.findByUsername(auth.getName());
            Client client = clientService.findById(dto.getClientId());
            FinishedProduct product = productService.findById(dto.getProductId());

            Shipment shipment = new Shipment();
            shipment.setClient(client);
            shipment.setProduct(product);
            shipment.setQuantity(dto.getQuantity());
            shipment.setPrice(dto.getPrice());
            shipment.setTotalAmount(dto.getQuantity().multiply(dto.getPrice()));
            shipment.setShipmentDate(dto.getShipmentDate());
            shipment.setCreatedBy(user);

            shipmentService.createShipment(shipment);
            ra.addFlashAttribute("success", "Отгрузка оформлена");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/shipments";
    }
}

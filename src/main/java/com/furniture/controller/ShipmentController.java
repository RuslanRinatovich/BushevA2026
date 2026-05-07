package com.furniture.controller;

import com.furniture.dto.ShipmentDto;
import com.furniture.service.*;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import com.furniture.entity.Shipment;

@Controller
@RequestMapping("/shipments")
@RequiredArgsConstructor
public class ShipmentController {
    private final ExcelExportService excelExportService;
    private final ShipmentService shipmentService;
    private final ClientService clientService;
    private final FinishedProductService productService;
    private final UserService userService;


    @GetMapping("/export/excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        List<Shipment> shipments = shipmentService.findAll();
        excelExportService.exportShipmentsToExcel(shipments, response);
    }

    @GetMapping
    public String list(@RequestParam(required = false) String search, Model model) {
        List<com.furniture.entity.Shipment> shipments;
        if (search != null && !search.isBlank()) {
            shipments = shipmentService.searchShipments(search);
            model.addAttribute("search", search);
        } else {
            shipments = shipmentService.findAll();
        }
        model.addAttribute("shipments", shipments);
        return "shipments/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("shipmentDto", new ShipmentDto());
        model.addAttribute("clients", clientService.findAll());
        model.addAttribute("products", productService.findAll());
        return "shipments/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ShipmentDto shipmentDto,
                       Authentication auth,
                       RedirectAttributes ra) {
        try {
            var user = userService.findByUsername(auth.getName());
            shipmentService.createShipment(shipmentDto, user);
            ra.addFlashAttribute("success", "Отгрузка оформлена успешно");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/shipments";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            shipmentService.deleteShipment(id);
            ra.addFlashAttribute("success", "Отгрузка удалена");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/shipments";
    }
}
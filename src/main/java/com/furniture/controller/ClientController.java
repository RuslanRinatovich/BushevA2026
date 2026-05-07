package com.furniture.controller;

import com.furniture.entity.Client;
import com.furniture.service.ClientService;
import com.furniture.service.ExcelExportService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ExcelExportService excelExportService;

    private final ClientService clientService;
    @GetMapping("/export/excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        List<Client> clients = clientService.findAll();
        excelExportService.exportClientsToExcel(clients, response);
    }
    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       Model model) {
        List<Client> clients;
        if (search != null && !search.isBlank()) {
            clients = clientService.search(search);
            model.addAttribute("search", search);
        } else {
            clients = clientService.findAll();
        }
        model.addAttribute("clients", clients);
        return "clients/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("client", new Client());
        return "clients/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Client client = clientService.findById(id);
        model.addAttribute("client", client);
        return "clients/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Client client,
                       RedirectAttributes ra) {
        try {
            clientService.save(client);
            if (client.getId() == null) {
                ra.addFlashAttribute("success", "Клиент успешно добавлен");
            } else {
                ra.addFlashAttribute("success", "Клиент успешно обновлён");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/clients";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         RedirectAttributes ra) {
        try {
            clientService.deleteById(id);
            ra.addFlashAttribute("success", "Клиент удалён");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Ошибка при удалении клиента");
        }
        return "redirect:/clients";
    }
}
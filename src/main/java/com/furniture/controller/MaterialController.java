package com.furniture.controller;

import com.furniture.entity.Material;
import com.furniture.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    // Список единиц измерения - вынесен в константу
    private static final List<String> UNIT_OPTIONS = Arrays.asList(
            "лист", "м.пог", "м²", "кг", "упак", "шт", "комплект", "пара"
    );

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) String status,
                       Model model) {
        List<Material> materials = materialService.findAll();

        if (search != null && !search.isBlank()) {
            materials = materials.stream()
                    .filter(m -> m.getName().toLowerCase().contains(search.toLowerCase()) ||
                            m.getSku().toLowerCase().contains(search.toLowerCase()))
                    .toList();
            model.addAttribute("search", search);
        }

        if (status != null && !status.isBlank()) {
            if ("low".equals(status)) {
                materials = materials.stream()
                        .filter(m -> m.getCurrentBalance().compareTo(m.getMinBalance()) < 0)
                        .toList();
            } else if ("normal".equals(status)) {
                materials = materials.stream()
                        .filter(m -> m.getCurrentBalance().compareTo(m.getMinBalance()) >= 0)
                        .toList();
            }
            model.addAttribute("status", status);
        }

        model.addAttribute("materials", materials);
        return "materials/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("material", new Material());
        model.addAttribute("unitOptions", UNIT_OPTIONS);  // Обязательно добавляем
        return "materials/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Material material = materialService.findById(id);
        model.addAttribute("material", material);
        model.addAttribute("unitOptions", UNIT_OPTIONS);  // Обязательно добавляем
        return "materials/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Material material, RedirectAttributes ra) {
        try {
            // Проверка уникальности SKU для нового материала
            if (material.getId() == null) {
                try {
                    materialService.findBySku(material.getSku());
                    ra.addFlashAttribute("error", "Материал с артикулом '" + material.getSku() + "' уже существует");
                    return "redirect:/materials/create";
                } catch (RuntimeException e) {
                    // SKU не найден, можно сохранять
                }
            }
            materialService.save(material);
            ra.addFlashAttribute("success", "Материал сохранён");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/materials/create";
        }
        return "redirect:/materials";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            materialService.deleteById(id);
            ra.addFlashAttribute("success", "Материал удалён");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/materials";
    }
}
package com.furniture.controller;

import com.furniture.dto.ProductDto;
import com.furniture.entity.FinishedProduct;
import com.furniture.entity.ProductCategory;
import com.furniture.service.ExcelExportService;
import com.furniture.service.FinishedProductService;
import com.furniture.service.ProductCategoryService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final FinishedProductService productService;
    private final ProductCategoryService categoryService;
    private final ExcelExportService excelExportService;

    @GetMapping("/export/excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        List<FinishedProduct> products = productService.findAll();
        excelExportService.exportProductsToExcel(products, response);
    }
    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) Long categoryId,
                       Model model) {
        List<FinishedProduct> products;
        if (categoryId != null && categoryId > 0) {
            products = productService.filterByCategory(categoryId);
            model.addAttribute("selectedCategoryId", categoryId);
        } else {
            products = productService.search(search);
        }

        model.addAttribute("products", products);
        model.addAttribute("search", search);
        model.addAttribute("categories", categoryService.findAll());
        return "products/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new ProductDto());
        model.addAttribute("categories", categoryService.findAll());
        return "products/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ProductDto dto,
                       @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                       RedirectAttributes ra) {
        try {
            FinishedProduct product = new FinishedProduct();
            product.setId(dto.getId());
            product.setSku(dto.getSku());
            product.setName(dto.getName());
            product.setUnit(dto.getUnit());
            product.setSellingPrice(dto.getSellingPrice());

            if (dto.getCategoryId() != null) {
                ProductCategory category = categoryService.findById(dto.getCategoryId());
                product.setCategory(category);
            }

            // Важно: не устанавливаем image из DTO
            // product.setImage(...) - не делаем этого

            if (dto.getId() == null) {
                // Новый продукт - передаём файл
                productService.save(product, imageFile);
                ra.addFlashAttribute("success", "Продукт добавлен");
            } else {
                // Обновление - передаём файл и ID
                productService.update(product, imageFile);
                ra.addFlashAttribute("success", "Продукт обновлён");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            e.printStackTrace();
            return dto.getId() == null ? "redirect:/products/create" : "redirect:/products/edit/" + dto.getId();
        }
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        FinishedProduct product = productService.findById(id);
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setUnit(product.getUnit());
        dto.setSellingPrice(product.getSellingPrice());
        dto.setCostPrice(product.getCostPrice());
        dto.setCurrentBalance(product.getCurrentBalance());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        dto.setHasImage(product.getImage() != null);

        model.addAttribute("product", dto);
        model.addAttribute("categories", categoryService.findAll());
        return "products/form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            productService.deleteById(id);
            ra.addFlashAttribute("success", "Продукт удалён");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products";
    }

    @GetMapping("/image/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        byte[] image = productService.getImage(id);
        if (image == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
    }
}
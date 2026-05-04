package com.furniture.service;

import com.furniture.entity.FinishedProduct;
import com.furniture.entity.ProductCategory;
import com.furniture.repository.FinishedProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinishedProductService {

    private final FinishedProductRepository productRepository;

    public List<FinishedProduct> findAll() {
        return productRepository.findAll();
    }

    public FinishedProduct findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Продукт не найден"));
    }

    public FinishedProduct findBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Продукт с артикулом " + sku + " не найден"));
    }

    public List<FinishedProduct> findByCategory(ProductCategory category) {
        return productRepository.findByCategory(category);
    }

    public List<FinishedProduct> search(String search) {
        if (search == null || search.isBlank()) {
            return productRepository.findAll();
        }
        return productRepository.findAll().stream()
                .filter(p -> p.getName().toLowerCase().contains(search.toLowerCase()) ||
                        p.getSku().toLowerCase().contains(search.toLowerCase()))
                .toList();
    }

    public List<FinishedProduct> filterByCategory(Long categoryId) {
        if (categoryId == null || categoryId == 0) {
            return productRepository.findAll();
        }
        return productRepository.findAll().stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
                .toList();
    }

    @Transactional
    public FinishedProduct save(FinishedProduct product, MultipartFile imageFile) throws IOException {
        if (product.getId() == null && productRepository.existsBySku(product.getSku())) {
            throw new RuntimeException("Продукт с артикулом '" + product.getSku() + "' уже существует");
        }

        product.setCreatedAt(LocalDateTime.now());

        if (imageFile != null && !imageFile.isEmpty()) {
            product.setImage(imageFile.getBytes());
        } else {
            product.setImage(null);
        }

        return productRepository.save(product);
    }

    @Transactional
    public FinishedProduct update(FinishedProduct product, MultipartFile imageFile) throws IOException {
        FinishedProduct existing = findById(product.getId());

        // Проверка уникальности SKU при изменении
        if (!existing.getSku().equals(product.getSku()) && productRepository.existsBySku(product.getSku())) {
            throw new RuntimeException("Продукт с артикулом '" + product.getSku() + "' уже существует");
        }

        existing.setSku(product.getSku());
        existing.setName(product.getName());
        existing.setCategory(product.getCategory());
        existing.setSellingPrice(product.getSellingPrice());
        existing.setUnit(product.getUnit());

        if (imageFile != null && !imageFile.isEmpty()) {
            existing.setImage(imageFile.getBytes());
        }
        // Если файл не загружен - оставляем существующее изображение (не трогаем)

        return productRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    @Transactional
    public void updateBalance(Long id, BigDecimal quantity) {
        FinishedProduct product = findById(id);
        BigDecimal newBalance = product.getCurrentBalance().add(quantity);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Недостаточно продукции на складе");
        }
        product.setCurrentBalance(newBalance);
        productRepository.save(product);
    }

    @Transactional
    public void updateCostPrice(Long productId, BigDecimal costPrice) {
        FinishedProduct product = findById(productId);
        product.setCostPrice(costPrice);
        productRepository.save(product);
    }

    public byte[] getImage(Long id) {
        FinishedProduct product = findById(id);
        return product.getImage();
    }
}
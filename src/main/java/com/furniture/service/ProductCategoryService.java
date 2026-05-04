package com.furniture.service;

import com.furniture.entity.ProductCategory;
import com.furniture.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;

    public List<ProductCategory> findAll() {
        return categoryRepository.findAll();
    }

    public ProductCategory findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));
    }

    @Transactional
    public ProductCategory save(ProductCategory category) {
        if (category.getName() == null || category.getName().isBlank()) {
            throw new RuntimeException("Название категории обязательно");
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }
}
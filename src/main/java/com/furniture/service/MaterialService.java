package com.furniture.service;

import com.furniture.entity.Material;
import com.furniture.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;

    public List<Material> findAll() {
        return materialRepository.findAll();
    }

    public Material findById(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Материал не найден"));
    }

    public Material findBySku(String sku) {
        return materialRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Материал с артикулом " + sku + " не найден"));
    }

    @Transactional
    public Material save(Material material) {
        if (material.getId() == null && materialRepository.existsBySku(material.getSku())) {
            throw new RuntimeException("Материал с артикулом '" + material.getSku() + "' уже существует");
        }
        return materialRepository.save(material);
    }

    @Transactional
    public void deleteById(Long id) {
        materialRepository.deleteById(id);
    }

    @Transactional
    public void updateBalance(Long id, BigDecimal quantity) {
        Material material = findById(id);
        BigDecimal newBalance = material.getCurrentBalance().add(quantity);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Недостаточно материала на складе");
        }
        material.setCurrentBalance(newBalance);
        materialRepository.save(material);
    }

    public List<Material> getMaterialsWithLowStock() {
        return materialRepository.findLowStockMaterials();
    }

    // Добавленный метод для дашборда
    public long countLowStock() {
        return materialRepository.findLowStockMaterials().size();
    }
}
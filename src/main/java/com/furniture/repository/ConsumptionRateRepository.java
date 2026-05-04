package com.furniture.repository;

import com.furniture.entity.ConsumptionRate;
import com.furniture.entity.FinishedProduct;
import com.furniture.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ConsumptionRateRepository extends JpaRepository<ConsumptionRate, Long> {
    List<ConsumptionRate> findByProduct(FinishedProduct product);
    Optional<ConsumptionRate> findByProductAndMaterial(FinishedProduct product, Material material);
    void deleteByProduct(FinishedProduct product);

    @Query("SELECT cr FROM ConsumptionRate cr JOIN FETCH cr.material WHERE cr.product = :product")
    List<ConsumptionRate> findByProductWithMaterials(FinishedProduct product);
}
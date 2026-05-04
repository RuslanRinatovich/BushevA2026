package com.furniture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "consumption_rates", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "material_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsumptionRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private FinishedProduct product;

    @ManyToOne
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
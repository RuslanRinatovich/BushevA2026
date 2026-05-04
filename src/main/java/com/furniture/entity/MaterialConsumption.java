package com.furniture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "material_consumption")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private ProductionOrder order;

    @ManyToOne
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "planned_quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal plannedQuantity;

    @Column(name = "actual_quantity", precision = 12, scale = 3)
    private BigDecimal actualQuantity;

    @Column(name = "written_off")
    private Boolean writtenOff = false;

    @Column(name = "written_off_at")
    private LocalDateTime writtenOffAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
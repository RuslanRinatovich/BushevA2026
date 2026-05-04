package com.furniture.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProductionOrderDto {
    private Long id;
    private String orderNumber;
    private Long productId;
    private String productName;
    private BigDecimal plannedQuantity;
    private BigDecimal actualQuantity;
    private String status;
    private LocalDate plannedDate;
    private LocalDate completedDate;
    private String createdBy;
}
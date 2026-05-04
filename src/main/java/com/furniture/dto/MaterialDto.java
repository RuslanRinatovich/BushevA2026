package com.furniture.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MaterialDto {
    private Long id;
    private String sku;
    private String name;
    private String unit;
    private BigDecimal price;
    private BigDecimal currentBalance;
    private BigDecimal minBalance;
}
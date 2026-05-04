package com.furniture.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDto {
    private Long id;
    private String sku;
    private String name;
    private Long categoryId;
    private String categoryName;
    private String unit;
    private BigDecimal sellingPrice;
    private BigDecimal costPrice;
    private BigDecimal currentBalance;
    private boolean hasImage;
}
package com.nihar.inventory_service.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProductBatchDto {
    private Long id;
    private Long productId;
    private String batchNumber;
    private Integer quantity;
    private LocalDate expiryDate;
}

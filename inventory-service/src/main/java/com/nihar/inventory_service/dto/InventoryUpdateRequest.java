package com.nihar.inventory_service.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryUpdateRequest {

    @NotNull(message = "Product ID cannot be Null")
    private Long productId;

    @Min(value = 1, message = "Quantity must be atleast 1")
    private int quantity;
}

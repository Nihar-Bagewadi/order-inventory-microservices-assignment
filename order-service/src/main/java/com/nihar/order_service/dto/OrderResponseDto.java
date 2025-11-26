package com.nihar.order_service.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponseDto {

    private String orderNumber;
    private Long productId;
    private int quantity;
}

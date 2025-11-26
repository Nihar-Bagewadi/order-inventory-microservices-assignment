package com.nihar.inventory_service.service;

import com.nihar.inventory_service.dto.InventoryUpdateRequest;
import com.nihar.inventory_service.dto.ProductBatchDto;
import com.nihar.inventory_service.dto.ProductDto;
import com.nihar.inventory_service.model.Product;
import com.nihar.inventory_service.model.ProductBatch;

import java.util.List;

public interface InventoryService {

    List<ProductBatchDto> getBatchesByProduct(Long productId);
    void updateInventory(InventoryUpdateRequest request);
    List<ProductDto> getProducts();
}

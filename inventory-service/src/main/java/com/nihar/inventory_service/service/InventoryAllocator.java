package com.nihar.inventory_service.service;

import com.nihar.inventory_service.model.ProductBatch;

import java.util.List;

public interface InventoryAllocator {
    void allocate(List<ProductBatch> batches, int requiredQty);

}

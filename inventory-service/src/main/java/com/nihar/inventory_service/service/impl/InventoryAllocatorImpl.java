package com.nihar.inventory_service.service.impl;

import com.nihar.inventory_service.model.ProductBatch;
import com.nihar.inventory_service.service.InventoryAllocator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryAllocatorImpl implements InventoryAllocator {

    @Override
    public void allocate(List<ProductBatch> batches, int requiredQty) {

        List<ProductBatch> usedBatches = new ArrayList<>();

        for (ProductBatch batch : batches) {
            if (requiredQty <= 0) break;

            int used = Math.min(batch.getQuantity(), requiredQty);
            batch.setQuantity(batch.getQuantity() - used);
            requiredQty -= used;

            usedBatches.add(batch);
        }

    }
}
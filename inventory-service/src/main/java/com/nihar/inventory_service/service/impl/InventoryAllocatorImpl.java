package com.nihar.inventory_service.service.impl;

import com.nihar.inventory_service.model.ProductBatch;
import com.nihar.inventory_service.service.InventoryAllocator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class InventoryAllocatorImpl implements InventoryAllocator {

    @Override
    public void allocate(List<ProductBatch> batches, int requiredQty) {

        List<ProductBatch> usedBatches = new ArrayList<>();

        log.debug("Iterating through the batches to allocation quantity");
        for (ProductBatch batch : batches) {
            if (requiredQty <= 0) break;

            int used = Math.min(batch.getQuantity(), requiredQty);
            batch.setQuantity(batch.getQuantity() - used);
            requiredQty -= used;

            usedBatches.add(batch);
        }

        log.debug("Number of Batches used : {}", usedBatches.size());
    }
}
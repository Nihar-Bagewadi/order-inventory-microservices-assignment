package com.nihar.inventory_service.service.factory;

import com.nihar.inventory_service.service.InventoryAllocator;
import com.nihar.inventory_service.service.impl.InventoryAllocatorImpl;
import org.springframework.stereotype.Component;

@Component
public class InventoryAllocatorFactory {

    public InventoryAllocator getAllocator(String type) {
        if ("FEFO".equalsIgnoreCase(type)) {
            return new InventoryAllocatorImpl();
        }
        return new InventoryAllocatorImpl(); // default strategy
    }
}

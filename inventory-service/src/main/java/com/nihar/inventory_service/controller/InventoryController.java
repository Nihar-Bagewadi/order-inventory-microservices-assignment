package com.nihar.inventory_service.controller;

import com.nihar.inventory_service.dto.InventoryUpdateRequest;
import com.nihar.inventory_service.dto.ProductBatchDto;
import com.nihar.inventory_service.dto.ProductDto;
import com.nihar.inventory_service.model.Product;
import com.nihar.inventory_service.model.ProductBatch;
import com.nihar.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/get-products")
    public List<ProductDto> getProduts() {
        return inventoryService.getProducts();
    }

    @GetMapping("/{productId}")
    public List<ProductBatchDto> getInventory(@PathVariable Long productId) {
        return inventoryService.getBatchesByProduct(productId);
    }

    @PostMapping("/update")
    public String updateInventory(@RequestBody InventoryUpdateRequest request) {
        inventoryService.updateInventory(request);
        return "Inventory updated successfully";
    }
}

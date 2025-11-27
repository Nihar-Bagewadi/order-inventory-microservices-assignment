package com.nihar.inventory_service.controller;

import com.nihar.inventory_service.dto.InventoryUpdateRequest;
import com.nihar.inventory_service.dto.ProductBatchDto;
import com.nihar.inventory_service.dto.ProductDto;
import com.nihar.inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/get-products")
    public ResponseEntity<List<ProductDto>> getProducts() {

        log.info("Fetching List of Products in Inventory");
        List<ProductDto> products = inventoryService.getProducts();

        return new ResponseEntity<>(products,HttpStatus.OK);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<List<ProductBatchDto>> getInventory(@Validated @PathVariable Long productId) {
        log.info("Fetching the Batches of Product with Id : {}", productId);
        List<ProductBatchDto> batchesByProduct = inventoryService.getBatchesByProduct(productId);
        return new ResponseEntity<>(batchesByProduct, HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateInventory(@Valid @RequestBody InventoryUpdateRequest request) {

        inventoryService.updateInventory(request);
        log.info("Updated Inventory Successfully");
        return new ResponseEntity<>("Inventory updated successfully", HttpStatus.ACCEPTED);
    }
}

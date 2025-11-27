package com.nihar.inventory_service.service.impl;

import com.nihar.inventory_service.dto.InventoryUpdateRequest;
import com.nihar.inventory_service.dto.ProductBatchDto;
import com.nihar.inventory_service.dto.ProductDto;
import com.nihar.inventory_service.exception.InsufficientInventoryException;
import com.nihar.inventory_service.exception.ProductNotFoundException;
import com.nihar.inventory_service.model.Product;
import com.nihar.inventory_service.model.ProductBatch;
import com.nihar.inventory_service.repository.ProductBatchRepository;
import com.nihar.inventory_service.repository.ProductRepository;
import com.nihar.inventory_service.service.InventoryService;
import com.nihar.inventory_service.service.factory.InventoryAllocatorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final ProductBatchRepository batchRepository;
    private final InventoryAllocatorFactory allocatorFactory;

    public List<ProductBatchDto> getBatchesByProduct(Long productId) {

        log.debug("Fetching Product from Repository");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        log.debug("Fetching the batches from Repository");
        return batchRepository.findByProductIdOrderByExpiryDateAsc(product.getId()).stream()
                .map(batch -> {
                    ProductBatchDto batchDto = new ProductBatchDto();
                    batchDto.setId(batch.getId());
                    batchDto.setBatchNumber(batch.getBatchNumber());
                    batchDto.setExpiryDate(batch.getExpiryDate());
                    batchDto.setQuantity(batch.getQuantity());
                    batchDto.setProductId(batch.getProduct().getId());

                    return batchDto;
                }).toList();
    }

    public void updateInventory(InventoryUpdateRequest request) {

        log.debug("Fetching Product Information from Repository");
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        log.debug("Fetching list of Product Batches for id : {}", product.getId());
        List<ProductBatch> batches =
                batchRepository.findByProductIdOrderByExpiryDateAsc(product.getId());

        log.debug("Checking availablity of Product");
        int totalStock = batches.stream()
                .mapToInt(ProductBatch::getQuantity)
                .sum();

        if(totalStock < request.getQuantity()) {
            throw new InsufficientInventoryException("Not enough stock for product " + product.getName());
        }

        log.debug("Fetching the Inventory Allocator");
        var allocator = allocatorFactory.getAllocator("FEFO");

        log.info("Allocating the quantity from Batches");
        allocator.allocate(batches, request.getQuantity());

        log.info("Updating the batches in the database");
        batchRepository.saveAll(batches);
    }

    @Override
    public List<ProductDto> getProducts() {
        return productRepository.findAll().stream()
                .map(product -> {
                    ProductDto dto = new ProductDto();
                    dto.setId(product.getId());
                    dto.setName(product.getName());
                    dto.setDescription(product.getDescription());
                    return dto;
                })
                .toList();
    }
}

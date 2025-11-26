package com.nihar.inventory_service.service.impl;

import com.nihar.inventory_service.dto.InventoryUpdateRequest;
import com.nihar.inventory_service.dto.ProductBatchDto;
import com.nihar.inventory_service.dto.ProductDto;
import com.nihar.inventory_service.model.Product;
import com.nihar.inventory_service.model.ProductBatch;
import com.nihar.inventory_service.repository.ProductBatchRepository;
import com.nihar.inventory_service.repository.ProductRepository;
import com.nihar.inventory_service.service.InventoryService;
import com.nihar.inventory_service.service.factory.InventoryAllocatorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final ProductBatchRepository batchRepository;
    private final InventoryAllocatorFactory allocatorFactory;

    public List<ProductBatchDto> getBatchesByProduct(Long productId) {
        return batchRepository.findByProductIdOrderByExpiryDateAsc(productId).stream()
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
        List<ProductBatch> batches =
                batchRepository.findByProductIdOrderByExpiryDateAsc(request.getProductId());

        var allocator = allocatorFactory.getAllocator("FEFO");

        allocator.allocate(batches, request.getQuantity());

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

package com.nihar.inventory_service.repository;

import com.nihar.inventory_service.model.ProductBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long> {

    List<ProductBatch> findByProductIdOrderByExpiryDateAsc(Long productId);

    @Query("""
            select b
            from ProductBatch b
            where b.product.id = :productId
            and b.quantity > 0
            and b.expiryDate >= :today
            order by b.expiryDate asc
            """)
    List<ProductBatch> findAvailableByProductIdOrderByExpiryDateAsc(@Param("productId") Long productId, @Param("today") LocalDate today);

}

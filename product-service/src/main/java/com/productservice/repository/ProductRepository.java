package com.productservice.repository;

import com.productservice.model.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
          UPDATE Product p SET
          p.name = COALESCE(:name, p.name),
          p.price = COALESCE(:price, p.price),
          p.stock = COALESCE(:stock, p.stock),
          p.lastUpdateDate = COALESCE(:lastUpdateDate, p.lastUpdateDate),
          p.barcode = COALESCE(:barcode, p.barcode)
          WHERE p.barcode = :barcode""")
    void updateProductByBarcode(String barcode,
                                String name,
                                BigDecimal price,
                                Integer stock,
                                LocalDateTime lastUpdateDate);

    @Query("SELECT p FROM Product p WHERE p.image.imageCode = :imageCode")
    Optional<Product> findByImageCode(Long imageCode);
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(concat(:prefix, '%')) AND p.deleted = false")
    Page<Product> findByNameStartingWithIgnoreCaseAndDeletedFalse(String prefix, Pageable pageable);


    Optional<Product> findById(Long id);
    Optional<Product> findByBarcode(String barcode);
    Optional<Product> findByBarcodeAndDeletedFalse(String barcode);



    Page<Product> findAll(Pageable pageable);
    Page<Product> findByNameStartingWithIgnoreCase(String prefix, Pageable pageablei);

}

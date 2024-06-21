package com.reportingservice.repository;

import com.reportingservice.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    Page<Sale> findAll(Specification<Sale> spec, Pageable pageable);
    List<Sale> findAll(Specification<Sale> spec);
}

package com.saleservice.repository;

import com.saleservice.model.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    Optional<Campaign> findById(Long id);
    boolean existsByName(String Name);

    Page<Campaign> findAll(Pageable pageable);
    Page<Campaign> findAllByDeletedFalse(Pageable pageable);
}

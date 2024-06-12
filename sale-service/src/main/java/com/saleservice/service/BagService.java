package com.saleservice.service;

import com.saleservice.dto.BagDto;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;


public interface BagService {

    @Transactional
    BagDto addProductToBag(Long bagId, String barcode, int quantity);  //String userId alabilir parametre

    @Transactional
    BagDto removeProductFromBag(Long bagId, String barcode, int quantity);

    @Transactional
    BagDto removeAllProductsFromBag(Long bagId);

    @Transactional
    void deleteBagById(Long bagId);

    BagDto getBagById(Long bagId);

    Page<BagDto> getAllBags(int pageNumber, int pageSize);

    @Transactional
    BagDto applyCampaignToBag(Long bagId, Long campaignId);

    @Transactional
    BagDto removeCampaignFromBag(Long bagId);
}

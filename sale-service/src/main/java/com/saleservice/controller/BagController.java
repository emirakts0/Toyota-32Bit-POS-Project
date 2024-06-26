package com.saleservice.controller;

import com.saleservice.dto.BagDto;
import com.saleservice.service.BagService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/sale/bags")
@Validated
public class BagController {

    private final BagService bagService;


    @PostMapping("/products")
    public ResponseEntity<BagDto> addProductToBag(@RequestParam(required = false) Long bagId,
                                                  @RequestParam String barcode,
                                                  @RequestParam @Positive(message = "Quantity must be positive.")
                                                      int quantity) {
        log.trace("addProductToBag endpoint called with bagId: {}, barcode: {}, quantity: {}", bagId, barcode, quantity);

        BagDto bagDto = bagService.addProductToBag(bagId, barcode, quantity);
        return ResponseEntity.ok(bagDto);
    }


    @DeleteMapping("/products")
    public ResponseEntity<BagDto> removeProductFromBag(@RequestParam(defaultValue = "") Long bagId,
                                                       @RequestParam(defaultValue = "") String barcode,
                                                       @RequestParam
                                                           @Positive(message = "Quantity must be positive.")
                                                           int quantity) {
        log.trace("removeProductFromBag endpoint called with bagId: {}, barcode: {}, quantity: {}", bagId, barcode, quantity);

        BagDto bagDto = bagService.removeProductFromBag(bagId, barcode, quantity);
        return ResponseEntity.ok(bagDto);
    }


    @DeleteMapping("/{bagId}/products")
    public ResponseEntity<BagDto> removeAllProductsInTheBag(@PathVariable Long bagId) {
        log.trace("removeAllProductsInTheBag endpoint called with bagId: {}", bagId);

        BagDto bagDto = bagService.removeAllProductsFromBag(bagId);
        return ResponseEntity.ok(bagDto);
    }


    @GetMapping("/{bagId}")
    public ResponseEntity<BagDto> getBagById(@PathVariable Long bagId) {
        log.trace("getBagById endpoint called with bagId: {}", bagId);

        BagDto bagDto = bagService.getBagById(bagId);
        return ResponseEntity.ok(bagDto);
    }


    @GetMapping
    public ResponseEntity<Page<BagDto>> getAllBags(@RequestParam(defaultValue = "1") int pageNumber,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        log.trace("getAllBags endpoint called with pageNumber: {}, pageSize: {}", pageNumber, pageSize);

        Page<BagDto> bagPage = bagService.getAllBags(pageNumber, pageSize);
        return ResponseEntity.ok().body(bagPage);
    }


    @DeleteMapping("/{bagId}")
    public ResponseEntity<String> deleteBag(@PathVariable Long bagId) {
        log.trace("deleteBag endpoint called with bagId: {}", bagId);

        bagService.deleteBagById(bagId);
        return ResponseEntity.ok().body(String.format("The bag with ID %d has been deleted.", bagId));
    }


    @PostMapping("/{bagId}/campaign/{campaignId}")
    public ResponseEntity<BagDto> applyCampaignToBag(@PathVariable Long bagId,
                                                     @PathVariable Long campaignId) {
        log.trace("applyCampaignToBag endpoint called with bagId: {}, campaignId: {}", bagId, campaignId);

        BagDto bagDto = bagService.applyCampaignToBag(bagId, campaignId);
        return ResponseEntity.ok(bagDto);
    }


    @DeleteMapping("/{bagId}/campaign")
    public ResponseEntity<BagDto> removeCampaignFromBag(@PathVariable Long bagId) {
        log.trace("removeCampaignFromBag endpoint called with bagId: {}", bagId);

        BagDto bagDto = bagService.removeCampaignFromBag(bagId);
        return ResponseEntity.ok(bagDto);
    }
}

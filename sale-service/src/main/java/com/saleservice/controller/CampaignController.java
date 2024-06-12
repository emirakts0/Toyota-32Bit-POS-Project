package com.saleservice.controller;


import com.saleservice.dto.CampaignDto;
import com.saleservice.dto.CampaignResponseDto;
import com.saleservice.service.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/sale/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    //TODO: girilen Discount type 2 sinden biri olmalı validasyon ekle, Şuande JSON TYPE ERROR dönüyor, daha hoş gözükebilir.

    @PostMapping
    public ResponseEntity<String> addCampaign(@RequestBody @Valid CampaignDto requestDto) {
        log.trace("addCampaign endpoint called with requestDto: {}", requestDto);

        campaignService.addCampaign(requestDto);

        return ResponseEntity.ok("Successfully added campaign");
    }

    @PutMapping("/{id}")
    public ResponseEntity<CampaignResponseDto> updateCampaignDates(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "") String startDate,
            @RequestParam(required = false, defaultValue = "") String endDate) {
        log.trace("updateCampaignDates endpoint called with id: {}, startDate: {}, endDate: {}", id, startDate, endDate);

        CampaignResponseDto updatedCampaign = campaignService.updateCampaignDates(
                id,
                startDate,
                endDate);
        return ResponseEntity.ok(updatedCampaign);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCampaign(@PathVariable Long id) {
        log.trace("deleteCampaign endpoint called with id: {}", id);

        campaignService.deleteCampaignById(id);

        return ResponseEntity.ok(String.format("Campaign with ID %d succesfully deleted", id));
    }

    @GetMapping
    public ResponseEntity<Page<CampaignResponseDto>> getAllCampaigns(@RequestParam(defaultValue = "1") int pageNumber,
                                                                     @RequestParam(defaultValue = "10") int pageSize,
                                                                     @RequestParam(defaultValue = "true")boolean hideDeleted) {
        log.trace("getAllCampaigns endpoint called with pageNumber: {}, pageSize: {}, hideDeleted: {}",
                pageNumber, pageSize, hideDeleted);

        Page<CampaignResponseDto> page = campaignService.getAllCampaigns(pageNumber, pageSize, hideDeleted);

        return ResponseEntity.ok().body(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponseDto> getCampaignById(@PathVariable Long id) {
        log.trace("getCampaignById endpoint called with id: {}", id);

        CampaignResponseDto campaign = campaignService.getCampaignById(id);

        return ResponseEntity.ok().body(campaign);
    }

}

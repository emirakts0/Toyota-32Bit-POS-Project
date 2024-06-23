package com.saleservice.service.impl;

import com.saleservice.dto.CampaignDto;
import com.saleservice.dto.CampaignResponseDto;
import com.saleservice.exception.CampaignAlreadyDeletedException;
import com.saleservice.exception.CampaignAlreadyExistException;
import com.saleservice.exception.CampaignNotFoundException;
import com.saleservice.exception.InvalidInputException;
import com.saleservice.model.Campaign;
import com.saleservice.model.DiscountType;
import com.saleservice.repository.CampaignRepository;
import com.saleservice.service.CampaignService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@RequiredArgsConstructor
@Service
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepository;
    private final ModelMapper modelMapper;


    @Transactional
    @Override
    public CampaignDto addCampaign(CampaignDto campaignDto) {
        log.trace("addCampaign method begins. CampaignDto: {}", campaignDto);

        if (campaignRepository.existsByName(campaignDto.getName())) {
            log.warn("addCampaign: Campaign with name {} already exists", campaignDto.getName());
            throw new CampaignAlreadyExistException("Campaign with name " + campaignDto.getName() + " already exists");
        }
        if (campaignDto.getDiscountType() == DiscountType.PERCENTAGE && campaignDto.getDiscountValue() > 100) {
            log.warn("addCampaign: Discount value cannot be greater than 100 for percentage discounts");
            throw new InvalidInputException("Discount value cannot be greater than 100 for percentage discounts.");
        }

        LocalDateTime startDate = parseStartDate(campaignDto.getStartDate());
        LocalDateTime endDate = parseDate(campaignDto.getEndDate());

        if (endDate == null) {
            log.warn("addCampaign: End date is null");
            throw new InvalidInputException("End date cannot be null or empty."); }
        if (startDate.isAfter(endDate)) {
            log.warn("addCampaign: Start date {} cannot be after the end date {}", startDate, endDate);
            throw new InvalidInputException("Start date cannot be after the end date."); }

        Campaign campaign = new Campaign(
                campaignDto.getName(),
                startDate,
                endDate,
                campaignDto.getDiscountType(),
                campaignDto.getDiscountValue());

        campaignRepository.save(campaign);

        log.info("addCampaign: Campaign added successfully. CampaignDto: {}", campaignDto);
        log.trace("addCampaign method ends. CampaignDto: {}", campaignDto);
        return campaignDto;
    }


    @Transactional
    @Override
    public CampaignResponseDto updateCampaignDates( Long id,
                                                    String startDate,
                                                    String endDate) {
        log.trace("updateCampaignDates method begins. ID: {}, StartDate: {}, EndDate: {}", id, startDate, endDate);

        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("updateCampaignDates: Campaign with ID {} not found", id);
                    return new CampaignNotFoundException(String.format("Campaign with ID %d not found", id)); });

        if (campaign.isDeleted()) {
            log.warn("updateCampaignDates: Cannot update a deleted campaign with ID {}", id);
            throw new CampaignAlreadyDeletedException("Cannot update a deleted campaign."); }

        if (startDate != null && !startDate.isEmpty()) {
            LocalDateTime newStartDate = parseStartDate(startDate);
            campaign.setStartDate(newStartDate); }

        if (endDate != null && !endDate.isEmpty()) {
            LocalDateTime newEndDate = parseDate(endDate);
            campaign.setEndDate(newEndDate); }

        if (campaign.getEndDate().isBefore(campaign.getStartDate())) {
            log.warn("updateCampaignDates: End date {} cannot be before the start date {}", campaign.getEndDate(), campaign.getStartDate());
            throw new InvalidInputException("End date cannot be before the start date."); }

        campaignRepository.save(campaign);

        log.info("updateCampaignDates: Campaign dates updated successfully. ID: {}", id);
        log.trace("updateCampaignDates method ends. ID: {}, StartDate: {}, EndDate: {}", id, startDate, endDate);
        return modelMapper.map(campaign, CampaignResponseDto.class);
    }


    @Override
    @Transactional
    public void deleteCampaignById(Long id) {
        log.trace("deleteCampaignById method begins. ID: {}", id);

        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("deleteCampaignById: Campaign with ID {} not found", id);
                    return new CampaignNotFoundException(String.format("Campaign with ID %d not found", id)); });

        if (campaign.isDeleted()) {
            log.warn("deleteCampaignById: Campaign with ID {} already deleted", id);
            throw new CampaignAlreadyDeletedException(String.format("Campaign with ID %d already deleted", id));
        }

        campaign.setDeleted(true);
        campaignRepository.save(campaign);

        log.info("deleteCampaignById: Campaign deleted successfully. ID: {}", id);
        log.trace("deleteCampaignById method ends. ID: {}", id);
    }


    @Override
    public CampaignResponseDto getCampaignById(Long id) {
        log.trace("getCampaignById method begins. ID: {}", id);

        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("getCampaignById: Campaign with ID {} not found", id);
                    return new CampaignNotFoundException(String.format("Campaign with ID %d not found", id)); });

        log.info("getCampaignById: Campaign retrieved successfully. ID: {}", id);
        log.trace("getCampaignById method ends. ID: {}", id);
        return modelMapper.map(campaign, CampaignResponseDto.class);
    }


    @Override
    public Page<CampaignResponseDto> getAllCampaigns(int pageNumber, int pageSize, boolean hideDeleted) {
        log.trace("getAllCampaigns method begins. PageNumber: {}, PageSize: {}, HideDeleted: {}",
                pageNumber, pageSize, hideDeleted);

        if (pageSize < 1) {
            log.warn("getAllCampaigns: Minimum page size is 1");
            throw new InvalidInputException("Minimum page size is 1"); }
        if (pageNumber < 1) {
            log.warn("getAllCampaigns: Page number must be at least 1");
            throw new InvalidInputException("Page number must be at least 1"); }

        Pageable pageable = PageRequest.of(pageNumber-1, pageSize);

        Page<Campaign> campaignPage = hideDeleted ? campaignRepository.findAllByDeletedFalse(pageable)
                                                    : campaignRepository.findAll(pageable);

        log.info("getAllCampaigns: Retrieved all campaigns successfully. PageNumber: {}, PageSize: {}, HideDeleted: {}, TotalElements: {}",
                pageNumber, pageSize, hideDeleted, campaignPage.getTotalElements());
        log.trace("getAllCampaigns method ends. PageNumber: {}, PageSize: {}, HideDeleted: {}, TotalElements: {}",
                pageNumber, pageSize, hideDeleted, campaignPage.getTotalElements());
        return campaignPage.map(campaign -> modelMapper.map(campaign, CampaignResponseDto.class));
    }



    private LocalDateTime parseDate(String dateStr) {
        log.trace("parseDate method begins. DateStr: {}", dateStr);

        if (dateStr == null || dateStr.isEmpty()) {
            log.trace("parseDate method ends. DateStr is null or empty.");
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd" + " HH:mm:ss");
            LocalDateTime parsedDate = LocalDateTime.parse(dateStr.trim() + " 00:00:00", formatter);

            log.trace("parseDate method ends. ParsedDate: {}", parsedDate);
            return parsedDate;
        } catch (DateTimeParseException e) {
            log.warn("parseDate: Invalid date format for DateStr: {}", dateStr);
            throw new InvalidInputException("Invalid date format: Please use " + "yyyy-MM-dd" + " format.");
        }
    }


    private LocalDateTime parseStartDate(String startDateStr) {
        log.trace("parseStartDate method begins. StartDateStr: {}", startDateStr);

        if (startDateStr == null || startDateStr.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            log.trace("parseStartDate method ends. StartDateStr is null or empty. Using current time: {}", now);
            return now;
        }

        LocalDateTime parsedDate = parseDate(startDateStr);

        log.trace("parseStartDate method ends. ParsedDate: {}", parsedDate);
        return parsedDate;
    }

}

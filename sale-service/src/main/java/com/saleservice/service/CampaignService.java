package com.saleservice.service;

import com.saleservice.dto.CampaignDto;
import com.saleservice.dto.CampaignResponseDto;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;

public interface CampaignService {

    @Transactional
    CampaignDto addCampaign(CampaignDto campaignDto);

    @Transactional
    CampaignResponseDto updateCampaignDates(Long id,
                                            String startDate,
                                            String endDate);

    @Transactional
    void deleteCampaignById(Long id);

    CampaignResponseDto getCampaignById(Long id);

    Page<CampaignResponseDto> getAllCampaigns(int pageNumber,
                                              int pageSize,
                                              boolean hideDeleted);
}

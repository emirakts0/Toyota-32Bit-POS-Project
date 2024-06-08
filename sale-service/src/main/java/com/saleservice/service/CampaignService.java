package com.saleservice.service;


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

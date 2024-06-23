package com.saleservice.service;

import com.saleservice.dto.CampaignDto;
import com.saleservice.dto.CampaignResponseDto;
import com.saleservice.exception.CampaignAlreadyDeletedException;
import com.saleservice.exception.CampaignAlreadyExistException;
import com.saleservice.exception.CampaignNotFoundException;
import com.saleservice.exception.InvalidInputException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;

/**
 * Service interface for managing campaigns.
 * Provides methods for adding, updating, deleting, and retrieving campaigns.
 * @author Emir Aktaş
 */
public interface CampaignService {

    /**
     * Adds a new campaign.
     *
     * @param campaignDto the campaign data transfer object containing campaign details
     * @return the added campaign data transfer object
     * @throws CampaignAlreadyExistException if a campaign with the same name already exists
     * @throws InvalidInputException if the discount value is invalid or dates are incorrect
     */
    @Transactional
    CampaignDto addCampaign(CampaignDto campaignDto);


    /**
     * Updates the start and end dates of an existing campaign.
     *
     * @param id the ID of the campaign to be updated
     * @param startDate the new start date of the campaign
     * @param endDate the new end date of the campaign
     * @return the updated campaign response data transfer object
     * @throws CampaignNotFoundException if the campaign with the given ID is not found
     * @throws CampaignAlreadyDeletedException if the campaign is already deleted
     * @throws InvalidInputException if the dates are invalid
     */
    @Transactional
    CampaignResponseDto updateCampaignDates(Long id,
                                            String startDate,
                                            String endDate);


    /**
     * Deletes a campaign by its ID.
     *
     * @param id the ID of the campaign to be deleted
     * @throws CampaignNotFoundException if the campaign with the given ID is not found
     * @throws CampaignAlreadyDeletedException if the campaign is already deleted
     */
    @Transactional
    void deleteCampaignById(Long id);


    /**
     * Retrieves a campaign by its ID.
     *
     * @param id the ID of the campaign to be retrieved
     * @return the campaign response data transfer object
     * @throws CampaignNotFoundException if the campaign with the given ID is not found
     */
    CampaignResponseDto getCampaignById(Long id);


    /**
     * Retrieves a paginated list of all campaigns.
     *
     * @param pageNumber the number of the page to be retrieved
     * @param pageSize the size of the page to be retrieved
     * @param hideDeleted flag to hide deleted campaigns
     * @return a paginated list of campaign response data transfer objects
     * @throws InvalidInputException if the page size or page number is less than 1
     */
    Page<CampaignResponseDto> getAllCampaigns(int pageNumber,
                                              int pageSize,
                                              boolean hideDeleted);
}

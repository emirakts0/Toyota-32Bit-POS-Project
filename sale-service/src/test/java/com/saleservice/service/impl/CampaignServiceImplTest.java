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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CampaignServiceImplTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CampaignServiceImpl campaignService;


    @Test
    void whenAddCampaignWithValidDataAndFixedAmountDiscount_thenCampaignShouldBeAddedSuccessfully() {
        CampaignDto campaignDto = new CampaignDto();
        campaignDto.setName("New Campaign");
        campaignDto.setDiscountType(DiscountType.FIXED_AMOUNT);
        campaignDto.setDiscountValue(50);
        campaignDto.setStartDate("2024-01-01");
        campaignDto.setEndDate("2024-12-31");

        when(campaignRepository.existsByName(campaignDto.getName())).thenReturn(false);

        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 0, 0);

        Campaign campaign = new Campaign("New Campaign", startDate, endDate, DiscountType.PERCENTAGE, 50);
        when(campaignRepository.save(any(Campaign.class))).thenReturn(campaign);

        CampaignDto result = campaignService.addCampaign(campaignDto);

        assertNotNull(result);
        assertEquals(campaignDto.getName(), result.getName());
        verify(campaignRepository, times(1)).save(any(Campaign.class));
    }

    @Test
    void whenAddCampaignWithNullStartDate_thenUseCurrentTimeAsStartDate() {
        CampaignDto campaignDto = new CampaignDto();
        campaignDto.setName("New Campaign");
        campaignDto.setDiscountType(DiscountType.PERCENTAGE);
        campaignDto.setDiscountValue(50);
        campaignDto.setStartDate(null);
        campaignDto.setEndDate("2024-12-31");

        when(campaignRepository.existsByName(campaignDto.getName())).thenReturn(false);

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.of(2024, 12, 31, 0, 0);
        Campaign campaign = new Campaign("New Campaign", currentTime, endDate, DiscountType.PERCENTAGE, 50);

        when(campaignRepository.save(any(Campaign.class))).thenReturn(campaign);

        CampaignDto result = campaignService.addCampaign(campaignDto);

        assertNotNull(result);
        assertEquals(campaignDto.getName(), result.getName());
        verify(campaignRepository, times(1)).save(any(Campaign.class));
    }

    @Test
    void whenAddCampaignWithNullEndDateAndEmptyStartDate_thenHandleAppropriately() {
        CampaignDto campaignDtoWithNullDates = new CampaignDto();
        campaignDtoWithNullDates.setName("Campaign with Null Dates");
        campaignDtoWithNullDates.setDiscountType(DiscountType.PERCENTAGE);
        campaignDtoWithNullDates.setDiscountValue(50);
        campaignDtoWithNullDates.setStartDate("");
        campaignDtoWithNullDates.setEndDate(null);

        when(campaignRepository.existsByName(campaignDtoWithNullDates.getName())).thenReturn(false);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> campaignService.addCampaign(campaignDtoWithNullDates));

        assertEquals("End date cannot be null or empty.", exception.getMessage());
        verify(campaignRepository, never()).save(any(Campaign.class));
    }

    @Test
    void whenAddCampaignWithExistingName_thenThrowCampaignAlreadyExistException() {
        CampaignDto campaignDto = new CampaignDto();
        campaignDto.setName("Existing Campaign");
        campaignDto.setDiscountType(DiscountType.PERCENTAGE);
        campaignDto.setDiscountValue(50);

        when(campaignRepository.existsByName(campaignDto.getName())).thenReturn(true);

        CampaignAlreadyExistException exception = assertThrows(CampaignAlreadyExistException.class,
                () -> campaignService.addCampaign(campaignDto));

        assertEquals("Campaign with name Existing Campaign already exists", exception.getMessage());
        verify(campaignRepository, never()).save(any(Campaign.class));
    }

    @Test
    void whenAddCampaignWithInvalidPercentageDiscount_thenThrowInvalidInputException() {
        CampaignDto campaignDto = new CampaignDto();
        campaignDto.setName("New Campaign");
        campaignDto.setDiscountType(DiscountType.PERCENTAGE);
        campaignDto.setDiscountValue(150);

        when(campaignRepository.existsByName(campaignDto.getName())).thenReturn(false);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> campaignService.addCampaign(campaignDto));

        assertEquals("Discount value cannot be greater than 100 for percentage discounts.", exception.getMessage());
        verify(campaignRepository, never()).save(any(Campaign.class));
    }

    @Test
    void whenAddCampaignWithStartDateAfterEndDate_thenThrowInvalidInputException() {
        CampaignDto campaignDto = new CampaignDto();
        campaignDto.setName("New Campaign");
        campaignDto.setDiscountType(DiscountType.PERCENTAGE);
        campaignDto.setDiscountValue(50);
        campaignDto.setStartDate("2024-12-31");
        campaignDto.setEndDate("2024-01-01");

        when(campaignRepository.existsByName(campaignDto.getName())).thenReturn(false);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> campaignService.addCampaign(campaignDto));

        assertEquals("Start date cannot be after the end date.", exception.getMessage());
        verify(campaignRepository, never()).save(any(Campaign.class));
    }


    @Test
    void whenUpdateCampaignDatesWithValidDates_thenCampaignDatesShouldBeUpdatedSuccessfully() {
        Long id = 1L;
        String startDate = "2024-01-01";
        String endDate = "2024-12-31";

        Campaign campaign = new Campaign();
        campaign.setStartDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        campaign.setEndDate(LocalDateTime.of(2024, 12, 31, 0, 0));
        when(campaignRepository.findById(id)).thenReturn(Optional.of(campaign));

        CampaignResponseDto campaignResponseDto = new CampaignResponseDto();
        when(modelMapper.map(campaign, CampaignResponseDto.class)).thenReturn(campaignResponseDto);

        CampaignResponseDto result = campaignService.updateCampaignDates(id, startDate, endDate);

        assertNotNull(result);
        assertEquals(campaignResponseDto, result);
        verify(campaignRepository, times(1)).save(campaign);
    }

    @Test
    void whenUpdateCampaignDatesWithNonExistentCampaign_thenThrowCampaignNotFoundException() {
        Long id = 1L;
        String startDate = "2024-01-01";
        String endDate = "2024-12-31";

        when(campaignRepository.findById(id)).thenReturn(Optional.empty());

        CampaignNotFoundException exception = assertThrows(CampaignNotFoundException.class,
                () -> campaignService.updateCampaignDates(id, startDate, endDate));

        assertEquals("Campaign with ID 1 not found", exception.getMessage());
    }

    @Test
    void whenUpdateCampaignDatesWithDeletedCampaign_thenThrowCampaignAlreadyDeletedException() {
        Long id = 1L;
        String startDate = "2024-01-01";
        String endDate = "2024-12-31";

        Campaign campaign = new Campaign();
        campaign.setDeleted(true);
        when(campaignRepository.findById(id)).thenReturn(Optional.of(campaign));

        CampaignAlreadyDeletedException exception = assertThrows(CampaignAlreadyDeletedException.class,
                () -> campaignService.updateCampaignDates(id, startDate, endDate));

        assertEquals("Cannot update a deleted campaign.", exception.getMessage());
    }

    @Test
    void whenUpdateCampaignDatesWithInvalidStartDateFormat_thenThrowInvalidInputException() {
        Long id = 1L;
        String startDate = "invalid-date";
        String endDate = "2024-12-31";

        Campaign campaign = new Campaign();
        when(campaignRepository.findById(id)).thenReturn(Optional.of(campaign));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> campaignService.updateCampaignDates(id, startDate, endDate));

        assertEquals("Invalid date format: Please use yyyy-MM-dd format.", exception.getMessage());
    }

    @Test
    void whenUpdateCampaignDatesWithInvalidEndDateFormat_thenThrowInvalidInputException() {
        Long id = 1L;
        String startDate = "2024-01-01";
        String endDate = "invalid-date";

        Campaign campaign = new Campaign();
        when(campaignRepository.findById(id)).thenReturn(Optional.of(campaign));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> campaignService.updateCampaignDates(id, startDate, endDate));

        assertEquals("Invalid date format: Please use yyyy-MM-dd format.", exception.getMessage());
    }

    @Test
    void whenUpdateCampaignDatesWithEndDateBeforeStartDate_thenThrowInvalidInputException() {
        Long id = 1L;
        String startDate = "2024-12-31";
        String endDate = "2024-01-01";

        Campaign campaign = new Campaign();
        campaign.setStartDate(LocalDateTime.of(2024, 12, 31, 0, 0));
        campaign.setEndDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        when(campaignRepository.findById(id)).thenReturn(Optional.of(campaign));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> campaignService.updateCampaignDates(id, startDate, endDate));

        assertEquals("End date cannot be before the start date.", exception.getMessage());
    }

    @Test
    void whenUpdateCampaignDatesWithNullDates_thenCampaignDatesShouldBeNotUpdated() {
        Long id = 1L;
        String startDate = "2024-01-01";
        String endDate = "2024-12-31";

        Campaign campaign = new Campaign();
        campaign.setStartDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        campaign.setEndDate(LocalDateTime.of(2024, 12, 31, 0, 0));
        when(campaignRepository.findById(id)).thenReturn(Optional.of(campaign));

        CampaignResponseDto campaignResponseDto = new CampaignResponseDto();
        when(modelMapper.map(campaign, CampaignResponseDto.class)).thenReturn(campaignResponseDto);

        CampaignResponseDto result = campaignService.updateCampaignDates(id, startDate, endDate);

        assertNotNull(result);
        assertEquals(campaignResponseDto, result);
        verify(campaignRepository, times(1)).save(campaign);
    }

    @Test
    void whenUpdateCampaignDatesWithNullDates_thenCampaignDatesShouldNotBeUpdated() {
        Long id = 1L;

        Campaign campaign = new Campaign();
        LocalDateTime originalStartDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime originalEndDate = LocalDateTime.of(2024, 12, 31, 0, 0);
        campaign.setStartDate(originalStartDate);
        campaign.setEndDate(originalEndDate);

        when(campaignRepository.findById(id)).thenReturn(Optional.of(campaign));

        CampaignResponseDto campaignResponseDto = new CampaignResponseDto();
        when(modelMapper.map(campaign, CampaignResponseDto.class)).thenReturn(campaignResponseDto);

        CampaignResponseDto result = campaignService.updateCampaignDates(id, null, null);

        assertNotNull(result);
        assertEquals(campaignResponseDto, result);
        assertEquals(originalStartDate, campaign.getStartDate());
        assertEquals(originalEndDate, campaign.getEndDate());

        verify(campaignRepository, times(1)).save(campaign);
    }

    @Test
    void whenUpdateCampaignDatesWithEmptyDates_thenCampaignDatesShouldNotBeUpdated() {
        Long id = 1L;
        String startDate = "";
        String endDate = "";

        Campaign campaign = new Campaign();
        LocalDateTime originalStartDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime originalEndDate = LocalDateTime.of(2024, 12, 31, 0, 0);
        campaign.setStartDate(originalStartDate);
        campaign.setEndDate(originalEndDate);

        when(campaignRepository.findById(id)).thenReturn(Optional.of(campaign));

        CampaignResponseDto campaignResponseDto = new CampaignResponseDto();
        when(modelMapper.map(campaign, CampaignResponseDto.class)).thenReturn(campaignResponseDto);

        CampaignResponseDto result = campaignService.updateCampaignDates(id, startDate, endDate);

        assertNotNull(result);
        assertEquals(campaignResponseDto, result);
        assertEquals(originalStartDate, campaign.getStartDate());
        assertEquals(originalEndDate, campaign.getEndDate());

        verify(campaignRepository, times(1)).save(campaign);
    }


    @Test
    void whenDeleteCampaignByIdWithNonExistentCampaign_thenThrowCampaignNotFoundException() {
        Long id = 1L;

        when(campaignRepository.findById(id)).thenReturn(Optional.empty());

        CampaignNotFoundException exception = assertThrows(CampaignNotFoundException.class,
                () -> campaignService.deleteCampaignById(id));

        assertEquals("Campaign with ID 1 not found", exception.getMessage());
        verify(campaignRepository, never()).save(any(Campaign.class));
    }

    @Test
    void whenDeleteCampaignByIdWithAlreadyDeletedCampaign_thenThrowCampaignAlreadyDeletedException() {
        Long id = 1L;

        Campaign campaign = new Campaign();
        campaign.setDeleted(true);
        when(campaignRepository.findById(id)).thenReturn(Optional.of(campaign));

        CampaignAlreadyDeletedException exception = assertThrows(CampaignAlreadyDeletedException.class,
                () -> campaignService.deleteCampaignById(id));

        assertEquals("Campaign with ID 1 already deleted", exception.getMessage());
        verify(campaignRepository, never()).save(any(Campaign.class));
    }

    @Test
    void whenDeleteCampaignByIdWithValidId_thenCampaignShouldBeDeletedSuccessfully() {
        Long id = 1L;

        Campaign campaign = new Campaign();
        campaign.setDeleted(false);
        when(campaignRepository.findById(id)).thenReturn(Optional.of(campaign));

        campaignService.deleteCampaignById(id);

        assertTrue(campaign.isDeleted());
        verify(campaignRepository, times(1)).save(campaign);
    }


    @Test
    void whenGetCampaignByIdWithNonExistentCampaign_thenThrowCampaignNotFoundException() {
        Long id = 1L;

        when(campaignRepository.findById(id)).thenReturn(Optional.empty());

        CampaignNotFoundException exception = assertThrows(CampaignNotFoundException.class,
                () -> campaignService.getCampaignById(id));

        assertEquals("Campaign with ID 1 not found", exception.getMessage());
    }

    @Test
    void whenGetCampaignByIdWithValidId_thenCampaignShouldBeRetrievedSuccessfully() {
        Long id = 1L;

        Campaign campaign = new Campaign();
        campaign.setId(id);

        CampaignResponseDto campaignResponseDto = new CampaignResponseDto();
        campaignResponseDto.setId(id);

        when(campaignRepository.findById(id)).thenReturn(Optional.of(campaign));
        when(modelMapper.map(campaign, CampaignResponseDto.class)).thenReturn(campaignResponseDto);

        CampaignResponseDto result = campaignService.getCampaignById(id);

        assertNotNull(result);
        assertEquals(campaignResponseDto, result);
        verify(campaignRepository, times(1)).findById(id);
        verify(modelMapper, times(1)).map(campaign, CampaignResponseDto.class);
    }


    @Test
    void whenGetAllCampaignsWithPageSizeLessThanOne_thenThrowInvalidInputException() {
        int pageNumber = 1;
        int pageSize = 0;
        boolean hideDeleted = true;

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> campaignService.getAllCampaigns(pageNumber, pageSize, hideDeleted));

        assertEquals("Minimum page size is 1", exception.getMessage());
    }

    @Test
    void whenGetAllCampaignsWithPageNumberLessThanOne_thenThrowInvalidInputException() {
        int pageNumber = 0;
        int pageSize = 10;
        boolean hideDeleted = true;

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> campaignService.getAllCampaigns(pageNumber, pageSize, hideDeleted));

        assertEquals("Page number must be at least 1", exception.getMessage());
    }

    @Test
    void whenGetAllCampaignsHideDeleted_thenReturnNonDeletedCampaigns() {
        int pageNumber = 1;
        int pageSize = 10;
        boolean hideDeleted = true;

        Campaign campaign = new Campaign();
        campaign.setDeleted(false);
        List<Campaign> campaigns = Collections.singletonList(campaign);
        Page<Campaign> page = new PageImpl<>(campaigns);

        Pageable pageable = PageRequest.of(0, pageSize);

        when(campaignRepository.findAllByDeletedFalse(pageable)).thenReturn(page);
        when(modelMapper.map(campaign, CampaignResponseDto.class)).thenReturn(new CampaignResponseDto());

        Page<CampaignResponseDto> result = campaignService.getAllCampaigns(pageNumber, pageSize, hideDeleted);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(campaignRepository, times(1)).findAllByDeletedFalse(pageable);
    }

    @Test
    void whenGetAllCampaignsIncludeDeleted_thenReturnAllCampaigns() {
        int pageNumber = 1;
        int pageSize = 10;
        boolean hideDeleted = false;

        Campaign campaign = new Campaign();
        List<Campaign> campaigns = Collections.singletonList(campaign);
        Page<Campaign> page = new PageImpl<>(campaigns);

        Pageable pageable = PageRequest.of(0, pageSize);

        when(campaignRepository.findAll(pageable)).thenReturn(page);
        when(modelMapper.map(campaign, CampaignResponseDto.class)).thenReturn(new CampaignResponseDto());

        Page<CampaignResponseDto> result = campaignService.getAllCampaigns(pageNumber, pageSize, hideDeleted);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(campaignRepository, times(1)).findAll(pageable);
    }
}
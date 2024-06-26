package com.saleservice.service.impl;

import com.saleservice.client.ProductServiceClient;
import com.saleservice.dto.BagDto;
import com.saleservice.dto.CampaignResponseDto;
import com.saleservice.dto.ProductDto;
import com.saleservice.exception.BagNotFoundException;
import com.saleservice.exception.CampaignNotFoundException;
import com.saleservice.exception.InvalidCampaignException;
import com.saleservice.exception.InvalidInputException;
import com.saleservice.model.Bag;
import com.saleservice.model.BagItem;
import com.saleservice.model.DiscountType;
import com.saleservice.repository.BagRepository;
import com.saleservice.service.CampaignService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BagServiceImplTest {

    @Mock
    private BagRepository bagRepository;

    @Mock
    private ProductServiceClient productService;

    @Mock
    private CampaignService campaignService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private BagServiceImpl bagService;

    @Test
    void whenAddProductToBagWithNullBarcode_thenThrowInvalidInputException() {
        Long bagId = 1L;
        int quantity = 1;

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> bagService.addProductToBag(bagId, null, quantity));

        assertEquals("barcode is empty", exception.getMessage());
    }

    @Test
    void whenAddProductToBagWithEmptyBarcode_thenThrowInvalidInputException() {
        Long bagId = 1L;
        String barcode = " ";
        int quantity = 1;

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> bagService.addProductToBag(bagId, barcode, quantity));

        assertEquals("barcode is empty", exception.getMessage());
    }

    @Test
    void whenAddProductToBagWithExistingItemExceedingStock_thenThrowInvalidInputException() {
        Long bagId = 1L;
        String barcode = "123456";
        int quantity = 10;

        ProductDto productDto = new ProductDto();
        productDto.setStock(5);
        when(productService.getProduct(barcode)).thenReturn(ResponseEntity.ok(productDto));

        Bag bag = new Bag();
        BagItem existingItem = new BagItem(barcode, 4, BigDecimal.TEN, "Product Name");
        bag.getItems().add(existingItem);

        when(bagRepository.findById(bagId)).thenReturn(Optional.of(bag));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> bagService.addProductToBag(bagId, barcode, quantity));

        assertEquals("The quantity of products in the bag cannot be more than stock", exception.getMessage());
    }

    @Test
    void whenAddProductToBagWithNewItemExceedingStock_thenThrowInvalidInputException() {
        Long bagId = 1L;
        String barcode = "123456";
        int quantity = 10;

        ProductDto productDto = new ProductDto();
        productDto.setStock(5);
        when(productService.getProduct(barcode)).thenReturn(ResponseEntity.ok(productDto));

        Bag bag = new Bag();
        when(bagRepository.findById(bagId)).thenReturn(Optional.of(bag));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> bagService.addProductToBag(bagId, barcode, quantity));

        assertEquals("The quantity of products in the bag cannot be more than stock", exception.getMessage());
    }

    @Test
    void whenAddProductToBagWithNewItemNotExceedingStock_thenProductShouldBeAddedSuccessfully() {
        Long bagId = 1L;
        String barcode = "123456";
        int quantity = 10;

        ProductDto productDto = new ProductDto();
        productDto.setStock(10);
        productDto.setPrice(BigDecimal.TEN);
        productDto.setName("Product Name");
        when(productService.getProduct(barcode)).thenReturn(ResponseEntity.ok(productDto));

        Bag bag = new Bag();
        when(bagRepository.findById(bagId)).thenReturn(Optional.of(bag));

        BagDto bagDto = new BagDto();
        when(modelMapper.map(bag, BagDto.class)).thenReturn(bagDto);

        BagDto result = bagService.addProductToBag(bagId, barcode, quantity);

        assertNotNull(result);
        verify(bagRepository, times(1)).save(bag);
    }

    @Test
    void whenAddProductToBagWithValidNewItem_thenProductShouldBeAddedSuccessfully() {
        Long bagId = 1L;
        String barcode = "123456";
        int quantity = 2;

        ProductDto productDto = new ProductDto();
        productDto.setStock(5);
        productDto.setPrice(BigDecimal.TEN);
        productDto.setName("Product Name");
        when(productService.getProduct(barcode)).thenReturn(ResponseEntity.ok(productDto));

        Bag bag = new Bag();
        when(bagRepository.findById(bagId)).thenReturn(Optional.of(bag));

        BagItem newItem = new BagItem(barcode, quantity, BigDecimal.TEN, "Product Name");
        bag.getItems().add(newItem);

        BagDto bagDto = new BagDto();
        when(modelMapper.map(bag, BagDto.class)).thenReturn(bagDto);

        BagDto result = bagService.addProductToBag(bagId, barcode, quantity);

        assertNotNull(result);
        verify(bagRepository, times(1)).save(bag);
    }

    @Test
    void whenAddProductToBagWithValidExistingItem_thenProductQuantityShouldBeUpdatedSuccessfully() {
        Long bagId = 1L;
        String barcode = "123456";
        int quantity = 2;

        ProductDto productDto = new ProductDto();
        productDto.setStock(5);
        productDto.setPrice(BigDecimal.TEN);
        productDto.setName("Product Name");
        when(productService.getProduct(barcode)).thenReturn(ResponseEntity.ok(productDto));

        Bag bag = new Bag();
        BagItem existingItem = new BagItem(barcode, 1, BigDecimal.TEN, "Product Name");
        bag.getItems().add(existingItem);
        when(bagRepository.findById(bagId)).thenReturn(Optional.of(bag));

        BagDto bagDto = new BagDto();
        when(modelMapper.map(bag, BagDto.class)).thenReturn(bagDto);

        BagDto result = bagService.addProductToBag(bagId, barcode, quantity);

        assertNotNull(result);
        assertEquals(3, existingItem.getQuantity());
        verify(bagRepository, times(1)).save(bag);
    }

    @Test
    void whenAddProductToBagWithNullBagId_thenNewBagShouldBeCreated() {
        String barcode = "123456";
        int quantity = 2;

        ProductDto productDto = new ProductDto();
        productDto.setStock(5);
        productDto.setPrice(BigDecimal.TEN);
        productDto.setName("Product Name");
        when(productService.getProduct(barcode)).thenReturn(ResponseEntity.ok(productDto));

        Bag bag = new Bag();
        BagItem newBagItem = new BagItem(barcode, quantity, productDto.getPrice(), productDto.getName());
        bag.getItems().add(newBagItem);

        BagDto bagDto = new BagDto();
        when(modelMapper.map(any(Bag.class), eq(BagDto.class))).thenReturn(bagDto);

        BagDto result = bagService.addProductToBag(null, barcode, quantity);

        assertNotNull(result);
        verify(bagRepository, times(1)).save(any(Bag.class));
        assertEquals(1, bag.getItems().size());
        assertEquals(newBagItem, bag.getItems().get(0));
    }


    @Test
    void whenDeleteBagByIdWithNullBagId_thenThrowInvalidInputException() {
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> bagService.deleteBagById(null));

        assertEquals("bagId is empty", exception.getMessage());
        verify(bagRepository, never()).delete(any(Bag.class));
    }

    @Test
    void whenDeleteBagByIdWithNonExistentBag_thenThrowBagNotFoundException() {
        Long bagId = 1L;

        when(bagRepository.findById(bagId)).thenReturn(Optional.empty());

        BagNotFoundException exception = assertThrows(BagNotFoundException.class,
                () -> bagService.deleteBagById(bagId));

        assertEquals("Bag not found with id: " + bagId, exception.getMessage());
        verify(bagRepository, never()).delete(any(Bag.class));
    }

    @Test
    void whenDeleteBagByIdWithValidBagId_thenBagShouldBeDeletedSuccessfully() {
        Long bagId = 1L;

        Bag bag = new Bag();
        when(bagRepository.findById(bagId)).thenReturn(Optional.of(bag));

        bagService.deleteBagById(bagId);

        verify(bagRepository, times(1)).delete(bag);
    }


    @Test
    void whenRemoveProductFromBagWithNullBagId_thenThrowInvalidInputException() {
        String barcode = "123456";
        int quantity = 1;

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> bagService.removeProductFromBag(null, barcode, quantity));

        assertEquals("bagId is empty", exception.getMessage());
    }

    @Test
    void whenRemoveProductFromBagWithNullBarcode_thenThrowInvalidInputException() {
        Long bagId = 1L;
        int quantity = 1;

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> bagService.removeProductFromBag(bagId, null, quantity));

        assertEquals("Barcode is empty", exception.getMessage());
    }

    @Test
    void whenRemoveProductFromBagWithEmptyBarcode_thenThrowInvalidInputException() {
        Long bagId = 1L;
        String barcode = " ";
        int quantity = 1;

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> bagService.removeProductFromBag(bagId, barcode, quantity));

        assertEquals("Barcode is empty", exception.getMessage());
    }

    @Test
    void whenRemoveProductFromBagWithNonExistentBag_thenThrowBagNotFoundException() {
        Long bagId = 1L;
        String barcode = "123456";
        int quantity = 1;

        when(bagRepository.findById(bagId)).thenReturn(Optional.empty());

        BagNotFoundException exception = assertThrows(BagNotFoundException.class,
                () -> bagService.removeProductFromBag(bagId, barcode, quantity));

        assertEquals("Bag not found with id: " + bagId, exception.getMessage());
    }

    @Test
    void whenRemoveProductFromBagWithNonExistentItem_thenThrowBagNotFoundException() {
        Long bagId = 1L;
        String barcode = "123456";
        int quantity = 1;

        Bag bag = new Bag();
        when(bagRepository.findById(bagId)).thenReturn(Optional.of(bag));

        BagNotFoundException exception = assertThrows(BagNotFoundException.class,
                () -> bagService.removeProductFromBag(bagId, barcode, quantity));

        assertEquals("Item not found in bag with barcode: " + barcode, exception.getMessage());
    }

    @Test
    void whenRemoveProductFromBagWithQuantityExceedingItemQuantity_thenThrowInvalidInputException() {
        Long bagId = 1L;
        String barcode = "123456";
        int quantity = 5;

        Bag bag = new Bag();
        BagItem item = new BagItem(barcode, 2, BigDecimal.TEN, "Product Name");
        bag.setItems(Collections.singletonList(item));
        when(bagRepository.findById(bagId)).thenReturn(Optional.of(bag));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> bagService.removeProductFromBag(bagId, barcode, quantity));

        assertEquals("Quantity to remove exceeds the quantity in the bag", exception.getMessage());
    }

    @Test
    void whenRemoveProductFromBagWithQuantityEqualToItemQuantity_thenItemShouldBeRemovedAndCampaignUpdated() {
        Long bagId = 1L;
        String barcode = "123456";
        int quantity = 2;

        Bag bag = new Bag();
        bag.setCampaignId(1L);
        bag.setCampaignName("Test Campaign");
        bag.setDiscountValue(10);
        bag.setDiscountType(DiscountType.PERCENTAGE);

        BagItem item = new BagItem(barcode, 2, BigDecimal.TEN, "Product Name");
        bag.setItems(new ArrayList<>(Collections.singletonList(item)));
        when(bagRepository.findById(bagId)).thenReturn(Optional.of(bag));

        BagDto bagDto = new BagDto();
        when(modelMapper.map(bag, BagDto.class)).thenReturn(bagDto);

        BagDto result = bagService.removeProductFromBag(bagId, barcode, quantity);

        assertNotNull(result);
        assertTrue(bag.getItems().isEmpty());
        verify(bagRepository, times(1)).save(bag);

        BigDecimal expectedDiscountedPrice = BigDecimal.ZERO;
        assertEquals(0, bag.getDiscountedPrice().compareTo(expectedDiscountedPrice));    }

    @Test
    void whenRemoveProductFromBagWithQuantityLessThanItemQuantity_thenItemQuantityShouldBeUpdated() {
        Long bagId = 1L;
        String barcode = "123456";
        int quantity = 1;

        Bag bag = new Bag();
        BagItem item = new BagItem(barcode, 2, BigDecimal.TEN, "Product Name");
        bag.setItems(Collections.singletonList(item));
        when(bagRepository.findById(bagId)).thenReturn(Optional.of(bag));

        BagDto bagDto = new BagDto();
        when(modelMapper.map(bag, BagDto.class)).thenReturn(bagDto);

        BagDto result = bagService.removeProductFromBag(bagId, barcode, quantity);

        assertNotNull(result);
        assertEquals(1, item.getQuantity());
        verify(bagRepository, times(1)).save(bag);
    }


    @Test
    void whenRemoveAllProductsFromBagWithNullBagId_thenThrowInvalidInputException() {
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> bagService.removeAllProductsFromBag(null));

        assertEquals("bagId is empty", exception.getMessage());
    }

    @Test
    void whenRemoveAllProductsFromBagWithNonExistentBag_thenThrowBagNotFoundException() {
        Long bagId = 1L;

        when(bagRepository.findById(bagId)).thenReturn(Optional.empty());

        BagNotFoundException exception = assertThrows(BagNotFoundException.class,
                () -> bagService.removeAllProductsFromBag(bagId));

        assertEquals("Bag not found with id: " + bagId, exception.getMessage());
    }

    @Test
    void whenRemoveAllProductsFromBagWithValidBagId_thenAllProductsShouldBeRemovedSuccessfully() {
        Long bagId = 1L;

        Bag bag = new Bag();
        BagItem item = new BagItem("123456", 2, BigDecimal.TEN, "Product Name");
        bag.setItems(new ArrayList<>(Collections.singletonList(item)));
        when(bagRepository.findById(bagId)).thenReturn(Optional.of(bag));

        BagDto bagDto = new BagDto();
        when(modelMapper.map(bag, BagDto.class)).thenReturn(bagDto);

        BagDto result = bagService.removeAllProductsFromBag(bagId);

        assertNotNull(result);
        assertTrue(bag.getItems().isEmpty());
        verify(bagRepository, times(1)).save(bag);
    }


    @Test
    void whenGetBagByIdWithNullBagId_thenThrowInvalidInputException() {
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> bagService.getBagById(null));

        assertEquals("bagId is empty", exception.getMessage());
    }

    @Test
    void whenGetBagByIdWithNonExistentBag_thenThrowBagNotFoundException() {
        Long bagId = 1L;

        when(bagRepository.findById(bagId)).thenReturn(Optional.empty());

        BagNotFoundException exception = assertThrows(BagNotFoundException.class,
                () -> bagService.getBagById(bagId));

        assertEquals("Bag not found with id: " + bagId, exception.getMessage());
    }

    @Test
    void whenGetBagByIdWithValidBagId_thenBagShouldBeReturnedSuccessfully() {
        Long bagId = 1L;

        Bag bag = new Bag();
        BagDto bagDto = new BagDto();
        when(bagRepository.findById(bagId)).thenReturn(Optional.of(bag));
        when(modelMapper.map(bag, BagDto.class)).thenReturn(bagDto);

        BagDto result = bagService.getBagById(bagId);

        assertNotNull(result);
        assertEquals(bagDto, result);
        verify(bagRepository, times(1)).findById(bagId);
    }


    @Test
    void whenGetAllBagsWithPageSizeLessThanOne_thenThrowInvalidInputException() {
        int pageNumber = 1;
        int pageSize = 0;

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> bagService.getAllBags(pageNumber, pageSize));

        assertEquals("Minimum page size is 1", exception.getMessage());
    }

    @Test
    void whenGetAllBagsWithPageNumberLessThanOne_thenThrowInvalidInputException() {
        int pageNumber = 0;
        int pageSize = 10;

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> bagService.getAllBags(pageNumber, pageSize));

        assertEquals("Page number must be at least 1", exception.getMessage());
    }

    @Test
    void whenGetAllBagsWithValidPageNumberAndPageSize_thenReturnBagsPageSuccessfully() {
        int pageNumber = 1;
        int pageSize = 10;

        List<Bag> bags = new ArrayList<>();
        Bag bag1 = new Bag();
        Bag bag2 = new Bag();
        bags.add(bag1);
        bags.add(bag2);

        BagDto bagDto1 = new BagDto();
        BagDto bagDto2 = new BagDto();

        when(bagRepository.findAll()).thenReturn(bags);
        when(modelMapper.map(bag1, BagDto.class)).thenReturn(bagDto1);
        when(modelMapper.map(bag2, BagDto.class)).thenReturn(bagDto2);

        Page<BagDto> result = bagService.getAllBags(pageNumber, pageSize);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getContent().size());
        verify(bagRepository, times(1)).findAll();
    }


    @Test
    void whenApplyCampaignToBagWithNonExistentBag_thenThrowBagNotFoundException() {
        Long bagId = 1L;
        Long campaignId = 1L;

        when(bagRepository.findById(bagId)).thenReturn(Optional.empty());

        BagNotFoundException exception = assertThrows(BagNotFoundException.class,
                () -> bagService.applyCampaignToBag(bagId, campaignId));

        assertEquals("Bag not found with id: " + bagId, exception.getMessage());
    }

    @Test
    void whenApplyCampaignToBagWithNonExistentCampaign_thenThrowCampaignNotFoundException() {
        Long bagId = 1L;
        Long campaignId = 1L;

        Bag bag = new Bag();
        when(bagRepository.findById(bagId)).thenReturn(Optional.of(bag));
        when(campaignService.getCampaignById(campaignId)).thenReturn(null);

        CampaignNotFoundException exception = assertThrows(CampaignNotFoundException.class,
                () -> bagService.applyCampaignToBag(bagId, campaignId));

        assertEquals("Campaign not found with id: " + campaignId, exception.getMessage());
    }

    @Test
    void whenApplyCampaignToBagWithDeletedCampaign_thenThrowInvalidCampaignException() {
        Long bagId = 1L;
        Long campaignId = 1L;

        Bag bag = new Bag();
        CampaignResponseDto campaign = new CampaignResponseDto();
        campaign.setDeleted(true);
        when(bagRepository.findById(bagId)).thenReturn(Optional.of(bag));
        when(campaignService.getCampaignById(campaignId)).thenReturn(campaign);

        InvalidCampaignException exception = assertThrows(InvalidCampaignException.class,
                () -> bagService.applyCampaignToBag(bagId, campaignId));

        assertEquals("Campaign is not active or has been deleted.", exception.getMessage());
    }

    @Test
    void whenApplyCampaignToBagWithInactiveCampaignBeforeStart_thenThrowInvalidCampaignException() {
        Long bagId = 1L;
        Long campaignId = 1L;

        Bag bag = new Bag();
        CampaignResponseDto campaign = new CampaignResponseDto();
        campaign.setDeleted(false);
        campaign.setStartDate(LocalDateTime.now().plusDays(1));
        campaign.setEndDate(LocalDateTime.now().plusDays(10));
        when(bagRepository.findById(bagId)).thenReturn(Optional.of(bag));
        when(campaignService.getCampaignById(campaignId)).thenReturn(campaign);

        InvalidCampaignException exception = assertThrows(InvalidCampaignException.class,
                () -> bagService.applyCampaignToBag(bagId, campaignId));

        assertEquals("Campaign is not active or has been deleted.", exception.getMessage());
    }

    @Test
    void whenApplyCampaignToBagWithInactiveCampaignAfterEnd_thenThrowInvalidCampaignException() {
        Long bagId = 1L;
        Long campaignId = 1L;

        Bag bag = new Bag();
        CampaignResponseDto campaign = new CampaignResponseDto();
        campaign.setDeleted(false);
        campaign.setStartDate(LocalDateTime.now().minusDays(10));
        campaign.setEndDate(LocalDateTime.now().minusDays(1));
        when(bagRepository.findById(bagId)).thenReturn(Optional.of(bag));
        when(campaignService.getCampaignById(campaignId)).thenReturn(campaign);

        InvalidCampaignException exception = assertThrows(InvalidCampaignException.class,
                () -> bagService.applyCampaignToBag(bagId, campaignId));

        assertEquals("Campaign is not active or has been deleted.", exception.getMessage());
    }

    @Test
    void whenApplyCampaignToBagWithValidDataAndFixedAmountDiscount_thenApplyCampaignSuccessfully() {
        Long bagId = 1L;
        Long campaignId = 1L;

        Bag bag = new Bag();
        BagDto bagDto = new BagDto();
        CampaignResponseDto campaign = new CampaignResponseDto();
        campaign.setDeleted(false);
        campaign.setStartDate(LocalDateTime.now().minusDays(1));
        campaign.setEndDate(LocalDateTime.now().plusDays(1));
        campaign.setDiscountType(DiscountType.FIXED_AMOUNT);
        campaign.setDiscountValue(10);
        campaign.setName("Test Campaign");

        when(bagRepository.findById(bagId)).thenReturn(Optional.of(bag));
        when(campaignService.getCampaignById(campaignId)).thenReturn(campaign);
        when(modelMapper.map(bag, BagDto.class)).thenReturn(bagDto);

        BagDto result = bagService.applyCampaignToBag(bagId, campaignId);

        assertNotNull(result);
        assertEquals(campaignId, bag.getCampaignId());
        assertEquals("Test Campaign", bag.getCampaignName());
        verify(bagRepository, times(1)).save(bag);
    }

    @Test
    void whenApplyCampaignToBagWithValidDataAndPercentageDiscount_thenApplyCampaignSuccessfully() {
        Long bagId = 1L;
        Long campaignId = 1L;

        Bag bag = new Bag();
        BagDto bagDto = new BagDto();
        CampaignResponseDto campaign = new CampaignResponseDto();
        campaign.setDeleted(false);
        campaign.setStartDate(LocalDateTime.now().minusDays(1));
        campaign.setEndDate(LocalDateTime.now().plusDays(1));
        campaign.setDiscountType(DiscountType.PERCENTAGE);
        campaign.setDiscountValue(10);
        campaign.setName("Test Campaign");

        when(bagRepository.findById(bagId)).thenReturn(Optional.of(bag));
        when(campaignService.getCampaignById(campaignId)).thenReturn(campaign);
        when(modelMapper.map(bag, BagDto.class)).thenReturn(bagDto);

        BagDto result = bagService.applyCampaignToBag(bagId, campaignId);

        assertNotNull(result);
        assertEquals(campaignId, bag.getCampaignId());
        assertEquals("Test Campaign", bag.getCampaignName());
        verify(bagRepository, times(1)).save(bag);
    }


    @Test
    void whenRemoveCampaignFromBagWithNonExistentBag_thenThrowBagNotFoundException() {
        Long bagId = 1L;

        when(bagRepository.findById(bagId)).thenReturn(Optional.empty());

        BagNotFoundException exception = assertThrows(BagNotFoundException.class,
                () -> bagService.removeCampaignFromBag(bagId));

        assertEquals("Bag not found with id: " + bagId, exception.getMessage());
    }

    @Test
    void whenRemoveCampaignFromBagWithNoCampaignInBag_thenThrowCampaignNotFoundException() {
        Long bagId = 1L;

        Bag bag = new Bag();
        when(bagRepository.findById(bagId)).thenReturn(Optional.of(bag));

        CampaignNotFoundException exception = assertThrows(CampaignNotFoundException.class,
                () -> bagService.removeCampaignFromBag(bagId));

        assertEquals("Campaign not found in the bag with id: " + bagId, exception.getMessage());
    }

    @Test
    void whenRemoveCampaignFromBagWithValidBagId_thenRemoveCampaignSuccessfully() {
        Long bagId = 1L;

        Bag bag = new Bag();
        bag.setCampaignId(1L);
        bag.setCampaignName("Test Campaign");
        bag.setDiscountValue(10);
        bag.setDiscountType(DiscountType.PERCENTAGE);

        BagDto bagDto = new BagDto();
        when(bagRepository.findById(bagId)).thenReturn(Optional.of(bag));
        when(modelMapper.map(bag, BagDto.class)).thenReturn(bagDto);

        BagDto result = bagService.removeCampaignFromBag(bagId);

        assertNotNull(result);
        assertNull(bag.getCampaignId());
        assertNull(bag.getCampaignName());
        assertEquals(0, bag.getDiscountValue());
        assertNull(bag.getDiscountType());
        verify(bagRepository, times(1)).save(bag);
    }
}
package com.saleservice.service;

import com.saleservice.dto.BagDto;
import com.saleservice.exception.BagNotFoundException;
import com.saleservice.exception.CampaignNotFoundException;
import com.saleservice.exception.InvalidCampaignException;
import com.saleservice.exception.InvalidInputException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;

/**
 * Service interface for managing shopping bags.
 * Provides methods for adding, removing, and managing products in bags, as well as applying and removing campaigns.
 * @author Emir Aktaş
 */
public interface BagService {

    /**
     * Adds a product to a bag.
     *
     * @param bagId    the ID of the bag
     * @param barcode  the barcode of the product to be added
     * @param quantity the quantity of the product to be added
     * @return the updated bag data transfer object
     * @throws InvalidInputException if the barcode is empty or if the quantity exceeds the stock
     */
    @Transactional
    BagDto addProductToBag(Long bagId, String barcode, int quantity);


    /**
     * Removes a product from a bag.
     *
     * @param bagId    the ID of the bag
     * @param barcode  the barcode of the product to be removed
     * @param quantity the quantity of the product to be removed
     * @return the updated bag data transfer object
     * @throws InvalidInputException if the bagId or barcode is empty, or if the quantity to remove exceeds the quantity in the bag
     * @throws BagNotFoundException if the bag or product is not found
     */
    @Transactional
    BagDto removeProductFromBag(Long bagId, String barcode, int quantity);


    /**
     * Removes all products from a bag.
     *
     * @param bagId the ID of the bag
     * @return the updated bag data transfer object
     * @throws InvalidInputException if the bagId is empty
     * @throws BagNotFoundException if the bag is not found
     */
    @Transactional
    BagDto removeAllProductsFromBag(Long bagId);


    /**
     * Deletes a bag by its ID.
     *
     * @param bagId the ID of the bag to be deleted
     * @throws InvalidInputException if the bagId is empty
     * @throws BagNotFoundException if the bag is not found
     */
    @Transactional
    void deleteBagById(Long bagId);


    /**
     * Retrieves a bag by its ID.
     *
     * @param bagId the ID of the bag to be retrieved
     * @return the bag data transfer object
     * @throws InvalidInputException if the bagId is empty
     * @throws BagNotFoundException if the bag is not found
     */
    BagDto getBagById(Long bagId);


    /**
     * Retrieves a paginated list of all bags.
     *
     * @param pageNumber the number of the page to be retrieved
     * @param pageSize   the size of the page to be retrieved
     * @return a paginated list of bag data transfer objects
     * @throws InvalidInputException if the page size or page number is less than 1
     */
    Page<BagDto> getAllBags(int pageNumber, int pageSize);


    /**
     * Applies a campaign to a bag.
     *
     * @param bagId     the ID of the bag
     * @param campaignId the ID of the campaign to be applied
     * @return the updated bag data transfer object
     * @throws BagNotFoundException if the bag is not found
     * @throws CampaignNotFoundException if the campaign is not found or is not active
     * @throws InvalidCampaignException if the campaign is not valid
     */
    @Transactional
    BagDto applyCampaignToBag(Long bagId, Long campaignId);


    /**
     * Removes a campaign from a bag.
     *
     * @param bagId the ID of the bag
     * @return the updated bag data transfer object
     * @throws BagNotFoundException if the bag is not found
     * @throws CampaignNotFoundException if the campaign is not found in the bag
     */
    @Transactional
    BagDto removeCampaignFromBag(Long bagId);
}

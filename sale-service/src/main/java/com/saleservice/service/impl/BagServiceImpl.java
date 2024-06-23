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
import com.saleservice.service.BagService;
import com.saleservice.service.CampaignService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class BagServiceImpl implements BagService {

    private final BagRepository bagRepository;
    private final ProductServiceClient productService;
    private final CampaignService campaignService;
    private final ModelMapper modelMapper;


    @Override
    @Transactional
    public BagDto addProductToBag(Long bagId, String barcode, int quantity) {
        log.trace("addProductToBag method begins. BagId: {}, Barcode: {}, Quantity: {}", bagId, barcode, quantity);

        if (barcode == null || barcode.trim().isEmpty()) {
            log.warn("addProductToBag: barcode is empty");
            throw new InvalidInputException("barcode is empty");
        }

        ProductDto productDto = productService.getProduct(barcode).getBody();

        Bag bag = (bagId == null) ? new Bag()
                                  : bagRepository.findById(bagId).orElse(new Bag());

        BagItem existingItem = bag.getItems().stream()
                .filter(item -> item.getBarcode().equals(barcode))
                .findFirst()
                .orElse(null);

        int thresholdValue = productDto.getStock();
        if (existingItem != null) {
            if ((existingItem.getQuantity() + quantity) > thresholdValue) {
                log.warn("addProductToBag: Quantity exceeds stock for product with barcode {}", barcode);
                throw new InvalidInputException("The quantity of products in the bag cannot be more than stock");}

            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        }
        else {
            if (quantity > thresholdValue) {
                log.warn("addProductToBag: Quantity exceeds stock for product with barcode {}", barcode);
                throw new InvalidInputException("The quantity of products in the bag cannot be more than stock");}

            BagItem newBagItem = new BagItem(barcode, quantity, productDto.getPrice(), productDto.getName());
            bag.getItems().add(newBagItem);
        }

        updatePrice(bag);
        bag.setExpiration(1800L);
        bagRepository.save(bag);

        log.info("addProductToBag: Product added to bag successfully. BagId: {}, Barcode: {}", bagId, barcode);
        log.trace("addProductToBag method ends. BagId: {}, Barcode: {}, Quantity: {}", bagId, barcode, quantity);
        return modelMapper.map(bag, BagDto.class);
    }


    @Override
    @Transactional
    public void deleteBagById(Long bagId) {
        log.trace("deleteBagById method begins. BagId: {}", bagId);

        if (bagId == null) {
            log.warn("deleteBagById: bagId is empty");
            throw new InvalidInputException("bagId is empty");
        }

        Bag bag = bagRepository.findById(bagId)
                .orElseThrow(() -> {
                    log.warn("deleteBagById: Bag not found with id {}", bagId);
                    return new BagNotFoundException("Bag not found with id: " + bagId); });

        bagRepository.delete(bag);

        log.info("deleteBagById: Bag deleted successfully. BagId: {}", bagId);
        log.trace("deleteBagById method ends. BagId: {}", bagId);
    }


    @Override
    @Transactional
    public BagDto removeProductFromBag(Long bagId, String barcode, int quantity) {

        log.trace("removeProductFromBag method begins. BagId: {}, Barcode: {}, Quantity: {}", bagId, barcode, quantity);

        if (bagId == null) {
            log.warn("removeProductFromBag: bagId is empty");
            throw new InvalidInputException("bagId is empty");
        }
        if (barcode == null || barcode.trim().isEmpty()) {
            log.warn("removeProductFromBag: Barcode is empty");
            throw new InvalidInputException("Barcode is empty");
        }

        Bag bag = bagRepository.findById(bagId)
                .orElseThrow(() -> {
                    log.warn("removeProductFromBag: Bag not found with id {}", bagId);
                    return new BagNotFoundException("Bag not found with id: " + bagId); });

        BagItem existingItem = bag.getItems().stream()
                .filter(item -> item.getBarcode().equals(barcode))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("removeProductFromBag: Item not found in bag with barcode {}", barcode);
                    return new BagNotFoundException("Item not found in bag with barcode: " + barcode); });

        if (existingItem.getQuantity() < quantity) {
            log.warn("removeProductFromBag: Quantity to remove exceeds the quantity in the bag. Barcode: {}", barcode);
            throw new InvalidInputException("Quantity to remove exceeds the quantity in the bag");
        }

        if (existingItem.getQuantity() == quantity) {
            bag.getItems().remove(existingItem);
        } else {
            existingItem.setQuantity(existingItem.getQuantity() - quantity);
        }

        updatePrice(bag);
        bag.setExpiration(1800L);
        bagRepository.save(bag);

        log.info("removeProductFromBag: Product removed from bag successfully. BagId: {}, Barcode: {}", bagId, barcode);
        log.trace("removeProductFromBag method ends. BagId: {}, Barcode: {}, Quantity: {}", bagId, barcode, quantity);
        return modelMapper.map(bag, BagDto.class);
    }


    @Override
    @Transactional
    public BagDto removeAllProductsFromBag(Long bagId) {
        log.trace("removeAllProductsFromBag method begins. BagId: {}", bagId);

        if (bagId == null) {
            log.warn("removeAllProductsFromBag: bagId is empty");
            throw new InvalidInputException("bagId is empty");
        }

        Bag bag = bagRepository.findById(bagId)
                .orElseThrow(() -> {
                    log.warn("removeAllProductsFromBag: Bag not found with id {}", bagId);
                    return new BagNotFoundException("Bag not found with id: " + bagId); });

        bag.getItems().clear();

        resetCampaignDetails(bag);

        updatePrice(bag);
        bag.setExpiration(1800L);
        bagRepository.save(bag);

        log.info("removeAllProductsFromBag: All products removed from bag successfully. BagId: {}", bagId);
        log.trace("removeAllProductsFromBag method ends. BagId: {}", bagId);
        return modelMapper.map(bag, BagDto.class);
    }


    @Override
    public BagDto getBagById(Long bagId) {
        log.trace("getBagById method begins. BagId: {}", bagId);

        if (bagId == null) {
            log.warn("getBagById: bagId is empty");
            throw new InvalidInputException("bagId is empty");
        }

        Bag bag = bagRepository.findById(bagId)
                .orElseThrow(() -> {
                    log.warn("getBagById: Bag not found with id {}", bagId);
                    return new BagNotFoundException("Bag not found with id: " + bagId); });

        log.info("getBagById: Bag retrieved successfully. BagId: {}", bagId);
        log.trace("getBagById method ends. BagId: {}", bagId);
        return modelMapper.map(bag, BagDto.class);
    }


    @Override
    public Page<BagDto> getAllBags(int pageNumber, int pageSize) {
        log.trace("getAllBags method begins. PageNumber: {}, PageSize: {}", pageNumber, pageSize);

        if (pageSize < 1) {
            log.warn("getAllBags: Minimum page size is 1");
            throw new InvalidInputException("Minimum page size is 1"); }
        if (pageNumber < 1) {
            log.warn("getAllBags: Page number must be at least 1");
            throw new InvalidInputException("Page number must be at least 1"); }

        List<Bag> bags = new ArrayList<>();
        bagRepository.findAll().forEach(bags::add);
        bags.removeIf(Objects::isNull);

        List<BagDto> bagDtos = bags.stream()
                .map(bag -> modelMapper.map(bag, BagDto.class))
                .collect(Collectors.toList());


        Pageable pageable = PageRequest.of(pageNumber-1, pageSize);

        int start = Math.min((int) pageable.getOffset(), bagDtos.size());
        int end = Math.min((start + pageable.getPageSize()), bagDtos.size());
        List<BagDto> pageContent = bagDtos.subList(start, end);

        log.info("getAllBags: Retrieved all bags successfully. PageNumber: {}, PageSize: {}", pageNumber, pageSize);
        log.trace("getAllBags method ends. PageNumber: {}, PageSize: {}", pageNumber, pageSize);
        return new PageImpl<>(pageContent, pageable, bagDtos.size());
    }


    @Override
    @Transactional
    public BagDto applyCampaignToBag(Long bagId, Long campaignId) {
        log.trace("applyCampaignToBag method begins. BagId: {}, CampaignId: {}", bagId, campaignId);

        Bag bag = bagRepository.findById(bagId)
                .orElseThrow(() -> {
                    log.warn("applyCampaignToBag: Bag not found with id {}", bagId);
                    return new BagNotFoundException("Bag not found with id: " + bagId); });

        CampaignResponseDto campaign = campaignService.getCampaignById(campaignId);
        if (campaign == null) {
            log.warn("applyCampaignToBag: Campaign not found with id {}", campaignId);
            throw new CampaignNotFoundException("Campaign not found with id: " + campaignId);
        }
        if (campaign.isDeleted() || LocalDateTime.now().isBefore(campaign.getStartDate()) || LocalDateTime.now().isAfter(campaign.getEndDate())) {
            log.warn("applyCampaignToBag: Campaign is not active or has been deleted. CampaignId: {}", campaignId);
            throw new InvalidCampaignException("Campaign is not active or has been deleted.");
        }

        BigDecimal totalPrice = calculateTotalPrice(bag);
        applyDiscount(bag, totalPrice, campaign.getDiscountValue(), campaign.getDiscountType());

        bag.setTotalPrice(totalPrice);
        bag.setCampaignId(campaignId);
        bag.setCampaignName(campaign.getName());
        bagRepository.save(bag);

        log.info("applyCampaignToBag: Campaign applied to bag successfully. BagId: {}, CampaignId: {}", bagId, campaignId);
        log.trace("applyCampaignToBag method ends. BagId: {}, CampaignId: {}", bagId, campaignId);
        return modelMapper.map(bag, BagDto.class);
    }


    @Override
    @Transactional
    public BagDto removeCampaignFromBag(Long bagId) {
        log.trace("removeCampaignFromBag method begins. BagId: {}", bagId);

        Bag bag = bagRepository.findById(bagId)
                .orElseThrow(() -> {
                    log.warn("removeCampaignFromBag: Bag not found with id {}", bagId);
                    return new BagNotFoundException("Bag not found with id: " + bagId); });

        if (bag.getCampaignId() == null) {
            log.warn("removeCampaignFromBag: Campaign not found in the bag with id {}", bagId);
            throw new CampaignNotFoundException("Campaign not found in the bag with id: " + bagId);
        }

        resetCampaignDetails(bag);

        bagRepository.save(bag);

        log.info("removeCampaignFromBag: Campaign removed from bag successfully. BagId: {}", bagId);
        log.trace("removeCampaignFromBag method ends. BagId: {}", bagId);
        return modelMapper.map(bag, BagDto.class);
    }



    private BigDecimal calculateTotalPrice(Bag bag) {
        log.trace("calculateTotalPrice method begins. BagId: {}", bag.getId());

        BigDecimal totalPrice = bag.getItems().stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.trace("calculateTotalPrice method ends. BagId: {}, TotalPrice: {}", bag.getId(), totalPrice);
        return totalPrice;
    }


    private void applyDiscount(Bag bag,
                               BigDecimal totalPrice,
                               double discountValue,
                               DiscountType discountType) {
        log.trace("applyDiscount method begins. BagId: {}, TotalPrice: {}, DiscountValue: {}, DiscountType: {}",
                bag.getId(), totalPrice, discountValue, discountType);

        BigDecimal discountAmount = BigDecimal.ZERO;

        if (discountType == DiscountType.PERCENTAGE) {
            BigDecimal discountFactor = BigDecimal.valueOf(discountValue)
                    .divide(new BigDecimal("100"), 5, RoundingMode.HALF_DOWN);
            discountAmount = totalPrice.multiply(discountFactor);
            discountAmount = discountAmount.setScale(2, RoundingMode.DOWN);
        } else if (discountType == DiscountType.FIXED_AMOUNT) {
            discountAmount = BigDecimal.valueOf(discountValue).min(totalPrice);
        }
        BigDecimal discountedPrice = totalPrice.subtract(discountAmount).max(BigDecimal.ZERO);

        bag.setDiscountedPrice(discountedPrice);
        bag.setDiscountValue(discountValue);
        bag.setDiscountType(discountType);

        log.trace("applyDiscount method ends. BagId: {}, DiscountedPrice: {}", bag.getId(), discountedPrice);
    }


    private void updatePrice(Bag bag) {
        log.trace("updatePrice method begins. BagId: {}", bag.getId());

        BigDecimal totalPrice = calculateTotalPrice(bag);
        bag.setTotalPrice(totalPrice);
        if (bag.getCampaignId() != null) {
                applyDiscount(bag, totalPrice, bag.getDiscountValue(), bag.getDiscountType());
        }

        log.trace("updatePrice method ends. BagId: {}, TotalPrice: {}", bag.getId(), totalPrice);
    }


    private void resetCampaignDetails(Bag bag) {
        log.trace("resetCampaignDetails method begins. BagId: {}", bag.getId());

        bag.setCampaignId(null);
        bag.setCampaignName(null);
        bag.setDiscountValue(0);
        bag.setDiscountType(null);
        bag.setDiscountedPrice(null);

        log.trace("resetCampaignDetails method ends. BagId: {}", bag.getId());
    }
}

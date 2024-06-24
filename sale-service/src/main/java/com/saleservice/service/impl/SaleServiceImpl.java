package com.saleservice.service.impl;

import com.saleservice.config.RabbitMqMessagePublisher;
import com.saleservice.dto.*;
import com.saleservice.exception.*;
import com.saleservice.model.PaymentMethod;
import com.saleservice.model.Sale;
import com.saleservice.model.SaleItem;
import com.saleservice.repository.SaleRepository;
import com.saleservice.service.BagService;
import com.saleservice.service.SaleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final BagService bagService;
    private final ModelMapper modelMapper;
    private final RabbitMqMessagePublisher rabbitMqMessagePublisher;


    @Override
    @Transactional
    public ReceiptMessage completeSale(Long bagId,
                                       BigDecimal amountReceived,
                                       PaymentMethod paymentMethod,
                                       String cashierName) {
        log.trace("completeSale method begins. BagId: {}, AmountReceived: {}, PaymentMethod: {}, CashierName: {}",
                bagId, amountReceived, paymentMethod, cashierName);

        BagDto bagDto = getBagDtoFromRepository(bagId);

        BigDecimal priceToPay = (bagDto.getCampaignId() != null)
                ? bagDto.getDiscountedPrice()
                : bagDto.getTotalPrice();

        amountReceived = validatePayment(amountReceived, paymentMethod, priceToPay);
        BigDecimal change = calculateChange(amountReceived, priceToPay, paymentMethod);

        Sale sale = Sale.builder()
                .cashierName(parseName(cashierName))
                .saleDate(LocalDateTime.now())
                .totalPrice(bagDto.getTotalPrice())
                .discountedPrice(bagDto.getDiscountedPrice())
                .campaignName(bagDto.getCampaignName())
                .campaignId(bagDto.getCampaignId())
                .discountType(bagDto.getDiscountType())
                .discountValue(bagDto.getDiscountValue())
                .amountReceived(amountReceived)
                .change(change)
                .paymentMethod(paymentMethod)
                .saleItems(new ArrayList<>())
                .isCancelled(false)
                .build();
        log.debug("completeSale: Sale object created: {}", sale);

        bagDto.getItems()
                .forEach(bagItemDto -> {
            if (bagItemDto != null){
                SaleItem saleItem = SaleItem.builder()
                        .barcode(bagItemDto.getBarcode())
                        .name(bagItemDto.getName())
                        .quantity(bagItemDto.getQuantity())
                        .salePrice(bagItemDto.getPrice())
                        .sale(sale)
                        .build();
                sale.getSaleItems().add(saleItem);

                StockUpdateMessage message = new StockUpdateMessage(bagItemDto.getBarcode(), -bagItemDto.getQuantity(), 0);
                rabbitMqMessagePublisher.publishMessage(message, RabbitMqMessagePublisher.MessageType.STOCK);
                log.debug("completeSale: Stock update message published for barcode: {}", bagItemDto.getBarcode());
            }
        });
        
        saleRepository.save(sale);
        log.info("completeSale: Sale saved successfully. SaleId: {}", sale.getId());
        bagService.deleteBagById(bagId);

        log.info("completeSale: Sale completed successfully. SaleId: {}", sale.getId());
        log.trace("completeSale method ends. BagId: {}, AmountReceived: {}, PaymentMethod: {}, CashierName: {}",
                bagId, amountReceived, paymentMethod, cashierName);
        return publishReceiptMessageToQueues(sale);
    }


    @Override
    @Transactional
    public void cancelSale(Long saleId) {
        log.trace("cancelSale method begins. SaleId: {}", saleId);

        Sale sale = saleRepository.findById(saleId).orElseThrow(() -> {
            log.warn("cancelSale: Sale not found with id: {}", saleId);
            return new SaleNotFoundException("Sale not found with id: " + saleId); });

        if (sale.isCancelled()) {
            log.warn("cancelSale: Sale is already cancelled. SaleId: {}", saleId);
            throw new SaleAlreadyCancelledException("Sale is already cancelled."); }

        sale.getSaleItems().forEach(saleItem -> {

            StockUpdateMessage message = new StockUpdateMessage(saleItem.getBarcode(), saleItem.getQuantity(), 0);
            rabbitMqMessagePublisher.publishMessage(message, RabbitMqMessagePublisher.MessageType.STOCK);
            log.debug("cancelSale: Stock update message published for barcode: {}, message : {}", saleItem.getBarcode(), message);
        });

        sale.setCancelled(true);
        saleRepository.save(sale);

        log.info("cancelSale: Sale cancelled successfully. SaleId: {}", saleId);
        log.trace("cancelSale method ends. SaleId: {}", saleId);
    }



    private BigDecimal calculateChange(BigDecimal amountReceived, BigDecimal priceToPay, PaymentMethod paymentMethod) {
        log.trace("calculateChange method begins. AmountReceived: {}, PriceToPay: {}, PaymentMethod: {}", amountReceived, priceToPay, paymentMethod);

        BigDecimal change;

        if (paymentMethod == PaymentMethod.CREDIT_CARD) {
            change = BigDecimal.ZERO;
            log.trace("calculateChange method ends. AmountReceived: {}, PriceToPay: {}, Change: {}", amountReceived, priceToPay, change);
            return change;
        }

        change = amountReceived.subtract(priceToPay).max(BigDecimal.ZERO);
        log.trace("calculateChange method ends. AmountReceived: {}, PriceToPay: {}, Change: {}", amountReceived, priceToPay, change);
        return change;
    }


    private static BigDecimal validatePayment(BigDecimal amountReceived, PaymentMethod paymentMethod, BigDecimal priceToPay) {
        log.trace("validatePayment method begins. AmountReceived: {}, PaymentMethod: {}, PriceToPay: {}",
                amountReceived, paymentMethod, priceToPay);

        if(paymentMethod.equals(PaymentMethod.CREDIT_CARD)) {
            amountReceived = priceToPay; }
        if (amountReceived.compareTo(priceToPay) < 0) {
            log.warn("validatePayment: Received amount is less than the total sale price. AmountReceived: {}, PriceToPay: {}", amountReceived, priceToPay);
            throw new InvalidInputException("Received amount is less than the total sale price.");
        }

        log.debug("validatePayment: Payment validated. AmountReceived: {}, PaymentMethod: {}, PriceToPay: {}",
                amountReceived, paymentMethod, priceToPay);
        log.trace("validatePayment method ends. AmountReceived: {}, PaymentMethod: {}, PriceToPay: {}",
                amountReceived, paymentMethod, priceToPay);
        return amountReceived;
    }


    private ReceiptMessage publishReceiptMessageToQueues(Sale sale) {
        log.trace("publishReceiptMessagesToQueues method begins. SaleId: {}", sale.getId());

        SaleDto saleDto = modelMapper.map(sale, SaleDto.class);

        String receiptId = UUID.randomUUID().toString();
        ReceiptMessage message = new ReceiptMessage(receiptId, saleDto);

        rabbitMqMessagePublisher.publishMessage(message, RabbitMqMessagePublisher.MessageType.RECEIPT);
        log.debug("publishReceiptMessagesToQueues: Receipt message published. ReceiptId: {}", receiptId);
        rabbitMqMessagePublisher.publishEvent(saleDto.getId(), receiptId);
        log.debug("publishReceiptMessagesToQueues: Event published for SaleId: {} with ReceiptId: {}", saleDto.getId(), receiptId);

        log.trace("publishReceiptMessagesToQueues method ends. SaleId: {}, ReceiptId: {}", sale.getId(), receiptId);
        return message;
    }


    private BagDto getBagDtoFromRepository(Long bagId) {
        log.trace("getBagDto method begins. BagId: {}", bagId);

        BagDto bagDto = bagService.getBagById(bagId);

        if (bagDto == null) {
            log.warn("getBagDto: Bag not found with id: {}", bagId);
            throw new BagNotFoundException("Bag not found with id: " + bagId);}
        if (bagDto.getItems() == null || bagDto.getItems().isEmpty()) {
            log.warn("getBagDto: No items in the bag to process the sale. BagId: {}", bagId);
            throw new BagIsEmptyException("No items in the bag to process the sale.");}

        log.trace("getBagDto method ends. BagId: {}", bagId);
        return bagDto;
    }


    private String parseName(String name){
        log.trace("parseName method begins. Name: {}", name);

        String[] parts = name.split("-");
        String parsedName = parts[1];

        log.debug("parseName: Parsed name: {}", parsedName);
        log.trace("parseName method ends. Name: {}, ParsedName: {}", name, parsedName);
        return parsedName;
    }
}

package com.saleservice.service.impl;

import com.saleservice.config.RabbitMqMessagePublisher;
import com.saleservice.dto.*;
import com.saleservice.exception.*;
import com.saleservice.model.DiscountType;
import com.saleservice.model.PaymentMethod;
import com.saleservice.model.Sale;
import com.saleservice.model.SaleItem;
import com.saleservice.repository.SaleRepository;
import com.saleservice.service.BagService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SaleServiceImplTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private BagService bagService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private RabbitMqMessagePublisher rabbitMqMessagePublisher;

    @InjectMocks
    private SaleServiceImpl saleService;


    @Test
    void whenCompleteSaleWithNullBagId_thenThrowBagNotFoundException() {
        BigDecimal amountReceived = BigDecimal.TEN;
        PaymentMethod paymentMethod = PaymentMethod.CASH;
        String cashierName = "John Doe";

        BagNotFoundException exception = assertThrows(BagNotFoundException.class,
                () -> saleService.completeSale(null, amountReceived, paymentMethod, cashierName));

        assertEquals("Bag not found with id: null", exception.getMessage());
        verify(bagService, never()).getBagById(anyLong());
    }

    @Test
    void whenCompleteSaleWithNullBagDto_thenThrowBagNotFoundException() {
        Long bagId = 1L;
        BigDecimal amountReceived = BigDecimal.TEN;
        PaymentMethod paymentMethod = PaymentMethod.CASH;
        String cashierName = "John Doe";

        when(bagService.getBagById(bagId)).thenReturn(null);

        BagNotFoundException exception = assertThrows(BagNotFoundException.class,
                () -> saleService.completeSale(bagId, amountReceived, paymentMethod, cashierName));

        assertEquals("Bag not found with id: 1", exception.getMessage());
        verify(bagService, times(1)).getBagById(bagId);
    }

    @Test
    void whenCompleteSaleWithEmptyBag_thenThrowBagIsEmptyException() {
        Long bagId = 1L;
        BigDecimal amountReceived = BigDecimal.TEN;
        PaymentMethod paymentMethod = PaymentMethod.CASH;
        String cashierName = "John Doe";

        BagDto bagDto = new BagDto();
        bagDto.setItems(Collections.emptyList());

        when(bagService.getBagById(bagId)).thenReturn(bagDto);

        BagIsEmptyException exception = assertThrows(BagIsEmptyException.class,
                () -> saleService.completeSale(bagId, amountReceived, paymentMethod, cashierName));

        assertEquals("No items in the bag to process the sale.", exception.getMessage());
        verify(bagService, times(1)).getBagById(bagId);
    }

    @Test
    void whenCompleteSaleWithNullBagItems_thenThrowBagIsEmptyException() {
        Long bagId = 1L;
        BigDecimal amountReceived = BigDecimal.TEN;
        PaymentMethod paymentMethod = PaymentMethod.CASH;
        String cashierName = "John Doe";

        BagDto bagDto = new BagDto();
        bagDto.setItems(null);

        when(bagService.getBagById(bagId)).thenReturn(bagDto);

        BagIsEmptyException exception = assertThrows(BagIsEmptyException.class,
                () -> saleService.completeSale(bagId, amountReceived, paymentMethod, cashierName));

        assertEquals("No items in the bag to process the sale.", exception.getMessage());
        verify(bagService, times(1)).getBagById(bagId);
    }

    @Test
    void whenCompleteSaleWithCreditCardPayment_thenSetAmountReceivedToTotalPrice() {
        Long bagId = 1L;
        BigDecimal amountReceived = BigDecimal.ZERO;
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;
        String cashierName = "Store-123";

        BagItemDto bagItem = new BagItemDto("12345", 2, BigDecimal.TEN, "Test Product");
        BagDto bagDto = new BagDto();
        bagDto.setItems(Collections.singletonList(bagItem));
        bagDto.setTotalPrice(BigDecimal.valueOf(20));

        Sale sale = Sale.builder()
                .cashierName(cashierName)
                .saleDate(LocalDateTime.now())
                .totalPrice(BigDecimal.valueOf(20))
                .amountReceived(BigDecimal.valueOf(20))
                .change(BigDecimal.ZERO)
                .paymentMethod(paymentMethod)
                .saleItems(new ArrayList<>())
                .isCancelled(false)
                .build();

        when(bagService.getBagById(bagId)).thenReturn(bagDto);
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        SaleDto saleDto = new SaleDto();
        saleDto.setId(1L);
        when(modelMapper.map(any(Sale.class), eq(SaleDto.class))).thenReturn(saleDto);

        saleService.completeSale(bagId, amountReceived, paymentMethod, cashierName);

        verify(saleRepository, times(1)).save(any(Sale.class));
        verify(bagService, times(1)).deleteBagById(bagId);
    }

    @Test
    void whenCompleteSaleWithAmountReceivedLessThanTotalPrice_thenThrowInvalidInputException() {
        Long bagId = 1L;
        BigDecimal amountReceived = BigDecimal.valueOf(5);
        PaymentMethod paymentMethod = PaymentMethod.CASH;
        String cashierName = "John Doe";

        BagItemDto bagItem = new BagItemDto("12345", 2, BigDecimal.TEN, "Test Product");
        BagDto bagDto = new BagDto();
        bagDto.setItems(Collections.singletonList(bagItem));
        bagDto.setTotalPrice(BigDecimal.valueOf(20));

        when(bagService.getBagById(bagId)).thenReturn(bagDto);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> saleService.completeSale(bagId, amountReceived, paymentMethod, cashierName));

        assertEquals("Received amount is less than the total sale price.", exception.getMessage());
        verify(saleRepository, never()).save(any(Sale.class));
        verify(bagService, never()).deleteBagById(anyLong());
    }

    @Test
    void whenCompleteSaleWithNullAndValidBagItems_thenProcessSaleCorrectly() {
        Long bagId = 1L;
        BigDecimal amountReceived = BigDecimal.valueOf(18);
        PaymentMethod paymentMethod = PaymentMethod.CASH;
        String cashierName = "Store-123";

        BagItemDto bagItem1 = new BagItemDto("12345", 2, BigDecimal.TEN, "Test Product");
        BagItemDto bagItem2 = null;
        BagDto bagDto = new BagDto();
        bagDto.setItems(Arrays.asList(bagItem1, bagItem2));
        bagDto.setTotalPrice(BigDecimal.valueOf(20));
        bagDto.setDiscountedPrice(BigDecimal.valueOf(18));
        bagDto.setCampaignId(1L);
        bagDto.setCampaignName("Summer Sale");
        bagDto.setDiscountType(DiscountType.PERCENTAGE);
        bagDto.setDiscountValue(10);

        Sale sale = Sale.builder()
                .cashierName(cashierName)
                .saleDate(LocalDateTime.now())
                .totalPrice(BigDecimal.valueOf(20))
                .amountReceived(BigDecimal.valueOf(18))
                .change(BigDecimal.ZERO)
                .paymentMethod(paymentMethod)
                .saleItems(new ArrayList<>())
                .isCancelled(false)
                .build();

        when(bagService.getBagById(bagId)).thenReturn(bagDto);
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        SaleDto saleDto = new SaleDto();
        saleDto.setId(1L);
        when(modelMapper.map(any(Sale.class), eq(SaleDto.class))).thenReturn(saleDto);

        saleService.completeSale(bagId, amountReceived, paymentMethod, cashierName);

        verify(saleRepository, times(1)).save(any(Sale.class));
        verify(bagService, times(1)).deleteBagById(bagId);
        verify(rabbitMqMessagePublisher, times(1)).publishMessage(any(ReceiptMessage.class), eq(RabbitMqMessagePublisher.MessageType.RECEIPT));
        verify(rabbitMqMessagePublisher, times(1)).publishMessage(any(StockUpdateMessage.class), eq(RabbitMqMessagePublisher.MessageType.STOCK));
    }


    @Test
    void whenCancelSaleWithNonExistentSale_thenThrowSaleNotFoundException() {
        Long saleId = 1L;

        when(saleRepository.findById(saleId)).thenReturn(Optional.empty());

        SaleNotFoundException exception = assertThrows(SaleNotFoundException.class,
                () -> saleService.cancelSale(saleId));

        assertEquals("Sale not found with id: 1", exception.getMessage());
        verify(saleRepository, times(1)).findById(saleId);
        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    void whenCancelSaleWithAlreadyCancelledSale_thenThrowSaleAlreadyCancelledException() {
        Long saleId = 1L;

        Sale sale = Sale.builder()
                .isCancelled(true)
                .build();

        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));

        SaleAlreadyCancelledException exception = assertThrows(SaleAlreadyCancelledException.class,
                () -> saleService.cancelSale(saleId));

        assertEquals("Sale is already cancelled.", exception.getMessage());
        verify(saleRepository, times(1)).findById(saleId);
        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    void whenCancelSaleSuccessfully_thenPublishStockUpdateMessages() {
        Long saleId = 1L;

        SaleItem saleItem1 = SaleItem.builder()
                .barcode("12345")
                .quantity(2)
                .build();

        SaleItem saleItem2 = SaleItem.builder()
                .barcode("67890")
                .quantity(1)
                .build();

        Sale sale = Sale.builder()
                .isCancelled(false)
                .saleItems(Arrays.asList(saleItem1, saleItem2))
                .build();

        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));

        saleService.cancelSale(saleId);

        assertTrue(sale.isCancelled());
        verify(saleRepository, times(1)).findById(saleId);
        verify(saleRepository, times(1)).save(sale);
        verify(rabbitMqMessagePublisher, times(2)).publishMessage(any(StockUpdateMessage.class), eq(RabbitMqMessagePublisher.MessageType.STOCK));
    }
}
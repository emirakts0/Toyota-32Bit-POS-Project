package com.reportingservice.service.impl;

import com.reportingservice.dto.ReceiptMessage;
import com.reportingservice.dto.SaleDto;
import com.reportingservice.dto.SaleSearchCriteria;
import com.reportingservice.dto.SaleSearchCriteriaWithPagination;
import com.reportingservice.exception.InvalidInputException;
import com.reportingservice.exception.SaleNotFoundException;
import com.reportingservice.model.Sale;
import com.reportingservice.repository.SaleRepository;
import com.reportingservice.service.ReceiptTrackingService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.reportingservice.model.PaymentMethod.CREDIT_CARD;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SaleReportingServiceImplTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ReceiptTrackingService receiptTrackingService;

    @Mock
    private AmqpTemplate rabbitTemplate;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Root<Sale> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private Predicate predicate;


    @InjectMocks
    private SaleReportingServiceImpl saleReportingService;


    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(saleReportingService, "queueName", "testQueue");
    }

    @Test
    void whenGenerateReceiptByIdWithValidId_thenReceiptGeneratedSuccessfully() {
        Long saleId = 1L;
        Sale sale = new Sale();
        SaleDto saleDto = new SaleDto();
        String requestId = "requestId";

        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(modelMapper.map(sale, SaleDto.class)).thenReturn(saleDto);
        when(receiptTrackingService.initializeReceiptCache(saleId, null)).thenReturn(requestId);

        String result = saleReportingService.generateReceiptById(saleId);

        assertEquals(requestId, result);
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), any(ReceiptMessage.class));
    }

    @Test
    void whenGenerateReceiptByIdWithInvalidId_thenThrowSaleNotFoundException() {
        Long saleId = 1L;

        when(saleRepository.findById(saleId)).thenReturn(Optional.empty());

        SaleNotFoundException exception = assertThrows(SaleNotFoundException.class,
                () -> saleReportingService.generateReceiptById(saleId));

        assertEquals("Sale not found with id: " + saleId, exception.getMessage());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), any(ReceiptMessage.class));
    }


    @ParameterizedTest
    @CsvSource({
            "true, true",
            "false, false"
    })
    void whenGetSalesByCriteriaWithAllCriteria_thenReturnSales(boolean hasCampaign, boolean includeCanceled) {
        SaleSearchCriteria criteria = new SaleSearchCriteria();
        criteria.setMinTotalPrice(BigDecimal.valueOf(50));
        criteria.setMaxTotalPrice(BigDecimal.valueOf(150));
        criteria.setHasCampaign(hasCampaign);
        criteria.setCampaignId(1L);
        criteria.setPaymentMethod(CREDIT_CARD);
        criteria.setSaleDateStart("2023-01-01T00:00:00");
        criteria.setSaleDateEnd("2023-12-31T23:59:59");
        criteria.setIncludeCanceled(includeCanceled);

        Sale sale = new Sale();
        sale.setId(1L);
        sale.setTotalPrice(BigDecimal.valueOf(100));
        sale.setSaleDate(LocalDateTime.now());

        SaleDto saleDto = new SaleDto();
        saleDto.setId(1L);
        saleDto.setTotalPrice(BigDecimal.valueOf(100));
        saleDto.setSaleDate(LocalDateTime.now());

        List<Sale> sales = List.of(sale);
        when(saleRepository.findAll(any(Specification.class))).thenReturn(sales);
        when(modelMapper.map(any(Sale.class), any(Class.class))).thenReturn(saleDto);

        List<SaleDto> result = saleReportingService.getSalesByCriteria(criteria);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(saleDto, result.get(0));

        //---learn
        ArgumentCaptor<Specification<Sale>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(saleRepository).findAll(specCaptor.capture());
        Specification<Sale> capturedSpec = specCaptor.getValue();

        assertNotNull(capturedSpec);

        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(predicate);
        Predicate predicate = capturedSpec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(predicate);
        //---

        verify(saleRepository, times(1)).findAll(any(Specification.class));
        verify(modelMapper, times(1)).map(sale, SaleDto.class);
    }

    @Test
    void whenGetSalesByCriteriaWithNoCriteria_thenReturnSales() {
        SaleSearchCriteria criteria = new SaleSearchCriteria();

        Sale sale = new Sale();
        sale.setId(1L);
        sale.setTotalPrice(BigDecimal.valueOf(100));
        sale.setSaleDate(LocalDateTime.now());

        SaleDto saleDto = new SaleDto();
        saleDto.setId(1L);
        saleDto.setTotalPrice(BigDecimal.valueOf(100));
        saleDto.setSaleDate(LocalDateTime.now());

        List<Sale> sales = List.of(sale);
        when(saleRepository.findAll(any(Specification.class))).thenReturn(sales);
        when(modelMapper.map(any(Sale.class), any(Class.class))).thenReturn(saleDto);

        List<SaleDto> result = saleReportingService.getSalesByCriteria(criteria);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(saleDto, result.get(0));

        //---learn
        ArgumentCaptor<Specification<Sale>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(saleRepository).findAll(specCaptor.capture());
        Specification<Sale> capturedSpec = specCaptor.getValue();

        assertNotNull(capturedSpec);

        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(predicate);
        Predicate predicate = capturedSpec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(predicate);
        //---

        verify(saleRepository, times(1)).findAll(any(Specification.class));
        verify(modelMapper, times(1)).map(sale, SaleDto.class);
    }

    @Test
    void whenGetSalesByCriteriaWithPaginationAndInvalidEndDate_thenThrowInvalidInputExceptionWithPagination() {
        SaleSearchCriteriaWithPagination criteriaWithPagination = new SaleSearchCriteriaWithPagination();
        criteriaWithPagination.setSaleDateEnd("invalid-date-format");
        criteriaWithPagination.setPage(1);
        criteriaWithPagination.setSize(10);
        criteriaWithPagination.setSortDir("asc");

        SaleSearchCriteria criteria = new SaleSearchCriteria();
        criteria.setSaleDateEnd("invalid-date-format");


        when(modelMapper.map(any(SaleSearchCriteriaWithPagination.class), eq(SaleSearchCriteria.class))).thenReturn(criteria);
        //---learn
        when(saleRepository.findAll(any(Specification.class), any(Pageable.class))).thenAnswer(invocation -> {
                    Specification<Sale> specification = invocation.getArgument(0);
                    specification.toPredicate(root, query, criteriaBuilder);
                    return new PageImpl<>(List.of()); });
        //---

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> saleReportingService.getSalesByCriteriaWithPagination(criteriaWithPagination));

        assertEquals("Date must be in the format 'yyyy-MM-ddTHH:mm:ss'", exception.getMessage());


        verify(saleRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void whenGetSalesByCriteriaWithPaginationAndSimpleParameters_thenReturnPaginatedSales() {
        SaleSearchCriteriaWithPagination criteriaWithPagination = new SaleSearchCriteriaWithPagination();
        criteriaWithPagination.setPage(1);
        criteriaWithPagination.setSize(10);
        SaleSearchCriteria criteria = new SaleSearchCriteria();


        when(modelMapper.map(any(SaleSearchCriteriaWithPagination.class), eq(SaleSearchCriteria.class))).thenReturn(criteria);
        //---learn
        when(saleRepository.findAll(any(Specification.class), any(Pageable.class))).thenAnswer(invocation -> {
                    Specification<Sale> specification = invocation.getArgument(0);
                    specification.toPredicate(root, query, criteriaBuilder);
                    return new PageImpl<>(List.of()); });
        //---

        Page<SaleDto> saleDtoPage = saleReportingService.getSalesByCriteriaWithPagination(criteriaWithPagination);
        assertNotNull(saleDtoPage);
    }

}
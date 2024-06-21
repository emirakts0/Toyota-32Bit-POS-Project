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
import com.reportingservice.service.SaleReportingService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleReportingServiceImpl implements SaleReportingService {

    private final SaleRepository saleRepository;
    private final ModelMapper modelMapper;
    private final ReceiptTrackingService requestTrackingService;
    private final AmqpTemplate rabbitTemplate;

    @Value("${receipt.rabbitmq.queue}")
    private String queueName;


    @Override
    public String generateReceiptById(Long id) {
        log.trace("generateReceiptById method begins. SaleId: {}", id);

        SaleDto saleDto = getSaleById(id);
        String requestId = requestTrackingService.initializeReceiptCache(id, null);
        rabbitTemplate.convertAndSend(queueName, new ReceiptMessage(requestId, saleDto));

        log.trace("generateReceiptById method ends. SaleId: {}", id);
        return requestId;
    }


    @Override
    public SaleDto getSaleById(Long saleId) {
        log.trace("getSaleById method begins. SaleId: {}", saleId);

        Sale sale = saleRepository.findById(saleId).orElseThrow(() -> {
            log.warn("getSaleById: Sale not found with id: {}", saleId);
            return new SaleNotFoundException("Sale not found with id: " + saleId);});

        SaleDto saleDto = modelMapper.map(sale, SaleDto.class);

        log.trace("getSaleById method ends. SaleId: {}", saleId);
        return saleDto;
    }


    @Override
    public List<SaleDto> getSalesByCriteria(SaleSearchCriteria criteria) {
        log.trace("getSalesByCriteria method begins. Criteria: {}", criteria);

        Specification<Sale> specification = buildSpecification(criteria);
        List<Sale> sales = saleRepository.findAll(specification);

        List<SaleDto> saleDtos = sales.stream()
                .map(sale -> modelMapper.map(sale, SaleDto.class))
                .collect(Collectors.toList());

        log.trace("getSalesByCriteria method ends. Criteria: {}", criteria);
        return saleDtos;
    }


    @Override
    public Page<SaleDto> getSalesByCriteriaWithPagination(SaleSearchCriteriaWithPagination criteria) {
        log.trace("getSalesByCriteriaWithPagination method begins. Criteria: {}", criteria);

        Pageable pageable = PageRequest.of(
                criteria.getPage() - 1,
                criteria.getSize(),
                Sort.by(criteria.getSortDir().equalsIgnoreCase("desc")
                                ? Sort.Direction.DESC : Sort.Direction.ASC,
                        criteria.getSortBy()));

        SaleSearchCriteria criteria1 = modelMapper.map(criteria, SaleSearchCriteria.class);
        Specification<Sale> specification = buildSpecification(criteria1);
        Page<Sale> salesPage = saleRepository.findAll(specification, pageable);

        Page<SaleDto> saleDtoPage = salesPage.map(sale -> modelMapper.map(sale, SaleDto.class));

        log.info("getSalesByCriteriaWithPagination: Retrieved {} sales matching criteria", saleDtoPage.getTotalElements());
        log.trace("getSalesByCriteriaWithPagination method ends. Criteria: {}", criteria);
        return saleDtoPage;
    }


    private Specification<Sale> buildSpecification(SaleSearchCriteria criteria) {
        log.trace("buildSpecification method begins. Criteria: {}", criteria);

        Specification<Sale> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getMinTotalPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalPrice"), criteria.getMinTotalPrice()));
            }

            if (criteria.getMaxTotalPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalPrice"), criteria.getMaxTotalPrice()));
            }

            if (criteria.getHasCampaign() != null) {
                if (criteria.getHasCampaign()) {
                    predicates.add(criteriaBuilder.isNotNull(root.get("campaignId")));
                } else {
                    predicates.add(criteriaBuilder.isNull(root.get("campaignId")));
                }
            }

            if (criteria.getCampaignId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("campaignId"), criteria.getCampaignId()));
            }

            if (criteria.getPaymentMethod() != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentMethod"), criteria.getPaymentMethod()));
            }

            if (criteria.getIncludeCanceled() != null) {
                if (!criteria.getIncludeCanceled()) {
                    predicates.add(criteriaBuilder.isFalse(root.get("isCancelled")));
                }
            }

            if (criteria.getSaleDateStart() != null) {
                LocalDateTime saleDateStart = parseDate(criteria.getSaleDateStart());
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("saleDate"), saleDateStart));
            }

            if (criteria.getSaleDateEnd() != null) {
                LocalDateTime saleDateEnd = parseDate(criteria.getSaleDateEnd());
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("saleDate"), saleDateEnd));
            }

            log.trace("buildSpecification method ends. Criteria: {}", criteria);
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        log.trace("buildSpecification method ends. Criteria: {}", criteria);
        return specification;
    }


    private LocalDateTime parseDate(String dateStr) {
        log.trace("parseDate method begins. DateStr: {}", dateStr);

        try {
            LocalDateTime parsedDate = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            log.trace("parseDate: Parsed date successfully. DateStr: {}, ParsedDate: {}", dateStr, parsedDate);
            return parsedDate;
        } catch (DateTimeParseException e) {
            log.warn("parseDate: Failed to parse date: {}", dateStr, e);
            throw new InvalidInputException("Date must be in the format 'yyyy-MM-ddTHH:mm:ss'");
        }
    }
}

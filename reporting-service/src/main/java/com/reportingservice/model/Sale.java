package com.reportingservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "sales")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String cashierName;

    @Column(nullable = false)
    private BigDecimal totalPrice;
    private BigDecimal discountedPrice;

    private String campaignName;
    private Long campaignId;
    private DiscountType discountType;
    private double discountValue;

    private BigDecimal amountReceived;
    private BigDecimal change;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;


    @Column(nullable = false)
    private LocalDateTime saleDate;
    private boolean isCancelled;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL)
    private List<SaleItem> saleItems = new ArrayList<>();
}
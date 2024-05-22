package com.productservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_gen")
    @SequenceGenerator(name = "seq_gen", sequenceName = "seq", initialValue = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String barcode;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;
    private boolean deleted;

    @Column(updatable = false, nullable = false)
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "image_code", referencedColumnName = "image_code")
    private Image image;

    @Column(name = "has_image")
    private boolean hasImage;

}

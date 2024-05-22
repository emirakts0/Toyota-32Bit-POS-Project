package com.productservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateRequestDto {

    @NotBlank(message = "Product name cannot be empty or null.")
    @Size(min = 1, max = 50, message = "Name length must be between 1 and 50 characters.")
    private String name;

    @NotBlank(message = "Barcode cannot contain white whitespaces and cannot be empty or null.")
    @Size(min = 1, max = 35, message = "Barcode length must be between 1 and 35 characters.")
    @Pattern(regexp = "^\\S+$", message = "Barcode cannot contain white whitespaces and cannot be empty or null.")
    private String barcode;

    @NotNull(message = "Product price cannot be null.")
    @PositiveOrZero(message = "Product price must be positive or zero.")
    @Digits(integer = 10, fraction = 2, message = "Product price must have up to 2 decimal places.")
    private BigDecimal price;

    @NotNull(message = "Stock cannot be null.")
    @PositiveOrZero(message = "Stock must be positive or zero.")
    private Integer stock;

}

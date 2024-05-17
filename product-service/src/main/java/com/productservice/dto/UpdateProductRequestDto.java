package com.productservice.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateProductRequestDto {

    @Size(min = 1, max = 50, message = "Name length must be between 1 and 50 characters.")
    @Pattern(regexp = "\\S+", message = "Name cannot be blank or contain only whitespace")
    private String name;

    @Size(min = 1, max = 35, message = "Barcode length must be between 1 and 35 characters.")
    @Pattern(regexp = "\\S+", message = "Barcode cannot be blank or contain only whitespace")
    private String barcode;

    @PositiveOrZero(message = "Product price must be positive or zero.")
    private BigDecimal price;

    @PositiveOrZero(message = "Stock must be positive or zero.")
    private Integer stock;

}

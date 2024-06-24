package com.productservice;

import com.productservice.dto.ProductCreateRequestDto;
import com.productservice.service.ProductManagementService;
import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.Random;

@SpringBootApplication
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }

    @Autowired
    private ProductManagementService productManagementService;

    @Bean
    public int generateRandomProducts() {
        Random random = new Random();
        Faker faker = new Faker();

        for (int i = 0; i < 100; i++) {
            ProductCreateRequestDto request = new ProductCreateRequestDto();
            request.setName(faker.commerce().productName());
            request.setBarcode(faker.code().isbn10());
            request.setPrice(BigDecimal.valueOf(random.nextDouble() * 1000));
            request.setStock(random.nextInt(1000));

            try {
                productManagementService.addProduct(request, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 1;
    }

}

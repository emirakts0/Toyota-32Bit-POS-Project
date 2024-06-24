package com.saleservice;

import com.github.javafaker.Faker;
import com.saleservice.dto.SaleDto;
import com.saleservice.dto.SaleItemDto;
import com.saleservice.model.DiscountType;
import com.saleservice.model.PaymentMethod;
import com.saleservice.model.Sale;
import com.saleservice.model.SaleItem;
import com.saleservice.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootApplication
@EnableFeignClients
@EnableCaching
public class SalesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalesServiceApplication.class, args);
    }


    @Autowired
    private SaleRepository saleRepository;

    @Bean
    @Transactional
    public int generateRandomSales() {
        Faker faker = new Faker();
        Random random = new Random();

        for (int i = 0; i < 100; i++) {
            SaleDto saleDto = createRandomSaleDto(faker, random);
            Sale sale = convertToEntity(saleDto);
            saleRepository.save(sale);
        }
        return 1;
    }

    private SaleDto createRandomSaleDto(Faker faker, Random random) {
        SaleDto saleDto = new SaleDto();
        saleDto.setCashierName(faker.name().fullName());
        saleDto.setSaleDate(randomDateTimeInLastYear(random));

        BigDecimal totalPrice = BigDecimal.valueOf(Math.abs(random.nextDouble() * 1000)).setScale(2, RoundingMode.HALF_UP);
        DiscountType discountType = DiscountType.values()[random.nextInt(DiscountType.values().length)];
        BigDecimal discountValue = BigDecimal.valueOf(Math.abs(random.nextDouble() * 100)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountedPrice = totalPrice;

        if (discountType == DiscountType.PERCENTAGE) {
            discountValue = discountValue.min(BigDecimal.valueOf(100));
            discountedPrice = totalPrice.subtract(totalPrice.multiply(discountValue).divide(BigDecimal.valueOf(100)));
        } else if (discountType == DiscountType.FIXED_AMOUNT) {
            discountValue = discountValue.min(totalPrice);
            discountedPrice = totalPrice.subtract(discountValue);
        }

        saleDto.setTotalPrice(totalPrice);
        saleDto.setDiscountedPrice(discountedPrice.setScale(2, RoundingMode.HALF_UP));
        saleDto.setCampaignName(faker.company().name());
        saleDto.setCampaignId(Math.abs(random.nextLong() % 1000000));
        saleDto.setDiscountType(discountType);
        saleDto.setDiscountValue(discountValue.doubleValue());

        BigDecimal amountReceived = discountedPrice.add(BigDecimal.valueOf(Math.abs(random.nextDouble() * 500)).setScale(2, RoundingMode.HALF_UP));
        BigDecimal change = amountReceived.subtract(discountedPrice).setScale(2, RoundingMode.HALF_UP);

        saleDto.setAmountReceived(amountReceived);
        saleDto.setChange(change);
        saleDto.setPaymentMethod(PaymentMethod.values()[random.nextInt(PaymentMethod.values().length)]);
        saleDto.setCancelled(false);
        saleDto.setSaleItems(createRandomSaleItemDtos(faker, random));

        return saleDto;
    }


    private List<SaleItemDto> createRandomSaleItemDtos(Faker faker, Random random) {
        List<SaleItemDto> saleItemDtos = new ArrayList<>();
        int itemCount = random.nextInt(10) + 1;
        for (int i = 0; i < itemCount; i++) {
            SaleItemDto saleItemDto = new SaleItemDto();
            saleItemDto.setBarcode(faker.code().isbn10());
            saleItemDto.setName(faker.commerce().productName());
            saleItemDto.setQuantity(random.nextInt(10) + 1);
            saleItemDto.setSalePrice(BigDecimal.valueOf(random.nextDouble() * 100));
            saleItemDtos.add(saleItemDto);
        }
        return saleItemDtos;
    }

    private Sale convertToEntity(SaleDto saleDto) {
        Sale sale = Sale.builder()
                .cashierName(saleDto.getCashierName())
                .saleDate(saleDto.getSaleDate())
                .totalPrice(saleDto.getTotalPrice())
                .discountedPrice(saleDto.getDiscountedPrice())
                .campaignName(saleDto.getCampaignName())
                .campaignId(saleDto.getCampaignId())
                .discountType(saleDto.getDiscountType())
                .discountValue(saleDto.getDiscountValue())
                .amountReceived(saleDto.getAmountReceived())
                .change(saleDto.getChange())
                .paymentMethod(saleDto.getPaymentMethod())
                .isCancelled(saleDto.isCancelled())
                .saleItems(new ArrayList<>())
                .build();

        for (SaleItemDto saleItemDto : saleDto.getSaleItems()) {
            SaleItem saleItem = SaleItem.builder()
                    .barcode(saleItemDto.getBarcode())
                    .name(saleItemDto.getName())
                    .quantity(saleItemDto.getQuantity())
                    .salePrice(saleItemDto.getSalePrice())
                    .sale(sale)
                    .build();
            sale.getSaleItems().add(saleItem);
        }

        return sale;
    }

    private LocalDateTime randomDateTimeInLastYear(Random random) {
        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(now.minusYears(1), now);
        return now.minusDays(random.nextInt((int) days));
    }

}

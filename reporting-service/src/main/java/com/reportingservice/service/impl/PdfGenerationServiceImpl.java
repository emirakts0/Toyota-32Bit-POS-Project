package com.reportingservice.service.impl;

import com.reportingservice.dto.SaleDto;
import com.reportingservice.service.PdfGenerationService;
import com.reportingservice.utility.MarketInfo;
import com.reportingservice.utility.PdfReceiptGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PdfGenerationServiceImpl implements PdfGenerationService {

    @Value("${market.name}")
    private String marketName;
    @Value("${market.address}")
    private String marketAddress;
    @Value("${market.phone}")
    private String marketPhone;
    @Value("${market.city}")
    private String marketCity;


    @Override
    public byte[] generateReceiptPDF(SaleDto saleDto) {
        log.trace("generateReceipt method begins. SaleDto: {}", saleDto);

        MarketInfo marketInfo = new MarketInfo(marketName, marketAddress, marketPhone, marketCity);
        byte[] receiptBytes = PdfReceiptGenerator.createReceipt(saleDto, marketInfo);

        log.trace("generateReceipt method ends. SaleDto: {}", saleDto);
        return receiptBytes;
    }
}

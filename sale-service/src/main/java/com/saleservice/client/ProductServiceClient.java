package com.saleservice.client;

import com.saleservice.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service", path = "/product/search")
public interface ProductServiceClient {

    @GetMapping("/{barcode}")
    ResponseEntity<ProductDto> getProduct(@PathVariable String barcode);

}

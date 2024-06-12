package com.saleservice.client;

import com.saleservice.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "Product-service", path = "/product/search")
public interface ProductServiceClient {


    @GetMapping("/{barcode}")
    ResponseEntity<ProductDto> getProduct(@PathVariable String barcode);

}

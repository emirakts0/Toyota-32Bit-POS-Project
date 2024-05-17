package com.productservice.dto;

import lombok.Data;

@Data
public class ImageDto {
    private Long imageCode;
    private String fileName;
    private String type;
    private byte[] file;
}

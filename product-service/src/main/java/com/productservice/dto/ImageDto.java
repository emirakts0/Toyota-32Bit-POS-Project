package com.productservice.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.SpringVersion;

@Data
public class ImageDto {
    private Long imageCode;
    private String fileName;
    private String type;
    private byte[] file;
}

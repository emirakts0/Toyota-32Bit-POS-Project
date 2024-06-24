package com.productservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Random;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "images")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_code", unique = true)
    private Long imageCode;

    @Column(name = "file_name")
    private String fileName;
    private String type;

    @Lob
    private byte[] file;
    private boolean deleted;

    public Image(String fileName, String type, byte[] file) {
        this.fileName = fileName;
        this.type = type;
        this.file = file;
    }

    @PrePersist
    private void onCreate() {
        if (imageCode == null) {
            imageCode = new Random().nextLong() & Long.MAX_VALUE;
        }
    }
}

package com.reportingservice.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("receipt")
public class Receipt implements Serializable {

    @Id
    private String id;

    private String status;
    private Long saleId;
    private byte[] receiptData;

    @TimeToLive
    private Long expiration = 300L;
}

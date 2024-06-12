package com.saleservice.client;

import com.saleservice.exception.DefaultCustomException;
import com.saleservice.exception.ExceptionResponse;
import feign.Response;
import feign.codec.ErrorDecoder;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class RetreiveMessageErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder errorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        ExceptionResponse exceptionResponse = null;
        try (InputStream body = response.body().asInputStream()) {
            String responseBody = IOUtils.toString(body, StandardCharsets.UTF_8);
            String dateHeader = (String) response.headers().get("date").toArray()[0];
            LocalDateTime timeStamp = LocalDateTime.parse(dateHeader, java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME);
            HttpStatus httpStatus = HttpStatus.resolve(response.status());

            exceptionResponse = new ExceptionResponse(
                    responseBody,
                    httpStatus,
                    timeStamp
            );

        } catch (IOException exception) {
            return new RuntimeException("Failed to process the error response", exception);
        }

        switch (response.status()) {
            case 404:
                throw new NotFoundException(String.valueOf(exceptionResponse));
            default:
                return new DefaultCustomException(String.valueOf(exceptionResponse));
        }
    }
}



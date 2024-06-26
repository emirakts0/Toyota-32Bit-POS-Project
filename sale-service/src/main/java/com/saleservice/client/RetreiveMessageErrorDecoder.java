package com.saleservice.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saleservice.exception.*;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class RetreiveMessageErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder errorDecoder = new Default();
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public Exception decode(String methodKey, Response response) {
        String message = extractMessageFromBody(response);
        log.error("Error occurred for method {}: status {}, message {}", methodKey, response.status(), message);

        if (response.status() == 404) {
            return new ProductNotFoundException(message);
        } else {
            return new DefaultCustomException(message);
        }
    }


    private String extractMessageFromBody(Response response) {
        if (response.body() != null) {
            try (Response.Body body = response.body()) {
                String responseBody = new String(body.asInputStream().readAllBytes(), StandardCharsets.UTF_8);
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                JsonNode messageNode = jsonNode.get("message");
                if (messageNode != null) {
                    return messageNode.asText();
                } else {
                    return "No message field found";
                }
            } catch (IOException e) {
                return "Unknown error";
            }
        }
        return "No message";
    }
}

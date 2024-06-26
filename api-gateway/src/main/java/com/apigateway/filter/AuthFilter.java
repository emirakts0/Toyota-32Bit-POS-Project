package com.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;

/**
 * Filter to handle authorization for incoming requests in the API Gateway.
 * It uses WebClient to communicate with the authentication service and verifies
 * if the requests are authorized based on the specified paths.
 * @author Emir Aktaş
 */
@Slf4j
@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private final WebClient webClient;

    public AuthFilter(WebClient.Builder webClientBuilder, @Value("${auth.service.url}") String authServiceUrl) {
        super(Config.class);
        this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
    }


    /**
     * Applies the filter logic to the Gateway filter chain.
     *
     * @param config the configuration for the filter
     * @return a GatewayFilter instance
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String path = request.getPath().toString();
            String authPath = determineAuthPath(path);

            log.trace("apply(gateway): Processing request for path: {}", path);

            if (authPath == null) {
                log.warn("apply(gateway): Invalid request path: {}", path);
                return this.onError(exchange, "Invalid request path", HttpStatus.FORBIDDEN);
            }

            if (authHeader == null || authHeader.isEmpty()) {
                log.warn("apply(gateway): Authorization header is missing in request for path: {}", path);
                return this.onError(exchange, "Authorization header is missing in request", HttpStatus.UNAUTHORIZED);
            }

            log.debug("apply(gateway): Sending authorization request to authPath: {} with header: {}", authPath, authHeader);

            return this.webClient.post().uri(authPath)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .retrieve()
                    .bodyToMono(String.class)
                    .flatMap(response -> {
                        log.debug("apply(gateway): Authorization response received: {}", response);

                        if ("true".equalsIgnoreCase(response) || response.startsWith("name-")) {
                            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                    .header("Name", response)
                                    .build();
                            log.info("apply(gateway): Authorization successful for path: {}. Proceeding with request", path);

                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                        } else {
                            log.warn("apply(gateway): Authorization failed for path: {}. Response: {}", path, response);
                            return onError(exchange, response, HttpStatus.UNAUTHORIZED);
                        }
                    })
                    .onErrorResume( WebClientResponseException.class,
                                    e -> this.onError(exchange,
                                                      e.getResponseBodyAsString(),
                                                      HttpStatus.valueOf(e.getStatusCode().value())));
        };
    }


    /**
     * Determines the authentication path based on the request path.
     *
     * @param path the request path
     * @return the authentication path or null if the path is invalid
     */
    private String determineAuthPath(String path) {
        log.trace("determineAuthPath: Determining auth path for request path: {}", path);

        if (path.startsWith("/product")) {
            return "/auth/product";
        } else if (path.startsWith("/sale")) {
            return "/auth/sale";
        } else if (path.startsWith("/user")) {
            return "/auth/user";
        } else if(path.startsWith("/report/status/receipt")) {
            return "/auth/report/status/receipt";
        } else if (path.startsWith("/report")) {
            return "/auth/report";
        }
        else {
            return null;
        }
    }


    /**
     * Handles error responses by sending an appropriate HTTP status and error message.
     *
     * @param exchange the current server web exchange
     * @param err the error message
     * @param httpStatus the HTTP status to be set in the response
     * @return a Mono that completes the response
     */
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        log.error("onError(gateway): Handling error response. Status: {}, Error: {}", httpStatus, err);

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        byte[] bytes = err.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
    }
}

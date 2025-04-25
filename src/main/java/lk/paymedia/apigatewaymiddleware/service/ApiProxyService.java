package lk.dk.apigatewaymiddleware.service;

import lk.dk.apigatewaymiddleware.exception.TokenExpiredException;
import lk.dk.apigatewaymiddleware.util.JwtTokenUtil;
import lk.dk.apigatewaymiddleware.util.UrlWhitelistUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ApiProxyService {

    private static final Logger logger = LoggerFactory.getLogger(ApiProxyService.class);

    private final WebClient apiGatewayClient;
    private final JwtTokenUtil jwtTokenUtil;
    private final UrlWhitelistUtil urlWhitelistUtil;

    public ResponseEntity<String> forwardRequest(String path, String method, String body) {
        boolean isPublicApi = urlWhitelistUtil.isWhitelisted(path);
        logger.info("Forwarding {} request to path: {}", method, path);

        WebClient.RequestBodySpec requestSpec = apiGatewayClient.method(HttpMethod.valueOf(method))
                .uri(path);

        if (!isPublicApi) {
            String jwtToken = jwtTokenUtil.getCachedOrNewToken();
            logger.info("Private API detected. Attaching JWT token.");
            requestSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken);
        } else {
            logger.info("Public API detected. No JWT token attached.");
        }

        if (body != null && !body.isEmpty()) {
            logger.debug("Request body: {}", body);
            requestSpec.bodyValue(body);
        }

        ResponseEntity<String> response = requestSpec.retrieve()
                .onStatus(status -> status == HttpStatus.UNAUTHORIZED, clientResponse -> {
                    logger.error("Unauthorized error from WSO2: {}", clientResponse.statusCode());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new TokenExpiredException("Token expired or invalid: " + errorBody)));
                })
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    logger.error("4xx error from WSO2: {}", clientResponse.statusCode());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new RuntimeException("Client error from API Gateway: " + errorBody)));
                })
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                    logger.error("5xx error from WSO2: {}", clientResponse.statusCode());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new RuntimeException("Server error from API Gateway: " + errorBody)));
                })
                .toEntity(String.class)
                .block();

        if (response != null) {
            logger.info("Received response with status: {}", response.getStatusCode());
        } else {
            logger.warn("Received null response from WSO2.");
        }
        return response;
    }
}

package lk.dk.apigatewaymiddleware.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Configuration
public class WebClientConfig {

    @Value("${api-gateway.base-url}")
    private String apiGatewayBaseUrl;

    @Bean
    public WebClient apiGatewayClient() {
        return WebClient.builder()
                .baseUrl(apiGatewayBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}

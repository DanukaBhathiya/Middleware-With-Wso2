package lk.dk.apigatewaymiddleware.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class Wso2TokenService {

    private static final Logger logger = LoggerFactory.getLogger(Wso2TokenService.class);

    @Value("${wso2.token-url}")
    private String tokenUrl;

    @Value("${wso2.client-id}")
    private String clientId;

    @Value("${wso2.client-secret}")
    private String clientSecret;

    public String getAccessToken() {
        WebClient webClient = WebClient.builder().baseUrl(tokenUrl).build();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");

        return webClient.post()
                .uri("")
                .headers(headers -> {
                    String auth = clientId + ":" + clientSecret;
                    String encoded = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
                    headers.setBasicAuth(encoded);
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                })
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .doOnError(e -> logger.error("Error retrieving access token from WSO2", e))
                .map(TokenResponse::getAccessToken)
                .block();
    }

    private record TokenResponse(String access_token) {
        public String getAccessToken() {
            return access_token;
        }
    }
}

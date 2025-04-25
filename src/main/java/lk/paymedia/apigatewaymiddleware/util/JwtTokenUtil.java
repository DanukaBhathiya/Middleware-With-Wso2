package lk.dk.apigatewaymiddleware.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtTokenUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    private final String secretKey;
    private final long expirationMinutes;
    private final Map<String, CachedToken> tokenCache = new ConcurrentHashMap<>();

    public JwtTokenUtil(@Value("${middleware.jwt.secret}") String secretKey,
                        @Value("${middleware.jwt.expirationMinutes}") long expirationMinutes) {
        this.secretKey = secretKey;
        this.expirationMinutes = expirationMinutes;
    }

    public String getCachedOrNewToken() {
        CachedToken cachedToken = tokenCache.get("internal-jwt");

        if (cachedToken != null && cachedToken.expiry().isAfter(Instant.now())) {
            logger.info("Using cached JWT token. Expiry: {}", cachedToken.expiry());
            return cachedToken.token();
        }

        logger.info("Cached token expired or not found. Generating new JWT token.");
        String newToken = generateInternalToken();
        Instant newExpiry = Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES);
        tokenCache.put("internal-jwt", new CachedToken(newToken, newExpiry));

        logger.info("Generated new JWT token. Expiry: {}", newExpiry);
        return newToken;
    }

    private String generateInternalToken() {
        try {
            logger.debug("Generating JWT token internally.");
            return Jwts.builder()
                    .setSubject("middleware-service")
                    .setIssuedAt(new Date())
                    .setExpiration(Date.from(Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES)))
                    .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                    .compact();
        } catch (Exception e) {
            logger.error("Failed to generate JWT token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate JWT token.");
        }
    }

    private record CachedToken(String token, Instant expiry) {}
}

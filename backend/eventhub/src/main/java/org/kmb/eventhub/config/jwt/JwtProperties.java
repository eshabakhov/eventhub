package org.kmb.eventhub.config.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private long expirationMs = 1000L * 10; // access token expiration
    private long refreshExpirationMs = 1000L * 60 * 60 * 24; // refresh token expiration (например, 24 часа)
}


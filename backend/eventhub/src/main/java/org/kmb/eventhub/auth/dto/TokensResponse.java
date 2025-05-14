package org.kmb.eventhub.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokensResponse {
    private String accessToken;
    private String refreshToken;
}

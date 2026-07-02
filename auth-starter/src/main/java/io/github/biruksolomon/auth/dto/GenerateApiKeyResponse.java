package io.github.biruksolomon.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class GenerateApiKeyResponse {
    private Long id;
    private String name;
    private String apiKey; // Plain key (shown only once)
    private String keyPrefix; // e.g., "sk_live_"
    private String expiresAt;
    private String createdAt;
}

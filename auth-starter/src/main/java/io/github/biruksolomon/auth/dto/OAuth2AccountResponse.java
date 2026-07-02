package io.github.biruksolomon.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OAuth2AccountResponse {
    private Long id;
    private String provider;
    private String email;
    private String displayName;
    private String profilePictureUrl;
    private String connectedAt;
    private String lastUsedAt;
}

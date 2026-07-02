package io.github.biruksolomon.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ApiKeyResponse {
    private Long id;
    private String name;
    private String keyPrefix;
    private boolean active;
    private List<String> permissions;
    private String expiresAt;
    private String lastUsedAt;
    private String createdAt;
    private String revokedAt;
}

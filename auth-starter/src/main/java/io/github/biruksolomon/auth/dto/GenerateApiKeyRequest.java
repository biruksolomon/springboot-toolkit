package io.github.biruksolomon.auth.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class GenerateApiKeyRequest {
    private String name;
    private LocalDateTime expiresAt;
    private Set<Long> permissionIds;
}

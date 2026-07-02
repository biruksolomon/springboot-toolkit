package io.github.biruksolomon.auth.dto;

import lombok.Data;

@Data
public class GoogleLoginRequest {
    private String googleId;
    private String email;
    private String displayName;
    private String pictureUrl;
}

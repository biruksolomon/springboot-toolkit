package io.github.biruksolomon.auth.controller;

import io.github.biruksolomon.auth.dto.AuthResponse;
import io.github.biruksolomon.auth.dto.GoogleLoginRequest;
import io.github.biruksolomon.auth.service.OAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {
    private final OAuth2Service oauth2Service;

    @PostMapping("/google/callback")
    public ResponseEntity<AuthResponse> googleCallback(@RequestBody GoogleLoginRequest request) {
        AuthResponse response = oauth2Service.handleGoogleLogin(
                request.getGoogleId(),
                request.getEmail(),
                request.getDisplayName(),
                request.getPictureUrl()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google/link")
    public ResponseEntity<Void> linkGoogleAccount(@RequestBody GoogleLoginRequest request) {
        // Requires authenticated user context
        // Will be implemented with user extraction from JWT
        return ResponseEntity.ok().build();
    }

    @PostMapping("/google/unlink")
    public ResponseEntity<Void> unlinkGoogleAccount() {
        // Requires authenticated user context
        // Will be implemented with user extraction from JWT
        return ResponseEntity.ok().build();
    }
}

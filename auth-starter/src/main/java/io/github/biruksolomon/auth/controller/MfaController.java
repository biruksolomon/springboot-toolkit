package io.github.biruksolomon.auth.controller;

import io.github.biruksolomon.auth.service.MfaService;
import io.github.biruksolomon.auth.service.MfaService.MfaSetupDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mfa")
@RequiredArgsConstructor
public class MfaController {
    private final MfaService mfaService;

    @PostMapping("/setup")
    public ResponseEntity<MfaSetupDto> setupMfa(Authentication auth) {
        Long userId = Long.parseLong(auth.getName());
        MfaSetupDto setup = mfaService.initiateMfaSetup(userId);
        return ResponseEntity.ok(setup);
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verifyMfa(
            Authentication auth,
            @RequestParam String totpCode) {
        Long userId = Long.parseLong(auth.getName());
        mfaService.verifyMfaSetup(userId, totpCode);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-login")
    public ResponseEntity<Void> verifyMfaLogin(
            @RequestParam Long userId,
            @RequestParam String totpCode) {
        mfaService.verifyMfaLogin(userId, totpCode);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public ResponseEntity<MfaStatusDto> getMfaStatus(Authentication auth) {
        Long userId = Long.parseLong(auth.getName());
        boolean enabled = mfaService.isMfaEnabled(userId);
        return ResponseEntity.ok(MfaStatusDto.builder().enabled(enabled).build());
    }

    @PostMapping("/disable")
    public ResponseEntity<Void> disableMfa(Authentication auth) {
        Long userId = Long.parseLong(auth.getName());
        mfaService.disableMfa(userId);
        return ResponseEntity.ok().build();
    }

    @lombok.Data
    @lombok.Builder
    public static class MfaStatusDto {
        private boolean enabled;
    }
}

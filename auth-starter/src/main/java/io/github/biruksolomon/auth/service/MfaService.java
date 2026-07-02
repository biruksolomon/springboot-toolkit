package io.github.biruksolomon.auth.service;

import io.github.biruksolomon.auth.domain.MfaSetup;
import io.github.biruksolomon.auth.domain.User;
import io.github.biruksolomon.auth.exception.AuthException;
import io.github.biruksolomon.auth.repository.MfaSetupRepository;
import io.github.biruksolomon.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MfaService {
    private final MfaSetupRepository mfaSetupRepository;
    private final UserRepository userRepository;

    @Transactional
    public MfaSetupDto initiateMfaSetup(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException.UserNotFound(userId));

        MfaSetup existing = mfaSetupRepository.findByUserId(userId).orElse(null);
        if (existing != null && existing.isEnabled()) {
            throw new AuthException("MFA already enabled for user", "MFA_ALREADY_ENABLED");
        }

        String secret = generateSecret();
        List<String> backupCodes = generateBackupCodes();

        MfaSetup mfa = MfaSetup.builder()
                .user(user)
                .secret(secret)
                .enabled(false)
                .verified(false)
                .backupCodes(backupCodes)
                .usedBackupCodes(new ArrayList<>())
                .build();

        mfa = mfaSetupRepository.save(mfa);
        log.info("MFA setup initiated for user: {}", userId);

        return MfaSetupDto.builder()
                .secret(secret)
                .qrCode(generateQrCode(user.getEmail(), secret))
                .backupCodes(backupCodes)
                .build();
    }

    @Transactional
    public boolean verifyMfaSetup(Long userId, String totpCode) {
        MfaSetup mfa = mfaSetupRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthException("MFA not initiated", "MFA_NOT_INITIATED"));

        if (!verifyTotp(mfa.getSecret(), totpCode)) {
            throw new AuthException("Invalid TOTP code", "INVALID_TOTP");
        }

        mfa.setVerified(true);
        mfa.setEnabled(true);
        mfa.setEnabledAt(LocalDateTime.now());
        mfaSetupRepository.save(mfa);

        log.info("MFA verified for user: {}", userId);
        return true;
    }

    @Transactional
    public boolean verifyMfaLogin(Long userId, String totpCode) {
        MfaSetup mfa = mfaSetupRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthException("MFA not setup", "MFA_NOT_SETUP"));

        if (!mfa.isEnabled() || !mfa.isVerified()) {
            throw new AuthException("MFA not properly enabled", "MFA_NOT_ENABLED");
        }

        // Try TOTP first
        if (verifyTotp(mfa.getSecret(), totpCode)) {
            return true;
        }

        // Try backup code
        if (mfa.getBackupCodes() != null && mfa.getBackupCodes().contains(totpCode)
                && !mfa.getUsedBackupCodes().contains(totpCode)) {
            mfa.getUsedBackupCodes().add(totpCode);
            mfaSetupRepository.save(mfa);
            log.info("MFA verified via backup code for user: {}", userId);
            return true;
        }

        throw new AuthException("Invalid MFA code", "INVALID_MFA_CODE");
    }

    @Transactional
    public void disableMfa(Long userId) {
        MfaSetup mfa = mfaSetupRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthException("MFA not setup", "MFA_NOT_SETUP"));

        mfa.setEnabled(false);
        mfa.setVerified(false);
        mfa.getUsedBackupCodes().clear();
        mfaSetupRepository.save(mfa);

        log.info("MFA disabled for user: {}", userId);
    }

    public boolean isMfaEnabled(Long userId) {
        return mfaSetupRepository.existsByUserIdAndEnabledTrue(userId);
    }

    private String generateSecret() {
        byte[] buffer = new byte[20];
        new Random().nextBytes(buffer);
        return Base64.getEncoder().encodeToString(buffer);
    }

    private List<String> generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            byte[] buffer = new byte[4];
            new Random().nextBytes(buffer);
            codes.add(Base64.getEncoder().encodeToString(buffer));
        }
        return codes;
    }

    private String generateQrCode(String email, String secret) {
        return String.format("otpauth://totp/%s?secret=%s&issuer=auth-core", email, secret);
    }

    private boolean verifyTotp(String secret, String code) {
        // TODO: Implement TOTP verification using totp library
        // Current placeholder - always returns false for safety
        // Production: Use dev.samstevens.totp.code.CodeGenerator
        return false;
    }

    @lombok.Data
    @lombok.Builder
    public static class MfaSetupDto {
        private String secret;
        private String qrCode;
        private List<String> backupCodes;
    }
}

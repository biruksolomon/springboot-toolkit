package io.github.biruksolomon.auth.service;

import io.github.biruksolomon.auth.exception.AuthException;
import io.github.biruksolomon.auth.properties.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PasswordValidationService {

    private final AuthProperties authProperties;

    public void validate(String password) {
        if (password == null || password.isEmpty()) {
            throw new AuthException.InvalidPassword("Password cannot be empty");
        }

        if (password.length() < authProperties.getPassword().getMinLength()) {
            throw new AuthException.InvalidPassword(
                    "Password must be at least " + authProperties.getPassword().getMinLength() + " characters long"
            );
        }

        if (authProperties.getPassword().isRequireSpecialChars()) {
            if (!hasSpecialCharacter(password)) {
                throw new AuthException.InvalidPassword("Password must contain at least one special character");
            }
        }
    }

    private boolean hasSpecialCharacter(String password) {
        return Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>?/\\\\|`~]").matcher(password).find();
    }
}

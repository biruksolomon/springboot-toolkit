package io.github.biruksolomon.auth.autoconfigure;

import io.github.biruksolomon.auth.config.SecurityConfig;
import io.github.biruksolomon.auth.controller.AuthController;
import io.github.biruksolomon.auth.properties.AuthProperties;
import io.github.biruksolomon.auth.repository.RefreshTokenRepository;
import io.github.biruksolomon.auth.repository.UserRepository;
import io.github.biruksolomon.auth.security.JwtAuthenticationFilter;
import io.github.biruksolomon.auth.security.JwtProvider;
import io.github.biruksolomon.auth.service.AuthService;
import io.github.biruksolomon.auth.service.PasswordValidationService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.password.PasswordEncoder;

@AutoConfiguration
@ComponentScan(basePackages = "io.github.biruksolomon.auth")
public class AuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtProvider jwtProvider(AuthProperties authProperties) {
        return new JwtProvider(authProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordValidationService passwordValidationService(AuthProperties authProperties) {
        return new PasswordValidationService(authProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthService authService(UserRepository userRepository,
                                   RefreshTokenRepository refreshTokenRepository,
                                   JwtProvider jwtProvider,
                                   PasswordValidationService passwordValidationService,
                                   AuthProperties authProperties,
                                   PasswordEncoder passwordEncoder) {
        return new AuthService(userRepository, refreshTokenRepository, jwtProvider, passwordEncoder, authProperties, passwordValidationService);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter(

            JwtProvider jwtProvider,
            UserRepository userRepository
    ) {
        return new JwtAuthenticationFilter(jwtProvider, userRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityConfig securityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        return new SecurityConfig(jwtAuthenticationFilter);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthController authController(AuthService authService) {
        return new AuthController(authService);
    }
}

package io.github.biruksolomon.auth.security;

import io.github.biruksolomon.auth.domain.User;
import io.github.biruksolomon.auth.properties.AuthProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtProvider {

    private final AuthProperties authProperties;

    private SecretKey getSigningKey() {
        byte[] keyBytes = authProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("roles", user.getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .collect(Collectors.toList()));

        return generateToken(user.getId().toString(), claims, authProperties.getJwt().getAccessTokenExpiry().toMillis());
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        return generateToken(user.getId().toString(), claims, authProperties.getJwt().getRefreshTokenExpiry().toMillis());
    }

    private String generateToken(String subject, Map<String, Object> claims, long expiryMillis) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiryMillis);

        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .issuer(authProperties.getJwt().getIssuer())
                .signWith(getSigningKey())
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return Long.parseLong(claims.getSubject());
        } catch (JwtException e) {
            log.warn("Failed to get user ID from token", e);
            return null;
        }
    }

    public Claims parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}

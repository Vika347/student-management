package ru.itmo.studentmanagement.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import ru.itmo.studentmanagement.user.entity.User;
import jakarta.enterprise.context.ApplicationScoped;

import javax.crypto.SecretKey;
import java.util.Date;

// Создание и проверка JWT-токенов
@ApplicationScoped
public class JwtService {

    // Секретный ключ JWT из переменной окружения
    private final SecretKey key = loadKey();

    private static SecretKey loadKey() {
        String secret = System.getenv("JWT_SECRET");

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "Не задана переменная окружения JWT_SECRET"
            );
        }

        byte[] keyBytes = secret.getBytes(
                java.nio.charset.StandardCharsets.UTF_8
        );

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET должен содержать минимум 32 байта"
            );
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    private final long accessTokenExpiration = 15 * 60 * 1000;
    private final long refreshTokenExpiration = 7 * 24 * 60 * 60 * 1000;

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("type", "access")
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + accessTokenExpiration
                ))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + refreshTokenExpiration
                ))
                .signWith(key)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    // Проверяем, что access-токен
    public boolean validateAccessToken(String token) {
        return validateTokenType(token, "access");
    }

    // Проверяем, что это  refresh-токен
    public boolean validateRefreshToken(String token) {
        return validateTokenType(token, "refresh");
    }

    // Общая проверка подписи, срока действия и типа токена
    private boolean validateTokenType(String token, String expectedType) {
        try {
            Claims claims = getClaims(token);
            return expectedType.equals(claims.get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }
}

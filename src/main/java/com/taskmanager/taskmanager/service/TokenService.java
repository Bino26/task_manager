package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.response.TokenResponse;
import com.taskmanager.taskmanager.dto.response.UserResponse;
import com.taskmanager.taskmanager.entity.RefreshToken;
import com.taskmanager.taskmanager.entity.User;
import com.taskmanager.taskmanager.exception.custom.CustomNotFoundException;
import com.taskmanager.taskmanager.exception.custom.TokenExpiredException;
import com.taskmanager.taskmanager.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Value("${jwt.secret-key}")
    public String token;
    @Value("${jwt.access-expiration-time}")
    public Integer accessExpiration;
    @Value("${jwt.refresh-expiration-time}")
    public Integer refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    public TokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String generateAccessToken(String username) {
        logger.info("Generating access token for user: {}", username);
        Map<String, Object> claims = new HashMap<>();
        return createAccessToken(claims, username);
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        String email = getEmailFromToken(token);
        Date expirationDate = getExpirationDateFromToken(token);
        logger.debug("Validating token for email: {}, Expiration Date: {}", email, expirationDate);
        return email.equals(userDetails.getUsername()) && expirationDate.after(new Date());
    }

    public String getEmailFromToken(String token) {
        logger.debug("Extracting email from token");
        return getAllClaimsFromToken(token).getSubject();
    }

    public Date getExpirationDateFromToken(String token) {
        logger.debug("Extracting expiration date from token");
        return getAllClaimsFromToken(token).getExpiration();
    }

    public String createRefreshToken(User user) {
        logger.info("Creating refresh token for user: {}", user.getId());
        RefreshToken refreshToken = refreshTokenRepository.findByUserIdAndDeletedAtNull(user.getId());

        if (refreshToken != null) {
            logger.debug("Existing refresh token found for user, soft deleting it");
            refreshToken.softDelete();
        }
        refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Date.from(Instant.now().plusSeconds(refreshExpiration)))
                .build();

        refreshTokenRepository.save(refreshToken);
        logger.info("New refresh token created for user: {}", user.getId());
        return refreshToken.getToken();
    }

    public TokenResponse validateRefreshToken(String refreshToken) {
        logger.info("Validating refresh token: {}", refreshToken);
        RefreshToken token = getRefreshToken(refreshToken);

        if (token.getExpiryDate().before(new Date())) {
            logger.warn("Refresh token has expired: {}", refreshToken);
            token.softDelete();
            throw new TokenExpiredException("Refresh token has expired");
        }

        token.softDelete();
        refreshTokenRepository.save(token);

        logger.info("Refresh token validated and soft deleted: {}", refreshToken);
        return new TokenResponse(
                UserResponse.from(token.getUser()),
                generateAccessToken(token.getUser().getEmail()),
                createRefreshToken(token.getUser())
        );
    }

    public void deleteRefreshTokenByUserId(UUID id) {
        logger.info("Deleting refresh token for user: {}", id);
        RefreshToken refreshToken = refreshTokenRepository.findByUserIdAndDeletedAtNull(id);
        if (refreshToken != null) {
            logger.debug("Found refresh token for user: {}. Soft deleting it", id);
            refreshToken.softDelete();
            refreshTokenRepository.save(refreshToken);
        } else {
            logger.warn("No refresh token found for user: {}", id);
        }
    }

    public void deleteRefreshToken(String refreshToken) {
        logger.info("Deleting refresh token: {}", refreshToken);
        RefreshToken token = getRefreshToken(refreshToken);
        token.softDelete();
        refreshTokenRepository.save(token);
    }

    private Claims getAllClaimsFromToken(String token) {
        logger.debug("Parsing JWT token to get claims");
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String createAccessToken(Map<String, Object> claims, String email) {
        logger.info("Creating access token for email: {}", email);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Key getKey() {
        logger.debug("Getting key for JWT signing");
        byte[] keyBytes = Decoders.BASE64.decode(token);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private RefreshToken getRefreshToken(String token) {
        return refreshTokenRepository.findByTokenAndDeletedAtNull(token)
                .orElseThrow(() -> {
                    logger.error("Refresh token not found: {}", token);
                    return new CustomNotFoundException("Refresh token not found");
                });
    }
}
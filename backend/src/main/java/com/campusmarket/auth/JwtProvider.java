package com.campusmarket.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类 — 负责 token 的生成、解析、验证
 *
 * JWT 流程回顾：
 * 1. 用户登录成功 → generateToken(userId) → 返回 token 给前端
 * 2. 前端每次请求带 Authorization: Bearer <token>
 * 3. 后端 validateToken(token) → 通过则取出 userId
 *
 * @Value 注解：从 application.yml 读取配置值
 *   campus-market:
 *     jwt:
 *       secret: campus-market-secret-key-2024-change-in-production
 *       expiration: 604800000  (7天 = 7 * 24 * 60 * 60 * 1000 毫秒)
 */
@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long expiration;

    // 构造器注入：启动时读取 yml 配置，初始化密钥和过期时间
    public JwtProvider(
            @Value("${campus-market.jwt.secret}") String secret,
            @Value("${campus-market.jwt.expiration}") long expiration) {
        // HMAC-SHA256 算法要求密钥至少 256 bits（32 字节），不够则补足
        String paddedSecret = secret.length() < 32
                ? String.format("%-32s", secret).replace(' ', '0')
                : secret;
        this.secretKey = Keys.hmacShaKeyFor(paddedSecret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /**
     * 生成 JWT token
     *
     * Jwts.builder()
     *   .subject(userId)  — 主题（存用户 ID）
     *   .issuedAt(now)    — 签发时间
     *   .expiration(...)  — 过期时间
     *   .signWith(key)    — HMAC-SHA256 签名
     *   .compact()        — 压缩为字符串
     */
    public String generateToken(Long userId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 从 token 中提取用户 ID
     */
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    /**
     * 从 token 中提取角色
     */
    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role", String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证 token 是否有效
     * 如果 token 被篡改、过期、格式不对，都会抛异常 → 返回 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

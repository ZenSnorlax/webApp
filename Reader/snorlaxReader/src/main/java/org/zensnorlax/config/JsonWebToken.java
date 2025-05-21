package org.zensnorlax.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/3/24 12:00
 */

@Component
public class JsonWebToken {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration-time}")
    private long expirationMillis;

    // 生成 JWT
    public String generateToken(Map<String, Object> claims) {
        long now = System.currentTimeMillis();
        Date expiration = new Date(now + expirationMillis);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // 验证 JWT
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 解析 JWT 获取 Claims
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 获取签名密钥（将字符串转换为 Key 对象）
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 根据 claim 名称提取特定的信息
    public <T> T extractClaim(String token, String claimName, Class<T> tClass) {
        Claims claims = extractAllClaims(token);
        return claims.get(claimName, tClass);
    }
}
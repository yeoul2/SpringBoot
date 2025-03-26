package com.example.back.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.back.model.User;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Log4j2
@Service
public class JWTService {

    @Value("${spring.security.jwt.secret}") // ✅ application.yml에서 불러오기
    private String secretKey;

    @Value("${spring.security.jwt.expiration}") // ✅ 만료 시간 가져오기
    private long expiration;

    
    /* @Value("${spring.security.jwt.refresh-expiration}") // ✅ 리프레시 토큰 만료 시간 추가
    private long refreshExpiration; */
    

    @PostConstruct
    public void logSecretKey() {
        log.info("✅ Loaded JWT Secret Key: " + secretKey);
        log.info("✅ JWT Expiration Time: " + expiration);
        // log.info("✅ Refresh Token Expiration Time: " + refreshExpiration);
    }

    public String generateToken(UserDetails userDetails) {
        log.info("@@generateToken@@");
        log.info(userDetails.getAuthorities());
        return Jwts.builder()
                .setSubject(userDetails.getUsername()) // JWT subject 설정
                .setIssuedAt(new Date(System.currentTimeMillis())) // 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // 만료 시간
                .signWith(getSignKey(), SignatureAlgorithm.HS256) // ✅ secret key 사용
                .claim("roles", userDetails.getAuthorities()) // 역할 정보 추가
                .compact();
    }

    public String generateRefreshToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        log.info("@@refreshToken@@");
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 7일
                //.setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    // ✅ application.yml에서 설정한 secret key를 사용
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey); // ✅ secret key를 Base64 디코딩
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey()) // ✅ secret key 사용
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ✅ 토큰이 만료되었는지 확인하는 메서드 추가
    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // ✅ User 타입을 받는 기존 메서드
    public boolean isTokenValid(String token, User user) {
        final String username = extractUserName(token);
        return (username.equals(user.getUser_name()) && !isTokenExpired(token)); // 🔥 이제 오류 안 남!
    }

    // ✅ UserDetails 타입도 받을 수 있도록 오버로드 추가
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUserName(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token)); // 🔥 이제 오류 안 남!
    }
}
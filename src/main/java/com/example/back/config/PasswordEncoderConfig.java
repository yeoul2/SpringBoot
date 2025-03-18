package com.example.back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {
    /* @Bean
    public PasswordEncoder passwordEncoder() {
      // 기본 알고리즘을 "bcrypt"로 설정
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", new BCryptPasswordEncoder());

        // DelegatingPasswordEncoder 생성 (기본값: bcrypt)
        return new DelegatingPasswordEncoder("bcrypt", encoders);
    } */

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

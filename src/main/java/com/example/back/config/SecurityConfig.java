package com.example.back.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

// 테스트위해서 모든 권한 허용
@Configuration
@EnableWebSecurity //시큐어 어노테이션 활성화
@RequiredArgsConstructor
public class SecurityConfig {
   @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http
              .csrf(csrf -> csrf.disable())  // CSRF 보호 비활성화
              .authorizeHttpRequests(auth -> auth
                      .anyRequest().authenticated()
              )
              .httpBasic(basic -> {
              });  // Basic 인증 활성화

      return http.build();
   }
}




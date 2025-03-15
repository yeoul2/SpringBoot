package com.example.back.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
// Configuration 과 Bean 어노테이션은 쌍으로 사용함
// Cors이슈를 필터를 사용하여 해결해 보기
import org.springframework.web.filter.CorsFilter;
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedHeader("*");
        config.setAllowCredentials(true); // ✅ withCredentials를 사용하려면 반드시 true
        config.addAllowedOrigin("http://localhost:6006");
        config.addAllowedOrigin("https://nid.naver.com"); // 네이버 OAuth 요청 허용
        config.setAllowCredentials(true); // ✅ withCredentials 활성화
        config.addAllowedMethod("*");
        config.addExposedHeader("Authorization"); // ✅ Authorization 헤더 보존 설정
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
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
        config.addAllowedHeader("*"); //모든 header에 응답을 허용
        config.addAllowedOrigin("http://localhost:6006/");//ip에 응답을 허용함
        config.addAllowedMethod("*");//모든 POST, GET, PUT, DELETE Rest API 허락할께
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

package com.example.back.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    // @Value 어노테이션을 사용하여 yml에서 사용
    @Value("${spring.mail.gmail.username}")  
    private String gmailUsername;

    @Value("${spring.mail.gmail.password}")  
    private String gmailPassword;

    @Value("${spring.mail.naver.username}")  
    private String naverUsername;

    @Value("${spring.mail.naver.password}")  
    private String naverPassword;

    @Bean
    public JavaMailSender javaMailSender() {
        return new JavaMailSenderImpl();
    }


    // 메일 제공업체(Gmail 또는 Naver)에 따라 적절한 설정 메소드
    public JavaMailSender getMailSender(String provider) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        Properties props = mailSender.getJavaMailProperties();

        if ("gmail".equalsIgnoreCase(provider)) {
            mailSender.setHost("smtp.gmail.com"); //TLS 사용
            mailSender.setPort(587);
            mailSender.setUsername(gmailUsername);
            mailSender.setPassword(gmailPassword);
        } else if ("naver".equalsIgnoreCase(provider)) {
            mailSender.setHost("smtp.naver.com"); //TLS 사용
            mailSender.setPort(587);
            mailSender.setUsername(naverUsername);
            mailSender.setPassword(naverPassword);
        } else {
            throw new IllegalArgumentException("지원되지 않는 이메일 제공업체: " + provider);
        }

        // SMTP 설정 적용
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust", mailSender.getHost());

        // SMTP 설정을 mailSender 객체에 적용한 후 반환
        mailSender.setJavaMailProperties(props);
        return mailSender;
    }
}

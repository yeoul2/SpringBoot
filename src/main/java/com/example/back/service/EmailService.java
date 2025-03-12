package com.example.back.service;

import com.example.back.config.MailConfig;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final MailConfig mailConfig;  // MailConfig 주입

    public void sendEmail(String provider, String to, String subject, String text) {
        try {
            log.info("📩 이메일 전송 요청 - 제공업체: {}, 받는 사람: {}", provider, to);

            JavaMailSender mailSender = mailConfig.getMailSender(provider);  // MailConfig 사용

            // JavaMailSender를 JavaMailSenderImpl로 캐스팅하여 getUsername() 사용 가능
            // 메일을 보내는 계정의 이메일 주소가 반환됨
            String fromEmail = ((JavaMailSenderImpl) mailSender).getUsername();

            // 이메일 내용을 구성
            MimeMessage message = mailSender.createMimeMessage();
            // 이메일을 편리하게 구성 
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromEmail); // 발신자
            helper.setTo(to); // 수신자
            helper.setSubject(subject); // 제목 
            helper.setText(text); // 본문 내용

            // 이메일 전송
            mailSender.send(message);

            log.info("✅ 이메일 전송 완료 - 제공업체: {}, 받는 사람: {}", provider, to);

        } catch (Exception e) {
            log.error("❌ 이메일 전송 실패 - 제공업체: {}, 오류: {}", provider, e.getMessage());
            throw new RuntimeException("이메일 전송 중 오류 발생: " + e.getMessage(), e);
        }
    }
}

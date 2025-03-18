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

    private final MailConfig mailConfig;  // ✅ MailConfig 주입

    // ✅ 이메일 도메인에 따라 적절한 SMTP 제공업체 선택 (자동 결정)
    public String determineProvider(String userEmail) {
        if (userEmail.endsWith("@naver.com")) {
            return "naver";
        } else if (userEmail.endsWith("@gmail.com")) {
            return "gmail";
        } else {
            return "gmail"; // 기본 제공업체를 Gmail로 설정
        }
    }

    // ✅ 기본 메일 제공업체(Gmail)로 전송
    public void sendEmail(String to, String subject, String text) {
        String provider = determineProvider(to); // 수신자 이메일에 맞는 smtp 선택
        sendEmail(provider, to, subject, text); // 기본 제공업체를 Gmail로 설정
    }

    // ✅ 특정 제공업체(Naver, Kakao 등) 선택 가능
    public void sendEmail(String provider, String to, String subject, String text) {
            try {
                log.info("📩 이메일 전송 요청 - 제공업체: {}, 받는 사람: {}", provider, to);

            JavaMailSender mailSender = mailConfig.getMailSender(provider);
            String fromEmail = ((JavaMailSenderImpl) mailSender).getUsername();

            // 이메일 내용 구성
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            mailSender.send(message);

            log.info("✅ 이메일 전송 완료 - 제공업체: {}, 받는 사람: {}", provider, to);

            } catch (Exception e) {
                log.error("❌ 이메일 전송 실패 - 제공업체: {}, 오류: {}", provider, e.getMessage());
                throw new RuntimeException("이메일 전송 중 오류 발생: " + e.getMessage(), e);
            }
        }
}

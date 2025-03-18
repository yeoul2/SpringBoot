package com.example.back.service;

import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.back.dao.UserDao;
import com.example.back.model.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserDao userDao;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

	// 🔹 UserDetailsService 메서드 정의
	public UserDetailsService userDetailsService() {
		return username -> {
			UserDetails userDetails = userDao.findByUsername(username);
			if (userDetails == null) {
				throw new UsernameNotFoundException("❌ 해당 유저가 없습니다: " + username);
			}
			return userDetails;
		};
	}

	// ✅ 사용자 정보를 반환하는 메서드 추가 (AuthController에서 필요)
	public User findByUsername(String userId) {
		return userDao.findByUsername(userId); // ✅ UserDao에서 데이터 조회
	}

	// 아이디 중복 확인 메서드
	public boolean isUsernameAvailable(String userId) {
		return userDao.isUsernameAvailable(userId);
	}

    // ✅ 이메일 중복 여부 확인 (USER 계정 기준)
    public boolean isEmailDuplicated(String userEmail) {
        int count = userDao.countByEmail(userEmail); // 이미 존재하는 이메일 개수 조회
        return count > 0; // 1개 이상이면 중복으로 처리
    }

    // 이메일 인증 여부 확인
    public boolean isEmailVerified(String userEmail) {
        Boolean verified = userDao.isEmailVerified(userEmail);
        return verified != null && verified;
    }

    // 아이디찾기
    public String findUserIdByNameAndEmail(String user_name, String user_email) {
        return userDao.findUserIdByNameAndEmail(user_name, user_email);
    }

    // 비번 찾기
    public boolean processFindPassword(String user_id, String user_email) {
        log.info("🔍 비밀번호 찾기 요청 - userId: {}, userEmail: {}", user_id, user_email);
        
        Integer user_no = userDao.findUserPwByIdAndEmail(user_id, user_email);

        if (user_no == null) {
            log.error("❌ 사용자 정보를 찾을 수 없음! (user_id: {}, user_email: {})", user_id, user_email);
            return false; // 사용자가 존재하지 않음
        }

        log.info("✅ 사용자 찾음! user_no: {}", user_no);

        String tempPassword = generateTempPassword(); // ✅ 8자리 임시 비밀번호 생성
        String encryptedPassword = passwordEncoder.encode(tempPassword); // ✅ 비밀번호 암호화

        userDao.updatePassword(user_no, encryptedPassword); // ✅ DB에 암호화된 비밀번호 저장
        log.info("🔍 업데이트하려는 user_no: {}, 암호화된 비밀번호: {}", user_no, encryptedPassword);


        // ✅ 이메일 전송 (기본 제공업체 Gmail)
        emailService.sendEmail(user_email, "임시 비밀번호 발급 안내", 
                "임시 비밀번호: " + tempPassword + "\n로그인 후 비밀번호를 변경해주세요.");

        return true;
    } 

    // ✅ 8자리 랜덤 비밀번호 생성
    private String generateTempPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    } 

}    




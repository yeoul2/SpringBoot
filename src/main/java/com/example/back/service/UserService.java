package com.example.back.service;

import com.example.back.dao.UserDao;
import com.example.back.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserDao userDao;

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

	// 아이디 찾기를 위한 메서드
	public String findUserIdByEmail(String user_email) {
		User user = userDao.findByEmail(user_email);
		if (user == null) {
			throw new RuntimeException("해당 이메일로 등록된 아이디가 없습니다.");
		}
		return user.getUser_id();
	}

	// 이메일 중복 확인
	public boolean isEmailExists(String user_email) {
		return userDao.userExists(user_email);
	}

	@Transactional
	public void updateUserRole(User user) {
		//userDao.updateRoleByEmail(user);  // MyBatis의 updateRoleByEmail 호출
	}
}

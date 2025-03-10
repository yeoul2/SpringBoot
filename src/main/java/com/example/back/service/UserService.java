package com.example.back.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.example.back.dao.UserDao;
import com.example.back.model.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


@Service
@Log4j2
@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;

    public UserDetailsService userDetailsService() {
        return username -> {
            UserDetails userDetails = userDao.findByUsername(username);
            if(userDetails == null) {
                throw new UsernameNotFoundException("❌ 해당 유저가 없습니다: " + username);
            }
            return userDetails;
        };
    }

    // ✅ 사용자 정보를 반환하는 메서드 추가 (AuthController에서 필요)
    public User findByUsername(String userId) {
        return userDao.findByUsername(userId); // ✅ UserDao에서 데이터 조회
    }
}


package com.example.back.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.example.back.dao.UserDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
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
}


package com.example.back.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.example.back.model.SignupRequest;
import com.example.back.model.User;

import org.mybatis.spring.SqlSessionTemplate;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository
public class UserDao {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    //리액트 로그인 화면에서 username과 비번을 입력했을 때 호출되는 메소드 입니다.
    public User findByUsername(String user_id) {
        log.info("🔍 사용자 조회 시도: " + user_id); // user_id가 제대로 전달되는지 확인
        User user = sqlSessionTemplate.selectOne("findByUsername", user_id);
        if (user == null) {
            log.warn("사용자 정보가 없습니다: " + user_id); // 쿼리 결과가 null인 경우 경고 로그
        } else {
            log.info("사용자 조회 성공: " + user.getUser_name()); // 사용자 정보가 조회되면 이름을 로깅
        }
        return user;
    }
    //회원 가입 구현
    public int signup(SignupRequest signupRequest){
        int result = 0;
        result = sqlSessionTemplate.insert("userInsert", signupRequest);
        return result;
    }
}

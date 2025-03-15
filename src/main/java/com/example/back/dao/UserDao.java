package com.example.back.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.example.back.model.Role;
import com.example.back.model.SignupRequest;
import com.example.back.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.mybatis.spring.SqlSessionTemplate;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository //이 클래스가 DAO 레이어임을 선언 (Spring이 Bean으로 관리)
public class UserDao {
    /* MyBatis의 SqlSessionTemplate을 사용하여 SQL을 실행할 수 있도록 자동 주입 */
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    //리액트 로그인 화면에서 username과 비번을 입력했을 때 호출되는 메소드 입니다.
    public User findByUsername(@Param("user_id") String user_id) {
        log.info("🔍 사용자 조회 시도: " + user_id); // user_id가 제대로 전달되는지 확인
        User user = sqlSessionTemplate.selectOne("findByUsername", user_id);
        if (user == null) {
            log.warn("사용자 정보가 없습니다: " + user_id); // 쿼리 결과가 null인 경우 경고 로그
        } else {
            log.info(" 사용자 조회 성공: " + user.getUser_name()); //  조회된 사용자 정보 확인
            log.info(" 조회된 user_id: " + user.getUser_id()); //  user_id 값이 정상적으로 가져와지는지 확인
        }
        return user;
    }

      // 이메일 기반 사용자 조회 (로그인 및 회원 정보 조회에 사용)
        public User findByEmail(String user_email) {
        log.info(" 사용자 조회 시도: {}", user_email); // user_email이 제대로 전달되는지 확인
        User user = sqlSessionTemplate.selectOne("findByEmail", user_email);
        
        if (user == null) {
            log.warn(" 사용자 정보가 없습니다: {}", user_email);
        } else {
            log.info(" 사용자 조회 성공: {}", user.getUser_email());
        }
        
        return user;
    }

    // 🔹 이메일 인증 코드 저장
    public void saveVerificationCode(String user_email, String code) {
        log.info("🔹 인증 코드 저장 - 이메일: {}, 코드: {}", user_email, code);
        Map<String, Object> params = new HashMap<>();
        params.put("user_email", user_email);
        params.put("code", code);

        sqlSessionTemplate.insert("insertVerificationCode", params);
    }

    // 🔹 이메일 인증 코드 조회
    public String findVerificationCodeByEmail(String user_email) {
        log.info("🔍 [DB 조회 시도] 이메일: {}", user_email);
        String code = sqlSessionTemplate.selectOne("findVerificationCodeByEmail", user_email);
        log.info("🔍 [DB 조회 결과] 이메일: {}, 인증 코드: {}", user_email, code);
        return code;
    }

    // 이메일 인증 여부 확인
    public boolean isEmailVerified(String user_email) {
        log.info("이메일 인증 여부 확인: {}", user_email);

        //List<String> roles = sqlSessionTemplate.selectList("findRolesByEmail", user_email);

        Integer count = sqlSessionTemplate.selectOne("isEmailVerified", user_email);

        boolean verified = count != null && count > 0;
        log.info("✅ 이메일 인증 상태 (0: 인증 안 됨, 1 이상: 인증 완료) → verified: {}", verified);

          // 기본 로그인(USER) 계정이 있으면 인증 필요
        //boolean requiresVerification = roles.contains("USER");

        /* if (!requiresVerification) {
            log.info("✅ 이메일 인증 필요 없음 (SNS 계정만 존재)");
            return true; // SNS 계정만 있으면 자동 인증 성공
        }

        int count = sqlSessionTemplate.selectOne("isEmailVerified", user_email);
        log.info("✅ 이메일 인증 여부 (0: 인증 안 됨, 1 이상: 인증 완료) → count: {}", count); */

        return verified;
    }

    // 이메일 인증 완료 시 `expired` 값을 true로 변경
    public void updateVerificationStatus(String user_email) {
    log.info("🔹 [이메일 인증 완료] 인증 상태 업데이트 - 이메일: {}", user_email);
    sqlSessionTemplate.update("updateVerificationStatus", user_email);
    }

    //이메일이 DB에 존재하는지 확인

    public List<String> findRolesByEmail(String user_email) {
        log.info("🔍 이메일에 대한 Role 조회: {}", user_email);
        return sqlSessionTemplate.selectList("findRolesByEmail", user_email);
    }

    // 이메일이 DB에 존재하는지 확인 (Role 기반 검사 추가)
    public boolean userExists(String user_email) {
    log.info("🔍 이메일 존재 여부 확인 (Role 포함): {}", user_email);
    
    List<String> roles = sqlSessionTemplate.selectList("findRolesByEmail", user_email);
    boolean exists = roles.contains("USER");

    log.info("✅ 이메일 존재 여부 (0: 없음, 1 이상: 존재) → exists: {}", exists);
    return exists;
    }


    /* public boolean userExists(String user_email) {
        log.info("🔍 이메일 존재 여부 확인: {}", user_email);

        // 같은 이메일을 가진 모든 계정 조회
        List<String> roles = sqlSessionTemplate.selectList("findRolesByEmail", user_email);

        // 기본로그인(USER) 가 있다면 중복메일로 처리
        boolean exists = roles.contains("USER");

        //int count = sqlSessionTemplate.selectOne("countByEmail", user_email);
        //log.info("✅ 이메일 존재 여부 (0: 없음, 1 이상: 존재) → count: {}", count);

        log.info("이메일 존재 여부(0: 없음 1 이상: 존재) -> exists: {}", exists);
        return exists;
    } */

    // 아이디 중복검사
    public boolean isUsernameAvailable(@Param("user_id") String user_id) {
        log.info(" 아이디 중복 확인: " + user_id);
        int count = sqlSessionTemplate.selectOne("countByUserId", user_id);
        log.info(" 중복된 아이디 개수: " + count);
        return count == 0; // 0이면 사용 가능, 1 이상이면 중복
    }

        //회원가입요청
    public int signup(SignupRequest signupRequest) {
    log.info("📝 회원가입 요청 - email: {}, name: {}", signupRequest.getUser_email(), signupRequest.getUser_name());
    if (signupRequest.getRole() == null) {
        signupRequest.setRole(Role.USER); // Enum 타입으로 설정
    }
    return sqlSessionTemplate.insert("userInsert", signupRequest);
    }

     // 기존 사용자의 role을 SNS로 업데이트
        public void updateRoleByEmail(String userEmail, String role) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_email", userEmail);
        params.put("role", role);

        sqlSessionTemplate.update("updateRoleByEmail", params);  // MyBatis update 쿼리 호출
    }
}

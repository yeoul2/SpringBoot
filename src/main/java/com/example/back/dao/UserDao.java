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
@Repository // 이 클래스가 DAO 레이어임을 선언 (Spring이 Bean으로 관리)
public class UserDao {

    /* MyBatis의 SqlSessionTemplate을 사용하여 SQL을 실행할 수 있도록 자동 주입 */

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    // 리액트 로그인 화면에서 username과 비번을 입력했을 때 호출되는 메소드 입니다.
    public User findByUsername(@Param("user_id") String user_id) {
        log.info("🔍 사용자 조회 시도: " + user_id); // user_id가 제대로 전달되는지 확인
        User user = sqlSessionTemplate.selectOne("findByUsername", user_id);
        if (user == null) {
            log.warn("사용자 정보가 없습니다: " + user_id); // 쿼리 결과가 null인 경우 경고 로그
        } else {
            log.info(" 사용자 조회 성공: " + user.getUser_name()); // 조회된 사용자 정보 확인
            log.info(" 조회된 user_id: " + user.getUser_id()); // user_id 값이 정상적으로 가져와지는지 확인
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
        Boolean verified = sqlSessionTemplate.selectOne("isEmailVerified", user_email);
        return verified != null && verified;

    }

    public int countByEmail(String user_email) {
        log.info("🔍 이메일 중복 확인 (USER 계정 기준): {}", user_email);
        return sqlSessionTemplate.selectOne("countByEmail", user_email);
    }

    public boolean isEmailRegistered(String user_email) {
        log.info("🔍 이메일 중복 확인 (USER 계정 기준): {}", user_email);
        int count = sqlSessionTemplate.selectOne("countByEmail", user_email);
        return count > 0;
    }

    // 이메일 인증 완료 시 `expired` 값을 true로 변경
    public void updateVerificationStatus(String user_email) {
        log.info("🔹 [이메일 인증 완료] 인증 상태 업데이트 - 이메일: {}", user_email);
        sqlSessionTemplate.update("updateVerificationStatus", user_email);
    }

    // 이메일이 DB에 존재하는지 확인 (Role 기반 검사 추가)
    public boolean userExists(String user_email) {
        log.info("🔍 이메일 존재 여부 확인 (Role 포함): {}", user_email);

        List<String> roles = sqlSessionTemplate.selectList("findRolesByEmail", user_email);
        boolean exists = roles.contains("USER");

        log.info("✅ 이메일 존재 여부 (0: 없음, 1 이상: 존재) → exists: {}", exists);
        return exists;
    }

    // ✅ 해당 이메일이 USER 역할을 가진 계정이 존재하는지 확인
    public boolean hasUserRoleByEmail(String user_email) {
        log.info("🔍 같은 이메일로 USER 계정 존재 여부 확인: {}", user_email);
        int count = sqlSessionTemplate.selectOne("countByEmail", user_email);
        return count > 0; // 1개 이상이면 USER 계정이 존재함
    }

    // ✅ 해당 이메일의 모든 Role 조회 (기존 findRolesByEmail 활용)
    public List<String> findRolesByEmail(String user_email) {
        log.info("🔍 이메일에 대한 Role 조회: {}", user_email);
        return sqlSessionTemplate.selectList("findRolesByEmail", user_email);
    }

    // ✅ 해당 이메일이 인증 완료(`expired = true`) 상태인지 확인
    public boolean isEmailExpired(String user_email) {
        log.info("🔍 이메일 인증 완료 여부 확인: {}", user_email);
        String code = sqlSessionTemplate.selectOne("findVerificationCodeByEmail", user_email);
        return code == null; // 인증 코드가 없으면 이미 만료된 것
    }

    // 아이디 중복검사
    public boolean isUsernameAvailable(@Param("user_id") String user_id) {
        log.info(" 아이디 중복 확인: " + user_id);
        int count = sqlSessionTemplate.selectOne("countByUserId", user_id);
        log.info(" 중복된 아이디 개수: " + count);
        return count == 0; // 0이면 사용 가능, 1 이상이면 중복
    }

    // 회원가입요청
    public int signup(SignupRequest signupRequest) {
        log.info("📝 회원가입 요청 - email: {}, name: {}", signupRequest.getUser_email(), signupRequest.getUser_name());
        if (signupRequest.getRole() == null) {
            signupRequest.setRole(Role.USER); // Enum 타입으로 설정
        }
        return sqlSessionTemplate.insert("userInsert", signupRequest);
    }

    // 아이디찾기
    public String findUserIdByNameAndEmail(String user_name, String user_email) {
        Map<String, String> params = new HashMap<>();
        params.put("user_name", user_name);
        params.put("user_email", user_email);

        String userId = sqlSessionTemplate.selectOne("findUserIdByNameAndEmail", params);

        if (userId != null) {
            log.info("🔍 [UserDao] 찾은 아이디: {}", userId);
        } else {
            log.warn("❌ [UserDao] 일치하는 아이디를 찾을 수 없음");
        }

        return sqlSessionTemplate.selectOne("findUserIdByNameAndEmail", params);
    }

    // 비번 찾기
    public Integer findUserPwByIdAndEmail(String user_id, String user_email) {
        Map<String, String> params = new HashMap<>();
        params.put("user_id", user_id);
        params.put("user_email", user_email);

        Integer userNo = sqlSessionTemplate.selectOne("findUserPwByIdAndEmail", params);

        if (userNo != null) {
            log.info("🔍 [UserDao] 찾은 사용자 번호: {}", userNo);
        } else {
            log.warn("⚠ [UserDao] 일치하는 계정이 없습니다.");
        }

        return userNo;
    }

    // 비밀번호 업데이트
    public void updatePassword(Integer user_no, String encryptedPassword, boolean isTempPw) {

        if (user_no == null || user_no == -1) {
            log.error("❌ 비밀번호 업데이트 실패: 유효하지 않은 user_no ({})", user_no);
            return; // user_no가 잘못된 경우 업데이트 실행하지 않음
        }

        Map<String, Object> params = new HashMap<>();
        params.put("user_no", user_no);
        params.put("user_pw", encryptedPassword);
        params.put("is_temp_pw", isTempPw);

        log.info("🔍 비밀번호 업데이트 실행: user_no={}, is_temp_pw={}", user_no, isTempPw); // 🔥 로그 추가

        int result = sqlSessionTemplate.update("updatePassword", params);

        if (result > 0) {
            log.info("🔍 비밀번호 업데이트 성공 (user_no: {}, is_temp_pw: {})", user_no, isTempPw);
        } else {
            log.info("🔍 비밀번호 업데이트 실패 (user_no: {})", user_no);
        }
    }

    // 사용자 정보 업데이트
    public void updateUserInfo(int user_no, String user_name, String user_email, String user_birth) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_no", user_no);
        params.put("user_name", user_name);
        params.put("user_email", user_email);
        params.put("user_birth", user_birth);

        log.info("🔄 사용자 정보 업데이트: {}", params);
        int result = sqlSessionTemplate.update("updateUserInfo", params);

        if (result > 0) {
            log.info("✅ 사용자 정보 업데이트 성공 (user_no: {})", user_no);
        } else {
            log.warn("❌ 사용자 정보 업데이트 실패 (user_no: {})", user_no);
        }
    }

    // 사용자 삭제
    public int deleteUser(int user_no) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_no", user_no);

        log.info("🗑️ 실행할 DELETE SQL: DELETE FROM users WHERE user_no = {}", params);
        int result = sqlSessionTemplate.delete("deleteUser", params);

        if (result > 0) {
            log.info("✅ 사용자 삭제 성공 (user_no: {})", user_no);
        } else {
            log.warn("❌ 사용자 삭제 실패 (user_no: {})", user_no);
        }
        return result;
    }
}

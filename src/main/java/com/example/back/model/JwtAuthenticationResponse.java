package com.example.back.model;

import lombok.Data;
//오라클 서버에 select문으로 처리한 결과를 담을 클래스 선언
//로그인 성공 후에 발급 받은 accessToken과 refreshToken을 동시에 담기
//로그인한 시간에서 현재 시간을 차를 구하면 흘러간 시간이 나옴
//시간이 파기시간과 일치하기 60초 전에 토큰을 연장하시겠습니까?
//로그인 성공시 DB에서 꺼내온 정보를 담아서 리액트로 전달하기
//리액트에서는 localStorage에 저장했다가 활용하기
@Data
public class JwtAuthenticationResponse {
    //private String googleAccessToken;  // 🔥 추가: 구글에서 받은 액세스 토큰
    //JWTService에서 가져온 값 담음
    private String accessToken;//로그인 할 때 생성된 토큰
    //JWTService에서 가져온 값 담음
    private String refreshToken;//활용할 때 사용할 토큰- 최초 같이 생성함.
    private Role role; //오라클 DB에서 꺼낸값이 담김
    private String user_name;//DB에서 조회된 값
    private int user_no;//DB에서 조회된 값
    private String user_email;//DB에서 조회된 값
    private String user_birth;//DB에서 조회된 값
    private String user_id;//DB에서 조회된 값
    private boolean isTempPw;

    // ✅ 직접 생성자 추가 (모든 필드를 받는 생성자)
    public JwtAuthenticationResponse( String accessToken, String refreshToken, String user_id, 
                                    String user_email, String user_name, String user_birth, 
                                    int user_no, Role role, boolean isTempPw) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user_id = user_id;
        this.user_email = user_email;
        this.user_name = user_name;
        this.user_birth = user_birth;
        this.user_no = user_no;
        this.role = role;
        this.isTempPw = isTempPw;
    }

}



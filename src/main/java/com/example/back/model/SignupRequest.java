package com.example.back.model;

import lombok.Data;
//회원가입 할 땐
@Data
public class SignupRequest {
    private int user_no;
    private String user_name;
    private String user_email; 
    private String user_id;
    private String user_pw; //12345가 아니라 암호화된 비번을 담을 것 - 주의
    private String user_birth;
    private Role role;
}

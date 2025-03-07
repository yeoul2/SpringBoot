package com.example.back.model;

import lombok.Data;

@Data
public class SigninRequest {
    private String user_id;
    private String user_pw;
}

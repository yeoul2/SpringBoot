package com.example.back.model;

import lombok.Data;

@Data
public class SearchRequest {
  private String userId;       // 🔹 로그인한 사용자 ID
  private String searchTerm;   // 🔹 검색어 (입력한 값)
  private String searchType;   // 🔹 검색 구분 (country / city)
}

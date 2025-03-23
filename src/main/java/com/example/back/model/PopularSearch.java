package com.example.back.model;

import lombok.Data;

@Data
public class PopularSearch {
  private String searchTerm;   // 🔹 검색어 (국가 또는 도시)
  private String searchType;   // 🔹 검색 구분 (country / city)
  private int searchCount;     // 🔹 누적 검색 횟수
}

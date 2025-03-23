package com.example.back.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SearchHistory {
  private int id;                  // 🔹 검색 기록 고유 ID (AUTO_INCREMENT)
  private String userId;          // 🔹 사용자 ID (users.user_id와 연결)
  private String searchTerm;      // 🔹 검색어 (도시명 또는 국가명)
  private String searchType;      // 🔹 검색 구분 (country / city)
  private LocalDateTime searchDate; // 🔹 검색한 시간
}

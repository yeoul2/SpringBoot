package com.example.back.model;

import lombok.Data;

@Data
public class SearchRequest {
	private String userId;        // 🔹 검색한 사용자 번호 (users 테이블의 user_no와 일치)
	private String searchTerm; // 🔹 검색한 나라 또는 도시
	private String searchType; // 🔹 검색 유형 (country / city)
}

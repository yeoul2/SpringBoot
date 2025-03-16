package com.example.back.model;

import lombok.Data;

@Data
public class PopularSearch {
	private String searchTerm;  // 검색된 나라 또는 도시
	private String searchType;  // 검색 유형 (country / city)
	private int searchCount;    // 해당 검색어가 검색된 횟수
}

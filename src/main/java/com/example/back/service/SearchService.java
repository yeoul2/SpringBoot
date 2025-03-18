package com.example.back.service;

import com.example.back.dao.SearchDao;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class SearchService {

	@Autowired
	private SearchDao searchDao;

	// 🔹 1. 최근 검색어 저장 (최대 5개 유지)
	@Transactional
	public int saveRecentSearch(int userNo, String searchTerm, String searchType) {
		log.info("🔍 saveRecentSearch 호출 | userNo: {}, searchTerm: {}, searchType: {}", userNo, searchTerm, searchType);

		// 🔹 최근 검색어 저장
		searchDao.saveRecentSearch(Map.of("userNo", userNo, "searchTerm", searchTerm, "searchType", searchType));

		// 🔹 최근 검색어 5개 유지 (초과 시 삭제)
		searchDao.deleteOldRecentSearches(userNo);

		log.info("✅ saveRecentSearch 완료");
		return 1; // ✅ 성공 시 1 반환
	}

	// 🔹 2. 최근 검색어 조회 (최대 5개) ✅ `getRecentSearchList()` 메서드명 일치
	public List<Map<String, Object>> getRecentSearchList(int userNo) {
		log.info("🔍 getRecentSearchList 호출 | userNo: {}", userNo);
		return searchDao.getRecentSearchList(userNo);
	}

	// 🔹 3. 특정 최근 검색어 삭제
	@Transactional
	public int deleteRecentSearch(int userNo, String searchTerm) {
		log.info("🔍 deleteRecentSearch 호출 | userNo: {}, searchTerm: {}", userNo, searchTerm);
		return searchDao.deleteRecentSearch(Map.of("userNo", userNo, "searchTerm", searchTerm));
	}

	// 🔹 4. 인기 검색어 업데이트 (검색할 때마다 호출)
	@Transactional
	public int updatePopularSearchCount(String searchTerm, String searchType) {
		log.info("🔍 updatePopularSearchCount 호출 | searchTerm: {}, searchType: {}", searchTerm, searchType);
		return searchDao.updatePopularSearchCount(Map.of("searchTerm", searchTerm, "searchType", searchType));
	}

	// 🔹 5. 인기 검색어 저장 (처음 검색할 때) ✅ 변경된 메서드명 적용
	@Transactional
	public int insertPopularSearch(String searchTerm, String searchType) {
		log.info("🔍 insertPopularSearch 호출 | searchTerm: {}, searchType: {}", searchTerm, searchType);
		return searchDao.insertPopularSearch(Map.of("searchTerm", searchTerm, "searchType", searchType));
	}
	// 🔹 6. 인기 검색어 조회 (TOP 10) ✅ 추가
	public List<Map<String, Object>> getPopularSearchList() {
		log.info("🔍 getPopularSearchList 호출");
		return searchDao.getPopularSearchList();
	}
}

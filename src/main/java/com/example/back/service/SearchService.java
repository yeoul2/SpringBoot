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

	// 🔹 1. 검색어 저장 (최근 검색어 + 인기 검색어 반영)
	@Transactional
	public void saveSearch(String userId, String searchTerm, String searchType) {
		log.info("🔍 saveSearch 호출 | userId: {}, searchTerm: {}, searchType: {}", userId, searchTerm, searchType);

		// 🔹 중복 검색어 삭제 후 저장
		searchDao.deleteDuplicateSearch(userId, searchTerm, searchType);
		searchDao.insertSearch(userId, searchTerm, searchType);

		// 🔹 최근 검색어 5개 유지 (초과 시 가장 오래된 검색어 삭제)
		if (searchDao.countRecentSearches(userId) >= 5) {
			searchDao.deleteOldestSearch(userId);
		}

		// 🔹 인기 검색어 업데이트 (기존 검색어는 count 증가)
		searchDao.updatePopularSearch(userId, searchTerm, searchType);

		log.info("✅ saveSearch 완료");
	}

	// 🔹 2. 최근 검색어 조회 (최대 5개)
	public List<Map<String, Object>> getRecentSearchList(String userId) {
		log.info("🔍 getRecentSearchList 호출 | userId: {}", userId);
		return searchDao.getRecentSearchList(userId);
	}

	// 🔹 3. 특정 검색어 삭제
	@Transactional
	public void deleteSearch(String userId, String searchTerm) {
		log.info("🔍 deleteSearch 호출 | userId: {}, searchTerm: {}", userId, searchTerm);
		searchDao.deleteSearch(userId, searchTerm);
	}

	// 🔹 4. 인기 검색어 업데이트 (검색할 때마다 호출)
	@Transactional
	public void updatePopularSearch(String userId, String searchTerm, String searchType) {
		log.info("🔍 updatePopularSearch 호출 | userId: {}, searchTerm: {}, searchType: {}", userId, searchTerm, searchType);
		searchDao.updatePopularSearch(userId, searchTerm, searchType);
	}

	// 🔹 5. 인기 검색어 삽입 (처음 검색할 때)
	@Transactional
	public void insertPopularSearch(String searchTerm, String searchType) {
		log.info("🔍 insertPopularSearch 호출 | searchTerm: {}, searchType: {}", searchTerm, searchType);
		searchDao.insertPopularSearch(searchTerm, searchType);
	}

	// 🔹 6. 인기 검색어 조회 (TOP 10)
	public List<Map<String, Object>> getPopularSearchList() {
		log.info("🔍 getPopularSearchList 호출");
		return searchDao.getPopularSearchList();
	}
}

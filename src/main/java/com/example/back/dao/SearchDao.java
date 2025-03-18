package com.example.back.dao;

import lombok.extern.log4j.Log4j2;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Log4j2
@Repository
public class SearchDao {

	@Autowired
	private SqlSessionTemplate sqlSessionTemplate;

	// 🔹 1. 최근 검색어 저장
	public int saveRecentSearch(Map<String, Object> sMap) {
		log.info("🔍 saveRecentSearch 호출 | 파라미터: {}", sMap);
		return sqlSessionTemplate.insert("saveRecentSearch", sMap);
	}

	// 🔹 2. 최근 검색어 5개 유지 (오래된 검색어 삭제)
	public int deleteOldRecentSearches(int userNo) {
		log.info("🔍 deleteOldRecentSearches 호출 | userNo: {}", userNo);
		return sqlSessionTemplate.delete("deleteOldRecentSearches", userNo);
	}

	// 🔹 3. 최근 검색어 조회 (최신 5개) ✅ 메서드명 변경
	public List<Map<String, Object>> getRecentSearchList(int userNo) {
		log.info("🔍 getRecentSearchList 호출 | userNo: {}", userNo);
		return sqlSessionTemplate.selectList("getRecentSearchList", userNo);
	}

	// 🔹 4. 특정 최근 검색어 삭제
	public int deleteRecentSearch(Map<String, Object> sMap) {
		log.info("🔍 deleteRecentSearch 호출 | 파라미터: {}", sMap);
		return sqlSessionTemplate.delete("deleteRecentSearch", sMap);
	}

	// 🔹 5. 인기 검색어 조회 (TOP 10)
	public List<Map<String, Object>> getPopularSearchList() {
		log.info("🔍 getPopularSearchList 호출");
		return sqlSessionTemplate.selectList("getPopularSearchList");
	}

	// 🔹 6. 인기 검색어 검색 횟수 증가 ✅ 추가
	public int updatePopularSearchCount(Map<String, Object> searchTerm) {
		log.info("🔍 updatePopularSearchCount 호출 | 파라미터: {}", searchTerm);
		return sqlSessionTemplate.update("updatePopularSearchCount", searchTerm);
	}

	// 🔹 7. 인기 검색어 저장 (신규 등록) ✅ `updatePopularSearch` → `insertPopularSearch`로 변경
	public int insertPopularSearch(Map<String, Object> searchTerm) {
		log.info("🔍 insertPopularSearch 호출 | 파라미터: {}", searchTerm);
		return sqlSessionTemplate.insert("insertPopularSearch", searchTerm);
	}
}

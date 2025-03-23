package com.example.back.dao;

import lombok.extern.log4j.Log4j2;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Repository
public class SearchDao {

	@Autowired
	private SqlSessionTemplate sqlSessionTemplate;

	// 🔹 1. 검색어 저장 (중복 제거 후 저장)
	public void insertSearch(String userId, String searchTerm, String searchType) {
		log.info("🔍 insertSearch 호출 | userId: {}, searchTerm: {}, searchType: {}", userId, searchTerm, searchType);
		sqlSessionTemplate.delete("deleteDuplicateSearch",
				Map.of("userId", userId, "searchTerm", searchTerm, "searchType", searchType));
		sqlSessionTemplate.insert("insertSearch",
				Map.of("userId", userId, "searchTerm", searchTerm, "searchType", searchType));
	}

	// 🔹 2. 중복 검색어 삭제
	public void deleteDuplicateSearch(String userId, String searchTerm, String searchType) {
		log.info("🔍 deleteDuplicateSearch 호출 | userId: {}, searchTerm: {}", userId, searchTerm);
		sqlSessionTemplate.delete("deleteDuplicateSearch",
				Map.of("userId", userId, "searchTerm", searchTerm, "searchType", searchType));
	}

	// 🔹 3. 최근 검색어 조회 (최대 5개)
	public List<Map<String, Object>> getRecentSearchList(String userId) {
		log.info("🔍 getRecentSearchList 호출 | userId: {}", userId);
		return sqlSessionTemplate.selectList("getRecentSearchList", userId);
	}

	// 🔹 4. 최근 검색어 개수 조회
	public int countRecentSearches(String userId) {
		log.info("🔍 countRecentSearches 호출 | userId: {}", userId);
		return sqlSessionTemplate.selectOne("countRecentSearches", userId);
	}

	// 🔹 5. 가장 오래된 검색어 삭제 (최대 5개 유지)
	public void deleteOldestSearch(String userId) {
		log.info("🔍 deleteOldestSearch 호출 | userId: {}", userId);
		sqlSessionTemplate.delete("deleteOldestSearch", userId);
	}

	// 🔹 6. 특정 검색어 삭제 (사용자가 직접 삭제)
	/* public void deleteSearch(String userId, String searchTerm) {
		log.info("🔍 deleteSearch 호출 | userId: {}, searchTerm: {}", userId, searchTerm);
		// sqlSessionTemplate.delete("deleteSearch",
		sqlSessionTemplate.delete("deleteSearch",
				Map.of("userId", userId, "searchTerm", searchTerm));
	} */

	/* 이희범 테스트 */
	public int deleteRecentSearch(String userId, String searchTerm) { // ✅ 함수명 변경
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("searchTerm", searchTerm);

        log.info("🗑️ 실행할 DELETE SQL: DELETE FROM recent_searches WHERE user_id = {} AND search_term = {}", userId, searchTerm);

        return sqlSessionTemplate.delete("deleteRecentSearch", params); // ✅ 매핑된 ID 변경
    }

	// 🔹 7. 인기 검색어 업데이트 (검색할 때마다 호출)
	public void updatePopularSearch(String userId, String searchTerm, String searchType) {
		log.info("🔍 updatePopularSearch 호출 | userId: {}, searchTerm: {}, searchType: {}", userId, searchTerm,
				searchType);
		sqlSessionTemplate.insert("updatePopularSearch",
				Map.of("userId", userId, "searchTerm", searchTerm, "searchType", searchType));
	}

	// 🔹 8. 인기 검색어 삽입 (처음 검색할 때)
	public void insertPopularSearch(String searchTerm, String searchType) {
		log.info("🔍 insertPopularSearch 호출 | searchTerm: {}, searchType: {}", searchTerm, searchType);
		sqlSessionTemplate.insert("insertPopularSearch",
				Map.of("searchTerm", searchTerm, "searchType", searchType));
	}

	// 🔹 9. 인기 검색어 조회 (TOP 10)
	public List<Map<String, Object>> getPopularSearchList() {
		log.info("🔍 getPopularSearchList 호출");
		return sqlSessionTemplate.selectList("getPopularSearchList");
	}

}

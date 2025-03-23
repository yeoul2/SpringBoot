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

	/* 🔹 최근 검색어 개수 조회 */
	public int countSearch(String userId) {
		log.info("countSearch 호출 성공");
		return sqlSessionTemplate.selectOne("countSearch", userId);
	}

	/* 🔹 최근 검색어 저장 (중복 시 시간만 갱신됨) */
	public int saveSearch(Map<String, Object> map) {
		log.info("saveSearch 호출 성공");
		return sqlSessionTemplate.insert("saveSearch", map);
	}

	/* 🔹 최근 검색어 목록 조회 (최신순, 최대 5개) */
	public List<Map<String, Object>> searchList(String userId) {
		log.info("searchList 호출 성공");
		return sqlSessionTemplate.selectList("searchList", userId);
	}

	/* 🔹 최근 검색어 삭제 (사용자 직접 요청) */
	public int deleteSearch(Map<String, Object> map) {
		log.info("deleteSearch 호출 성공");
		return sqlSessionTemplate.delete("deleteSearch", map);
	}

	/* 🔹 인기 검색어 저장 또는 검색 수 증가 */
	public int updatePopularSearch(Map<String, Object> map) {
		log.info("updatePopularSearch 호출 성공");
		return sqlSessionTemplate.insert("updatePopularSearch", map);
	}

	/* 🔹 인기 검색어 목록 조회 (TOP 10) */
	public List<Map<String, Object>> popularList() {
		log.info("popularList 호출 성공");
		return sqlSessionTemplate.selectList("popularList");
	}
}

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

  // 🔹 검색 기록 저장 (search_history 테이블에 INSERT)
  public int insertSearchHistory(Map<String, Object> sMap) {
    log.info("🔍 insertSearchHistory 호출 성공 | 파라미터: {}", sMap);
    int result = sqlSessionTemplate.insert("insertSearchHistory", sMap);
    log.info("✅ insertSearchHistory 실행 결과: {}", result);
    return result;
  }

  // 🔹 인기 검색어 존재 여부 확인 (popular_searches 테이블 조회)
  public int checkPopularSearchExists(Map<String, Object> sMap) {
    log.info("🔍 checkPopularSearchExists 호출 성공 | 파라미터: {}", sMap);
    int result = sqlSessionTemplate.selectOne("checkPopularSearchExists", sMap);
    log.info("✅ checkPopularSearchExists 결과: {}", result);
    return result;
  }

  // 🔹 기존 검색어 검색 횟수 증가 (search_count 값 증가)
  public int updatePopularSearchCount(Map<String, Object> sMap) {
    log.info("🔍 updatePopularSearchCount 호출 성공 | 파라미터: {}", sMap);
    int result = sqlSessionTemplate.update("updatePopularSearchCount", sMap);
    log.info("✅ updatePopularSearchCount 실행 결과: {}", result);
    return result;
  }

  // 🔹 새로운 검색어 추가 (popular_searches 테이블에 INSERT)
  public int insertPopularSearch(Map<String, Object> sMap) {
    log.info("🔍 insertPopularSearch 호출 성공 | 파라미터: {}", sMap);
    int result = sqlSessionTemplate.insert("insertPopularSearch", sMap);
    log.info("✅ insertPopularSearch 실행 결과: {}", result);
    return result;
  }

  // 🔹 인기 검색어 조회 (search_count 기준 상위 10개 검색어 가져오기)
  public List<Map<String, Object>> getPopularSearches() {
    log.info("🔍 getPopularSearches 호출 성공");
    List<Map<String, Object>> list = sqlSessionTemplate.selectList("getPopularSearches");
    log.info("✅ getPopularSearches 결과 개수: {}", (list != null ? list.size() : 0));
    return list != null ? list : List.of();  // ✅ NPE 방지
  }

  // 🔹 사용자의 최근 검색어 가져오기 (search_history 테이블에서 최신 검색어 1개 조회)
  public String getLastSearchTerm(Map<String, Object> sMap) {
    log.info("🔍 getLastSearchTerm 호출 성공 | 파라미터: {}", sMap);
    String lastTerm = sqlSessionTemplate.selectOne("getLastSearchTerm", sMap);
    log.info("✅ getLastSearchTerm 결과: {}", lastTerm);
    return lastTerm != null ? lastTerm : "";  // ✅ NULL 방지 (검색 기록이 없을 경우 빈 문자열 반환)
  }
}

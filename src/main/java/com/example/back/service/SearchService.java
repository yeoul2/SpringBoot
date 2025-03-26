package com.example.back.service;

import com.example.back.dao.SearchDao;
import com.example.back.model.PopularSearch;
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

  /* 🔹 최근 검색어 개수 조회 */
  public int countSearch(String user_id) {
    log.info("countSearch 호출 성공");
    return searchDao.countSearch(user_id);
  }

  /* 🔹 최근 검색어 저장 (최대 5개 유지, 중복 시 갱신) */
  @Transactional
  public int saveSearch(Map<String, Object> map) {
    String user_id = (String) map.get("user_id");
    if (user_id == null || user_id.isBlank()) {
      return 0;  // 유효한 userId가 없으면 저장하지 않음
    }

    // 도시명이 없으면 저장하지 않음
    String city = (String) map.get("searchTerm");
    if (city == null || city.isBlank()) {
      return 0;  // 도시명이 없으면 저장하지 않음
    }

    // 5개 이상의 검색어가 있으면 저장을 하지 않음
    int count = searchDao.countSearch(user_id);
    if (count >= 5) {
      // 동일한 검색어가 존재하는지 체크
      boolean isDuplicate = searchDao.isExistingSearch(map);
      if (isDuplicate) {
        // 중복이면 날짜만 갱신
        int result = searchDao.saveSearch(map);

        // ✅ 인기 검색어 집계 추가
        searchDao.updatePopularSearch(map);

        return result;
      } else {
        // 5개 초과 시 새로운 검색어는 저장하지 않음
        return 0;
      }
    }

    // 5개 미만이면 정상 저장
    int result = searchDao.saveSearch(map);

    // ✅ 인기 검색어 집계 추가
    searchDao.updatePopularSearch(map);

    return result;
  }

  /* 🔹 최근 검색어 목록 조회 (최신순, 최대 5개) */
  public List<Map<String, Object>> searchList(String user_id) {
    log.info("searchList 호출 성공");
    return searchDao.searchList(user_id);
  }

  /* 🔹 최근 검색어 삭제 */
  @Transactional
  public int deleteSearch(Map<String, Object> map) {
    log.info("deleteSearch 호출 성공");
    return searchDao.deleteSearch(map);
  }

  /* 🔹 인기 검색어 저장 또는 카운트 증가 */
  @Transactional
  public int updatePopularSearch(Map<String, Object> map) {
    log.info("updatePopularSearch 호출 성공");
    return searchDao.updatePopularSearch(map);
  }

  /* 🔹 인기 검색어 Top 10 조회 */
  public List<PopularSearch> popularList() {
    log.info("popularList 호출 성공");
    return searchDao.popularList();
  }
}

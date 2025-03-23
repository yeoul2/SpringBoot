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

  /* 🔹 최근 검색어 개수 조회 (저장 조건 확인용) */
  public int countSearch(String userId) {
    log.info("countSearch 호출 성공");
    return searchDao.countSearch(userId);
  }

  /* 🔹 최근 검색어 저장 (중복 시 시간 갱신) */
  @Transactional
  public int saveSearch(Map<String, Object> map) {
    log.info("saveSearch 호출 성공");
    return searchDao.saveSearch(map);
  }

  /* 🔹 최근 검색어 목록 조회 (최신순, 최대 5개) */
  public List<Map<String, Object>> searchList(String userId) {
    log.info("searchList 호출 성공");
    return searchDao.searchList(userId);
  }

  /* 🔹 최근 검색어 삭제 (사용자 요청 시) */
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
  public List<Map<String, Object>> popularList() {
    log.info("popularList 호출 성공");
    return searchDao.popularList();
  }
}

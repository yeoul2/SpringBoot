package com.example.back.controller;

import com.example.back.service.SearchService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/search")
public class SearchController {

  @Autowired
  private SearchService searchService;

  // 🔹 최근 검색어 저장 (중복 시 시간 갱신)
  @PostMapping("/save")
  public int saveSearch(@RequestBody Map<String, Object> map) {
    log.info("saveSearch 호출 성공");
    return searchService.saveSearch(map);
  }

  // 🔹 최근 검색어 목록 조회 (최신순 5개)
  @GetMapping("/recent/{userId}")
  public List<Map<String, Object>> searchList(@PathVariable("userId") String userId) {
    log.info("searchList 호출 성공");
    return searchService.searchList(userId);
  }

  // 🔹 최근 검색어 삭제 (사용자 요청)
  @DeleteMapping("/delete")
  public int deleteSearch(@RequestBody Map<String, Object> map) {
    log.info("deleteSearch 호출 성공");
    return searchService.deleteSearch(map);
  }

  // 🔹 인기 검색어 누적 업데이트
  @PostMapping("/popular/update")
  public int updatePopularSearch(@RequestBody Map<String, Object> map) {
    log.info("updatePopularSearch 호출 성공");
    return searchService.updatePopularSearch(map);
  }

  // 🔹 인기 검색어 조회 (Top 10)
  @GetMapping("/popular")
  public List<Map<String, Object>> popularList() {
    log.info("popularList 호출 성공");
    return searchService.popularList();
  }

  // 🔹 최근 검색어 개수 확인 (최대 5개 초과 방지용)
  @GetMapping("/count/{userId}")
  public int countSearch(@PathVariable("userId") String userId) {
    log.info("countSearch 호출 성공");
    return searchService.countSearch(userId);
  }
}

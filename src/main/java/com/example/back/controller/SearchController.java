package com.example.back.controller;

import com.example.back.service.SearchService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search/")
public class SearchController {

  private final SearchService searchService; // 🔹 `final` 사용하여 불변성 보장

  // 🔹 검색 기록 저장
  @PostMapping("saveSearch")
  public String saveSearch(@RequestBody Map<String, Object> requestData) {
    log.info("saveSearch 호출 성공 | 파라미터: {}", requestData);
    searchService.saveSearch(requestData);
    return "1"; // ✅ 기존 프로젝트 스타일 유지
  }

  // 🔹 인기 검색어 조회 (반환 타입 수정)
  @GetMapping("popular")
  public String getPopularSearches(@RequestParam Map<String, Object> paramMap) {
    log.info("getPopularSearches 호출 성공 | 파라미터: {}", paramMap);
    List<Map<String, Object>> list = searchService.getPopularSearches(); // ✅ 이제 List<Map<String, Object>> 반환됨
    return new Gson().toJson(list); // ✅ 컨트롤러에서 JSON 변환 수행
  }
}

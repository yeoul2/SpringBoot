package com.example.back.controller;

import com.example.back.service.JWTService;
import com.example.back.service.SearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Log4j2
@RestController
@RequiredArgsConstructor

@RequestMapping("/api/search")
public class SearchController {

  @Autowired
  private SearchService searchService;

  private final JWTService jwtService;

  // 🔹 최근 검색어 저장 (중복 시 시간 갱신)
  /*
   * @PostMapping("/save")
   * public int saveSearch(@RequestBody Map<String, Object> map) {
   * log.info("saveSearch 호출 성공");
   * return searchService.saveSearch(map);
   * }
   */

  // 🔍 인기 여행지 조회
  @GetMapping("/popular_list")
  public ResponseEntity<?> getPopularDestinations() {
    List<Map<String, Object>> list = searchService.popularList(); // 서비스 계층에서 인기 목록 받아오기
    return ResponseEntity.ok(list);
  }

  // 🔐 로그인 사용자만: 최근 검색어 목록 조회
  @GetMapping("/list") // <-- ✅ 이거 있어야 함!
  public ResponseEntity<?> getRecentSearches(@RequestHeader("Authorization") String token) {
    String user_id = jwtService.extractUserName(token.substring(7)); // 토큰에서 유저 ID 추출

    if (user_id == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("❌ 인증 실패");
    }

    List<Map<String, Object>> result = searchService.searchList(user_id);
    return ResponseEntity.ok(result);
  }

  // 🔐 로그인 사용자만: 최근 검색어 저장
  @PostMapping("/save")
  public ResponseEntity<?> saveSearch(@RequestHeader("Authorization") String token,
      @RequestBody Map<String, Object> map) {
    // JWT 검증 및 userId 추출
    if (token == null || !token.startsWith("Bearer ")) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("message", "❌ 인증되지 않은 요청입니다. 로그인 후 다시 시도하세요."));
    }

    String user_id = jwtService.extractUserName(token.substring(7)); // jwt에서 user_id 추출

    if (user_id == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("message", "❌ 사용자 정보를 찾을 수 없습니다."));
    }

    map.put("user_id", user_id);
    int result = searchService.saveSearch(map);
    return ResponseEntity.ok(result);
  }

  // 🔐 로그인 사용자만: 최근 검색어 목록 조회
  @GetMapping("/recent")
  public ResponseEntity<?> searchList(
      @RequestHeader("Authorization") String token) {
    // JWT 검증 및 userId 추출
    if (token == null || !token.startsWith("Bearer ")) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("message", "❌ 인증되지 않은 요청입니다. 로그인 후 다시 시도하세요."));
    }

    String user_id = jwtService.extractUserName(token.substring(7)); // jwt에서 user_id 추출

    if (user_id == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("message", "❌ 사용자 정보를 찾을 수 없습니다."));
    }

    List<Map<String, Object>> list = searchService.searchList(user_id);
    return ResponseEntity.ok(list);
  }

  /*
   * // 🔹 최근 검색어 목록 조회 (최신순 5개)
   * 
   * @GetMapping("/recent/{user_id}")
   * public List<Map<String, Object>> searchList(@PathVariable("user_id") String
   * user_id) {
   * log.info("searchList 호출 성공");
   * return searchService.searchList(user_id);
   * }
   */

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

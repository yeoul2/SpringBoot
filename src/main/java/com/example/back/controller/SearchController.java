package com.example.back.controller;

import com.example.back.dao.UserDao;
import com.example.back.model.PopularSearch;
import com.example.back.service.JWTService;
import com.example.back.service.SearchService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // ✅ 추가
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets; // ✅ 추가
import java.util.List;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api/search")
public class SearchController {

  @Autowired
  private SearchService searchService;

  @Autowired
  private JWTService jwtService;

  @Autowired
  private UserDao userDao;

  @Value("${spring.security.jwt.secret}") // ✅ YAML 경로에 맞게 주입
  private String secretKey;

  // 🔐 로그인 사용자만: 최근 검색어 저장
  @PostMapping("/save")
  public ResponseEntity<?> saveSearch(@RequestHeader("Authorization") String token,
                                      @RequestBody Map<String, Object> map) {
    String user_id = extractUserIdFromToken(token);
    if (user_id == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("❌ 인증된 사용자만 가능합니다.");
    }

    map.put("user_id", user_id);

    String city = (String) map.get("searchTerm");
    if (city == null || city.isBlank()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ 도시명을 입력해주세요.");
    }

    map.put("searchTerm", city);

    int result = searchService.saveSearch(map);
    return ResponseEntity.ok(result);
  }

  // 🔐 로그인 사용자만: 최근 검색어 목록 조회
  @GetMapping("/recent")
  public ResponseEntity<?> searchList(@RequestHeader("Authorization") String token) {
    String user_id = extractUserIdFromToken(token);
    if (user_id == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("❌ 인증된 사용자만 가능합니다.");
    }

    List<Map<String, Object>> list = searchService.searchList(user_id);
    return ResponseEntity.ok(list);
  }

  // 🔐 로그인 사용자만: 최근 검색어 삭제
  @DeleteMapping("/delete")
  public ResponseEntity<?> deleteSearch(@RequestHeader("Authorization") String token,
                                        @RequestBody Map<String, Object> map) {
    String userId = extractUserIdFromToken(token);
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("❌ 인증된 사용자만 가능합니다.");
    }

    map.put("userId", userId);
    int result = searchService.deleteSearch(map);
    return ResponseEntity.ok(result);
  }

  // ✅ 전체 사용자 공개: 인기 검색어 집계
  @PostMapping("/popular/update")
  public int updatePopularSearch(@RequestBody Map<String, Object> map) {
    return searchService.updatePopularSearch(map);
  }

  // ✅ 전체 사용자 공개: 인기 검색어 목록
  @GetMapping("/popular")
  public List<PopularSearch> popularList() {
    return searchService.popularList();
  }

  // 🔒 내부 메서드: JWT에서 userId 추출
  public String extractUserIdFromToken(String token) {
    try {
      if (token != null && token.startsWith("Bearer ")) {
        String jwt = token.substring(7);
        Claims claims = Jwts.parser()
            .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8)) // ✅ UTF-8 인코딩으로 보안성 강화
            .parseClaimsJws(jwt)
            .getBody();

        String userId = claims.getSubject();
        System.out.println("✅ 추출된 user_id: " + userId);
        return userId;
      }
    } catch (Exception e) {
      System.out.println("❌ 토큰 파싱 실패: " + e.getMessage());
    }
    return null;
  }
}

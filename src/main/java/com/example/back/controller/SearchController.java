package com.example.back.controller;

import com.example.back.model.CustomUserDetails;
import com.example.back.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

	private final SearchService searchService;

	// 🔹 1. 최근 검색어 저장 (로그인한 사용자만 가능)
	@PostMapping("/recent-save")
  public ResponseEntity<String> saveRecentSearch(@RequestParam String searchTerm,
                                                 @RequestParam String searchType,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
    if (userDetails == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
    }
    searchService.saveRecentSearch(userDetails.getUserNo(), searchTerm, searchType);
    return ResponseEntity.ok("최근 검색어가 저장되었습니다.");
  }

	// 🔹 2. 최근 검색어 조회 (최대 5개)
	@GetMapping("/recent-list")
	public ResponseEntity<List<Map<String, Object>>> getRecentSearchList(@AuthenticationPrincipal CustomUserDetails userDetails) {
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok(searchService.getRecentSearchList(userDetails.getUserNo()));
	}

	// 🔹 3. 최근 검색어 삭제
	@DeleteMapping("/recent-delete")
	public ResponseEntity<String> deleteRecentSearch(@RequestParam String searchTerm,
	                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		searchService.deleteRecentSearch(userDetails.getUserNo(), searchTerm);
		return ResponseEntity.ok("최근 검색어가 삭제되었습니다.");
	}

	// 🔹 4. 인기 검색어 업데이트 (검색할 때마다 호출, 모든 사용자 반영)
	@PostMapping("/popular-update")
	public ResponseEntity<String> updatePopularSearchCount(@RequestParam String searchTerm,
	                                                       @RequestParam String searchType) {
		searchService.updatePopularSearchCount(searchTerm, searchType);
		return ResponseEntity.ok("인기 검색어가 업데이트되었습니다.");
	}

	// 🔹 5. 인기 검색어 조회 (TOP 10)
	@GetMapping("/popular-list")
	public ResponseEntity<List<Map<String, Object>>> getPopularSearchList() {
		return ResponseEntity.ok(searchService.getPopularSearchList());
	}
}

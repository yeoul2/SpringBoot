package com.example.back.controller;

import com.example.back.model.CustomUserDetails;
import com.example.back.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private SearchService searchService;

	// 🔹 1. 검색어 저장 (최근 검색어 + 인기 검색어 반영)
	@PostMapping("/save")
	public ResponseEntity<String> saveSearch(@RequestParam String searchTerm,
	                                         @RequestParam String searchType,
	                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		searchService.saveSearch(userDetails.getUserId(), searchTerm, searchType);
		return ResponseEntity.ok("검색어가 저장되었습니다.");
	}

	// 🔹 2. 검색어 목록 조회 (최근 검색어 & 인기 검색어)
	@GetMapping("/list")
	public ResponseEntity<List<Map<String, Object>>> getSearchList(@RequestParam String category,
	                                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
		if ("recent".equals(category)) {
			if (userDetails == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}
			return ResponseEntity.ok(searchService.getRecentSearchList(userDetails.getUserId()));
		} else if ("popular".equals(category)) {
			return ResponseEntity.ok(searchService.getPopularSearchList());
		} else {
			return ResponseEntity.badRequest().body(null);
		}
	}

	// 🔹 3. 특정 검색어 삭제
	@DeleteMapping("/delete")
	public ResponseEntity<String> deleteSearch(@RequestParam String searchTerm,
	                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		searchService.deleteSearch(userDetails.getUserId(), searchTerm);
		return ResponseEntity.ok("검색어가 삭제되었습니다.");
	}

	// 🔹 4. 인기 검색어 업데이트 (검색할 때마다 호출)
	@PostMapping("/popular_update")
	public ResponseEntity<String> updatePopularSearch(@RequestParam String searchTerm,
	                                                  @RequestParam String searchType,
	                                                  @AuthenticationPrincipal CustomUserDetails userDetails) { // 🔥 userId 추가
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		searchService.updatePopularSearch(userDetails.getUserId(), searchTerm, searchType); // 🔥 userId 추가
		return ResponseEntity.ok("인기 검색어가 업데이트되었습니다.");
	}

	// 🔹 5. 인기 검색어 조회 (TOP 10)
	@GetMapping("/popular_list")
	public ResponseEntity<List<Map<String, Object>>> getPopularSearchList() {
		return ResponseEntity.ok(searchService.getPopularSearchList());
	}
}

package com.example.back.controller;

import com.example.back.dao.SearchDao;
import com.example.back.dao.UserDao;
import com.example.back.model.CustomUserDetails;
import com.example.back.model.User;
import com.example.back.service.JWTService;
import com.example.back.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

	@Autowired
	private SearchService searchService;

	// 🔹 1. 검색어 저장 (최근 검색어 + 인기 검색어 반영)

	/*
	 * @PostMapping("/save")
	 * public ResponseEntity<?> saveSearch(@RequestParam String searchTerm,
	 * 
	 * @RequestParam String searchType,
	 * 
	 * @AuthenticationPrincipal CustomUserDetails userDetails) {
	 * if (userDetails == null) { // 🔥 로그인하지 않은 경우 검색어 저장 금지
	 * return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
	 * }
	 * searchService.saveSearch(userDetails.getUserId(), searchTerm, searchType);
	 * return ResponseEntity.ok("검색어가 저장되었습니다.");
	 * }
	 */

	// private final User user;
	private final JWTService jwtService;
	private final UserDao userDao;
	private final SearchDao searchDao;

	/*
	 * @PostMapping("/save")
	 * public ResponseEntity<?> saveSearch(
	 * 
	 * @RequestHeader("Authorization") String token,
	 * 
	 * @RequestBody Map<String, String> request) {
	 * 
	 * String searchTerm = request.get("searchTerm");
	 * String searchType = request.get("searchType");
	 * 
	 * log.info("🔍 Received Token: {}", token);
	 * log.info("🔍 searchTerm: {}, searchType: {}", searchTerm, searchType);
	 * 
	 * if (token == null || !token.startsWith("Bearer ")) {
	 * return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	 * .body(Map.of("message", "❌ 인증되지 않은 요청입니다. 로그인 후 다시 시도하세요."));
	 * }
	 * 
	 * String user_id = jwtService.extractUserName(token.substring(7));
	 * log.info("🔍 Extracted User ID: {}", user_id);
	 * 
	 * if (user_id == null) {
	 * return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	 * .body(Map.of("message", "❌ 사용자 정보를 찾을 수 없습니다."));
	 * }
	 * 
	 * User user = userDao.findByUsername(user_id);
	 * if (user == null) {
	 * log.warn("❌ User not found: {}", user_id);
	 * return ResponseEntity.status(HttpStatus.NOT_FOUND)
	 * .body(Map.of("message", "❌ 사용자를 찾을 수 없습니다."));
	 * }
	 * 
	 * return ResponseEntity.ok(
	 * Map.of("message", "✅ 사용자 정보가 정상적으로 조회되었습니다.", "user_id", user_id,
	 * "user_name", user.getUser_name()));
	 * }
	 */

	@PostMapping("/save")
	public ResponseEntity<?> saveSearch(
			@RequestHeader("Authorization") String token,
			@RequestBody Map<String, String> request) {

		// ✅ 1. 요청 파라미터 추출
		String searchTerm = request.get("searchTerm");
		String searchType = request.get("searchType");

		log.info("🔍 Received Token: {}", token);
		log.info("🔍 searchTerm: {}, searchType: {}", searchTerm, searchType);

		// ✅ 2. 토큰 유효성 검사
		if (token == null || !token.startsWith("Bearer ")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "❌ 인증되지 않은 요청입니다. 로그인 후 다시 시도하세요."));
		}

		// ✅ 3. JWT에서 사용자 ID 추출
		String user_id = jwtService.extractUserName(token.substring(7));
		log.info("🔍 Extracted User ID: {}", user_id);

		if (user_id == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "❌ 사용자 정보를 찾을 수 없습니다."));
		}

		// ✅ 4. 사용자 정보 조회
		User user = userDao.findByUsername(user_id);
		if (user == null) {
			log.warn("❌ User not found: {}", user_id);
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "❌ 사용자를 찾을 수 없습니다."));
		}

		// ✅ 5. 검색어 저장 (원래 코드 기능 유지)
		// searchService.saveSearch(user.getUser_id(), searchTerm, searchType);
		log.info("✅ 검색어 저장 완료: user_no={}, searchTerm={}, searchType={}", user.getUser_no(), searchTerm, searchType);

		// ✅ 6. 응답 반환
		return ResponseEntity.ok(Map.of(
				"message", "✅ 검색어가 정상적으로 저장되었습니다.",
				"user_id", user_id,
				"user_name", user.getUser_name(),
				"searchTerm", searchTerm,
				"searchType", searchType));
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
	/*
	 * @DeleteMapping("/delete")
	 * public ResponseEntity<String> deleteSearch(@RequestParam String searchTerm,
	 * 
	 * @AuthenticationPrincipal CustomUserDetails userDetails) {
	 * if (userDetails == null) {
	 * return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
	 * }
	 * searchService.deleteSearch(userDetails.getUserId(), searchTerm);
	 * return ResponseEntity.ok("검색어가 삭제되었습니다.");
	 * }
	 */

	@DeleteMapping("/delete")
	public ResponseEntity<?> deleteSearch(
			@RequestHeader("Authorization") String token,
			@RequestBody Map<String, String> request) {

		// ✅ 1. 요청 파라미터 추출
		String searchTerm = request.get("searchTerm");

		log.info("🔍 Received Token: {}", token);
		log.info("🔍 searchTerm: {}", searchTerm);

		// ✅ 2. 토큰 유효성 검사
		if (token == null || !token.startsWith("Bearer ")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "❌ 인증되지 않은 요청입니다. 로그인 후 다시 시도하세요."));
		}

		// ✅ 3. JWT에서 사용자 ID 추출
		String user_id = null;
		try {
			user_id = jwtService.extractUserName(token.substring(7));
			log.info("🔍 Extracted User ID: {}", user_id);
		} catch (Exception e) {
			log.error("❌ JWT 파싱 중 오류 발생: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "❌ 잘못된 토큰입니다."));
		}

		if (user_id == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("message", "❌ 사용자 정보를 찾을 수 없습니다."));
		}

		// ✅ 4. 사용자 정보 조회
		User user = userDao.findByUsername(user_id);
		if (user == null) {
			log.warn("❌ User not found: {}", user_id);
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "❌ 사용자를 찾을 수 없습니다."));
		}

		int deleteResult = searchService.deleteSearch(user.getUser_id(), searchTerm); // ✅ 수정된 메서드 호출

		if (deleteResult > 0) {
			log.info("✅ 검색어 삭제 완료: user_id={}, searchTerm={}", user_id, searchTerm);
			return ResponseEntity.ok(Map.of("message", "✅ 검색어가 성공적으로 삭제되었습니다."));
		} else {
			log.warn("❌ 검색어 삭제 실패: user_id={}, searchTerm={}", user_id, searchTerm);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", "❌ 검색어 삭제에 실패했습니다."));
		}
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

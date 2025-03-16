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

	// 🔹 검색 기록 저장 및 인기 검색어 업데이트
	@Transactional
	public int saveSearch(Map<String, Object> sMap) {
		log.info("🔍 saveSearch 호출 성공 | 파라미터: {}", sMap);

		// 🔹 사용자의 최근 검색어 가져오기
		String lastSearchTerm = searchDao.getLastSearchTerm(sMap);
		log.info("✅ 최근 검색어: {}", lastSearchTerm);

		// 1. 검색 기록 저장
		int result1 = searchDao.insertSearchHistory(sMap);
		if (result1 != 1) {
			throw new RuntimeException("❌ 검색 기록 저장 실패");
		}

		// 2. 인기 검색어 존재 여부 확인
		int count = searchDao.checkPopularSearchExists(sMap);

		int result2 = -1;
		if (count > 0) {
			// 3. 이미 존재하는 검색어면 검색 횟수 증가
			result2 = searchDao.updatePopularSearchCount(sMap);
		} else {
			// 4. 존재하지 않는 검색어면 새로 추가
			result2 = searchDao.insertPopularSearch(sMap);
		}

		if (result2 != 1) {
			throw new RuntimeException("❌ 인기 검색어 업데이트 실패");
		}

		log.info("✅ saveSearch 완료");  // 🔹 무한 로딩 방지용 로그 추가
		return 1; // ✅ 정상적으로 `1` 반환
	}


	// 🔹 인기 검색어 조회
	public List<Map<String, Object>> getPopularSearches() { // ✅ 반환 타입 수정 (List<Map<String, Object>>)
		log.info("🔍 getPopularSearches 호출 성공");
		List<Map<String, Object>> list = searchDao.getPopularSearches();
		log.info("✅ 인기 검색어 개수: {}", (list != null ? list.size() : 0));
		return list; // ✅ JSON 변환을 컨트롤러에서 수행
	}


	// 🔹 자동 완성 검색어 조회
	public List<String> getSearchSuggestions(String searchTerm) {
		log.info("🔍 getSearchSuggestions 호출 성공 | 검색어: {}", searchTerm);

		if (searchTerm == null || searchTerm.trim().isEmpty()) {
			log.warn("⚠️ 검색어가 비어 있음! 자동 완성 검색어 조회 중단.");
			return List.of(); // ✅ 빈 리스트 반환하여 NPE 방지
		}

		List<String> list = searchDao.getSearchSuggestions(searchTerm);
		log.info("✅ 자동 완성 검색어 개수: {}", (list != null ? list.size() : 0));

		return list != null ? list : List.of(); // ✅ NPE 방지
	}


}

package com.example.back.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/places")
public class GooglePlacesController {

	private final WebClient webClient = WebClient.builder()
			.baseUrl("https://maps.googleapis.com/maps/api")
			.defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
			.defaultHeader(HttpHeaders.ACCEPT, "application/json")
			.defaultHeader(HttpHeaders.ACCEPT_LANGUAGE, "ko")
			.build();

	@Value("${google.places.api-key}")
	private String apiKey;
	private final RestTemplate restTemplate = new RestTemplate();

	/**
	 * 🔹 1. 자동완성 (Autocomplete)
	 */
	@GetMapping("/autocomplete")
	public Mono<String> getAutocomplete(@RequestParam String input) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/place/autocomplete/json")
						.queryParam("input", input)
						.queryParam("language", "ko")
						.queryParam("key", apiKey)
						.build())
				.retrieve()
				.onStatus(status -> status.isError(), clientResponse ->
						clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
							System.err.println("❌ API 오류 발생: " + errorBody);
							return Mono.error(new RuntimeException("Google API 오류: " + errorBody));
						})
				)
				.bodyToMono(String.class);
	}

	/**
	 * 🔹 2. 장소 검색 (Nearby Search)
	 */
	@GetMapping("/nearby_search")
	public Mono<String> getNearbySearch(@RequestParam String location,
	                                    @RequestParam int radius,
	                                    @RequestParam String type) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/place/nearbysearch/json")
						.queryParam("location", location)
						.queryParam("radius", radius)
						.queryParam("type", type)
						.queryParam("key", apiKey)
						.queryParam("language", "ko")
						.build())
				.retrieve()
				.bodyToMono(String.class);
	}

	/**
	 * 🔹 3. 장소 상세 정보 조회 (Place Details)
	 */
	@GetMapping("/place_details")
	public Mono<String> getPlaceDetails(@RequestParam String placeId) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/place/details/json")
						.queryParam("place_id", placeId)
						.queryParam("key", apiKey)
						.queryParam("language", "ko")
						.build())
				.retrieve()
				.bodyToMono(String.class);
	}

	/**
	 * 🔹 4. 장소 사진 조회 (Place Photos) - place_id 기반으로 자동 photo_reference 조회
	 */
	@GetMapping("/place_photo")
	public ResponseEntity<byte[]> getPlacePhoto(@RequestParam String placeId,
	                                            @RequestParam(defaultValue = "400") int maxWidth) {
		System.out.println("✅ 백엔드에서 받은 placeId: " + placeId);

		if (placeId == null || placeId.isEmpty()) {
			return ResponseEntity.badRequest().body("❌ placeId 값이 유효하지 않습니다.".getBytes());
		}

		// 🔹 1️⃣ Place Details API 호출 → photo_reference 가져오기
		String detailsUrl = String.format(
				"https://maps.googleapis.com/maps/api/place/details/json?place_id=%s&fields=photos&key=%s",
				URLEncoder.encode(placeId, StandardCharsets.UTF_8), apiKey);

		ResponseEntity<Map> detailsResponse = restTemplate.getForEntity(detailsUrl, Map.class);

		// 🔹 2️⃣ 응답 검증 (result 키 확인)
		Map<String, Object> responseBody = detailsResponse.getBody();
		if (responseBody == null || !responseBody.containsKey("result")) {
			return ResponseEntity.badRequest().body("❌ 유효하지 않은 placeId입니다.".getBytes());
		}

		// 🔹 3️⃣ result 내 photos 배열 존재 여부 확인
		Map<String, Object> result = (Map<String, Object>) responseBody.get("result");
		if (!result.containsKey("photos")) {
			return ResponseEntity.badRequest().body("❌ 해당 장소에는 사진이 없습니다.".getBytes());
		}

		// 🔹 4️⃣ 첫 번째 사진의 photo_reference 가져오기
		List<Map<String, Object>> photos = (List<Map<String, Object>>) result.get("photos");
		String photoReference = (String) photos.get(0).get("photo_reference");

		System.out.println("✅ 가져온 photo_reference: " + photoReference);

		// 🔹 5️⃣ 최종 사진 URL 생성
		String photoUrl = String.format(
				"https://maps.googleapis.com/maps/api/place/photo?maxwidth=%d&photo_reference=%s&key=%s",
				maxWidth, URLEncoder.encode(photoReference, StandardCharsets.UTF_8), apiKey);

		System.out.println("✅ 요청 URL: " + photoUrl);

		// 🔹 6️⃣ 사진 요청 후 Redirect 처리
		ResponseEntity<Void> initialResponse = restTemplate.exchange(photoUrl, HttpMethod.GET, null, Void.class);
		if (initialResponse.getStatusCode().is3xxRedirection()) {
			String redirectedUrl = initialResponse.getHeaders().getLocation().toString();
			System.out.println("✅ 리디렉션된 이미지 URL: " + redirectedUrl);

			ResponseEntity<byte[]> imageResponse = restTemplate.exchange(redirectedUrl, HttpMethod.GET, null, byte[].class);
			return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageResponse.getBody());
		}

		return ResponseEntity.badRequest().build();
	}


	/**
	 * 🔹 5. 주소 → 좌표 변환 (Geocoding)
	 */
	@GetMapping("/geocode")
	public Mono<String> getGeocode(@RequestParam String address) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/geocode/json")
						.queryParam("address", address)
						.queryParam("key", apiKey)
						.queryParam("language", "ko")
						.build())
				.retrieve()
				.onStatus(status -> status.isError(), clientResponse ->
						clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
							System.err.println("❌ API 오류 발생: " + errorBody);
							return Mono.error(new RuntimeException("Google API 오류: " + errorBody));
						})
				)
				.bodyToMono(String.class);
	}

	/**
	 * 🔹 6. 좌표 → 주소 변환 (Reverse Geocoding)
	 */
	@GetMapping("/reverse_geocode")
	public Mono<String> getReverseGeocode(@RequestParam String latlng) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/geocode/json")
						.queryParam("latlng", latlng)
						.queryParam("key", apiKey)
						.queryParam("language", "ko")
						.build())
				.retrieve()
				.bodyToMono(String.class);
	}
}

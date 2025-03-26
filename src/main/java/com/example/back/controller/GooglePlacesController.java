package com.example.back.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;

@RestController
@RequestMapping("/api/places")
public class GooglePlacesController {

	private final WebClient webClient;

	@Value("${google.places.api.key}")
	private String apiKey;

	public GooglePlacesController(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder
				.baseUrl("https://maps.googleapis.com/maps/api")
				.defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
				.defaultHeader(HttpHeaders.ACCEPT, "application/json")
				.defaultHeader(HttpHeaders.ACCEPT_LANGUAGE, "ko")
				.build();
	}

	/**
	 * 🔹 1. 자동완성 (Autocomplete) - 중복 예외 처리 제거
	 */
	@GetMapping("/autocomplete")
	public Mono<ResponseEntity<List<Map<String, String>>>> getAutocomplete(@RequestParam String input) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/place/autocomplete/json")
						.queryParam("input", input)
						.queryParam("language", "ko")
						.queryParam("key", apiKey)
						.build())
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
				})
				.flatMap(responseBody -> {
					List<Map<String, Object>> predictions = (List<Map<String, Object>>) responseBody.getOrDefault("predictions", new ArrayList<>());
					List<Map<String, String>> result = new ArrayList<>();

					for (Map<String, Object> prediction : predictions) {
						Map<String, String> map = new HashMap<>();
						map.put("description", String.valueOf(prediction.get("description")));
						map.put("place_id", String.valueOf(prediction.get("place_id")));
						result.add(map);
					}

					return Mono.just(ResponseEntity.ok().body(result));
				})
				.onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(List.of())));
	}


	/**
	 * 🔹 2. 장소 검색 (Nearby Search) - 'type' 기본값 추가
	 */
	@GetMapping("/nearby_search")
	public Mono<String> getNearbySearch(@RequestParam String location,
	                                    @RequestParam int radius,
	                                    @RequestParam(defaultValue = "tourist_attraction") String type) {
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
	public Mono<String> getPlaceDetails(@RequestParam String place_id) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/place/details/json")
						.queryParam("place_id", place_id)
						.queryParam("key", apiKey)
						.queryParam("language", "ko")
						.build())
				.retrieve()
				.bodyToMono(String.class);
	}

	/**
	 * 🔹 4. 장소 사진 조회 (Place Photos) - photo_reference 캐싱 최적화
	 */
	@GetMapping("/place_photo")
	public Mono<ResponseEntity<byte[]>> getPlacePhoto(@RequestParam String place_id,
	                                                  @RequestParam(defaultValue = "400") int maxWidth) {
		String detailsUrl = String.format(
				"https://maps.googleapis.com/maps/api/place/details/json?place_id=%s&fields=photos&key=%s",
				URLEncoder.encode(place_id, StandardCharsets.UTF_8), apiKey);

		return webClient.get()
				.uri(detailsUrl)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
				})
				.flatMap(responseBody -> {
					if (responseBody == null || !responseBody.containsKey("result")) {
						System.err.println("❌ 유효하지 않은 place_id: " + place_id);
						return Mono.just(ResponseEntity.badRequest().body("❌ 유효하지 않은 place_id입니다.".getBytes()));
					}

					Map<String, Object> result = (Map<String, Object>) responseBody.get("result");
					if (!result.containsKey("photos")) {
						System.err.println("❌ 사진이 없는 장소입니다: " + place_id);
						return Mono.just(ResponseEntity.badRequest().body("❌ 해당 장소에는 사진이 없습니다.".getBytes()));
					}

					List<Map<String, Object>> photos = (List<Map<String, Object>>) result.get("photos");
					if (photos.isEmpty()) {
						System.err.println("❌ 사진이 없는 장소입니다: " + place_id);
						return Mono.just(ResponseEntity.badRequest().body("❌ 해당 장소에는 사진이 없습니다.".getBytes()));
					}

					String photoReference = (String) photos.get(0).get("photo_reference");
					if (photoReference == null || photoReference.isEmpty()) {
						System.err.println("❌ 유효하지 않은 photo_reference");
						return Mono.just(ResponseEntity.badRequest().body("❌ 유효하지 않은 photo_reference 입니다.".getBytes()));
					}

					String photoUrl = String.format(
							"https://maps.googleapis.com/maps/api/place/photo?maxwidth=%d&photo_reference=%s&key=%s",
							maxWidth, URLEncoder.encode(photoReference, StandardCharsets.UTF_8), apiKey);

					System.out.println("✅ 요청 URL: " + photoUrl);

					return webClient.get()
							.uri(photoUrl)
							.retrieve()
							.bodyToMono(byte[].class)
							.map(imageData -> ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageData))
							.onErrorResume(e -> {
								System.err.println("❌ 사진 요청 실패: " + e.getMessage());
								return Mono.just(ResponseEntity.status(500).body("❌ 사진 요청 실패".getBytes()));
							});
				});
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

	/**
	 * 🔹 7. 추천된 여행 코스 (Google Directions API)
	 */
	@GetMapping("/recommend_route")
	public Mono<String> getRecommendedRoute(@RequestParam String origin,
	                                        @RequestParam String destination,
	                                        @RequestParam(required = false) String waypoints,
														@RequestParam(defaultValue = "transit") String mode) {
		return webClient.get()
				.uri(uriBuilder -> {
					var uri = uriBuilder.path("/directions/json")
							.queryParam("origin", origin)
							.queryParam("destination", destination)
							.queryParam("mode", mode)
							.queryParam("key", apiKey)
							.queryParam("language", "ko");

					if (waypoints != null) {
						uri.queryParam("waypoints", waypoints.replace("|", "%7C"));
					}

					return uri.build();
				})
				.retrieve()
				.bodyToMono(String.class)
				.flatMap(response -> {
					if (response.contains("\"status\" : \"ZERO_RESULTS\"")) {
						return webClient.get()
								.uri(uriBuilder -> uriBuilder.path("/directions/json")
										.queryParam("origin", origin)
										.queryParam("destination", destination)
										.queryParam("mode", mode)
										.queryParam("key", apiKey)
										.queryParam("language", "ko")
										.build())
								.retrieve()
								.bodyToMono(String.class);
					}
					return Mono.just(response);
				});
	}
}
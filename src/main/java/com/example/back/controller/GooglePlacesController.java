package com.example.back.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
	@GetMapping("/nearby-search")
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
	@GetMapping("/place-details")
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
	 * 🔹 4. 장소 사진 조회 (Place Photos)
	 */
	@GetMapping("/place-photo")
	public String getPlacePhoto(@RequestParam String photoReference,
	                            @RequestParam(defaultValue = "400") int maxWidth) {
		return "https://maps.googleapis.com/maps/api/place/photo"
				+ "?maxwidth=" + maxWidth
				+ "&photo_reference=" + photoReference
				+ "&key=" + apiKey;
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
	@GetMapping("/reverse-geocode")
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

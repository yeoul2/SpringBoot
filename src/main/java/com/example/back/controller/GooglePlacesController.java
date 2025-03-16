package com.example.back.controller;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/places")
public class GooglePlacesController {

	@Value("${google.places.api-key}")
	private String apiKey;

	private final OkHttpClient client = new OkHttpClient();

	/**
	 * 🔹 1. 자동완성 (Autocomplete)
	 */
	@GetMapping("/autocomplete")
	public String getAutocomplete(@RequestParam String input) throws IOException {
		// ✅ 한글 입력을 URL 인코딩하여 안전하게 전달
		String encodedInput = URLEncoder.encode(input, StandardCharsets.UTF_8);

		String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json"
				+ "?input=" + encodedInput
				+ "&language=ko"
				+ "&key=" + apiKey;

		System.out.println("🚀 API 요청 URL: " + url);

		Request request = new Request.Builder()
				.url(url)
				.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
				.header("Accept", "application/json")
				.header("Accept-Language", "ko")
				.build();

		try (Response response = client.newCall(request).execute()) {
			String responseBody = response.body().string();
			System.out.println("📌 응답: " + responseBody);
			return responseBody;
		}
	}

	/**
	 * 🔹 2. 장소 검색 (Nearby Search)
	 */
	@GetMapping("/nearbysearch")
	public String getNearbySearch(@RequestParam String location, @RequestParam int radius, @RequestParam String type) throws IOException {
		String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
				+ "?location=" + location
				+ "&radius=" + radius
				+ "&type=" + type
				+ "&key=" + apiKey
				+ "&language=ko";

		Request request = new Request.Builder().url(url).build();
		try (Response response = client.newCall(request).execute()) {
			return response.body().string();
		}
	}

	/**
	 * 🔹 3. 장소 상세 정보 조회 (Place Details)
	 */
	@GetMapping("/details")
	public String getPlaceDetails(@RequestParam String placeId) throws IOException {
		String url = "https://maps.googleapis.com/maps/api/place/details/json"
				+ "?place_id=" + placeId
				+ "&key=" + apiKey
				+ "&language=ko";

		Request request = new Request.Builder().url(url).build();
		try (Response response = client.newCall(request).execute()) {
			return response.body().string();
		}
	}

	/**
	 * 🔹 4. 장소 사진 조회 (Place Photos)
	 */
	@GetMapping("/photo")
	public String getPlacePhoto(@RequestParam String photoReference, @RequestParam(defaultValue = "400") int maxWidth) {
		return "https://maps.googleapis.com/maps/api/place/photo"
				+ "?maxwidth=" + maxWidth
				+ "&photo_reference=" + photoReference
				+ "&key=" + apiKey;
	}

	/**
	 * 🔹 5. 주소 → 좌표 변환 (Geocoding)
	 */
	@GetMapping("/geocode")
	public String getGeocode(@RequestParam String address) throws IOException {
		String url = "https://maps.googleapis.com/maps/api/geocode/json"
				+ "?address=" + URLEncoder.encode(address, StandardCharsets.UTF_8)
				+ "&key=" + apiKey
				+ "&language=ko";

		Request request = new Request.Builder().url(url).build();
		try (Response response = client.newCall(request).execute()) {
			return response.body().string();
		}
	}

	/**
	 * 🔹 6. 좌표 → 주소 변환 (Reverse Geocoding)
	 */
	@GetMapping("/reverse-geocode")
	public String getReverseGeocode(@RequestParam String latlng) throws IOException {
		String url = "https://maps.googleapis.com/maps/api/geocode/json"
				+ "?latlng=" + latlng
				+ "&key=" + apiKey
				+ "&language=ko";

		Request request = new Request.Builder().url(url).build();
		try (Response response = client.newCall(request).execute()) {
			return response.body().string();
		}
	}
}

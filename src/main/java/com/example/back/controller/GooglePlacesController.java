package com.example.back.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.springframework.core.ParameterizedTypeReference;

@RestController
@RequestMapping("/api/places")
public class GooglePlacesController {

  private final WebClient webClient;

  @Value("${google.places.api.key}")
  private String apiKey;

  public GooglePlacesController(WebClient.Builder webClientBuilder) {
    // ✅ WebClient 기본 설정 (Google API 요청용)
    this.webClient = webClientBuilder
        .baseUrl("https://maps.googleapis.com/maps/api")
        .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
        .defaultHeader(HttpHeaders.ACCEPT, "application/json")
        .defaultHeader(HttpHeaders.ACCEPT_LANGUAGE, "ko")
        .build();
  }

  /**
   * 🔹 1. 자동완성 검색
   * 사용자의 입력어(input)를 기반으로 장소 자동완성 목록을 반환
   *
   * @param input 사용자 입력 문자열
   * @return 장소 설명(description)과 place_id 리스트 반환
   */
  @GetMapping("/autocomplete")
  public Mono<ResponseEntity<List<Map<String, String>>>> getAutocomplete(@RequestParam(name = "input") String input) {
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
   * 🔹 2. 장소 검색 (Nearby Search)
   * 특정 위치 주변에서 지정된 타입의 장소를 검색
   *
   * @param location 위도,경도 문자열 (예: "37.5665,126.9780")
   * @param radius   반경 (미터 단위)
   * @param type     검색할 장소 유형 (기본: 관광지)
   * @return Google Places Nearby Search 결과
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
   * 🔹 3. 장소 상세 정보 조회
   * place_id를 기반으로 해당 장소의 상세 정보 반환
   *
   * @param place_id Google에서 제공하는 장소 ID
   * @return 상세 정보 JSON 문자열
   */
  @GetMapping("/place_details")
  public Mono<String> getPlaceDetails(@RequestParam("place_id") String place_id) {
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
   * 🔹 4. 장소 사진 조회
   * place_id를 기준으로 사진을 조회하여 byte[]로 반환
   *
   * @param place_id 장소 ID
   * @param maxWidth 이미지 최대 너비 (기본값: 400)
   * @return 이미지 바이너리 데이터를 포함한 ResponseEntity
   */
  @GetMapping("/place_photo")
  public Mono<ResponseEntity<byte[]>> getPlacePhoto(@RequestParam String place_id,
                                                    @RequestParam(defaultValue = "400") int maxWidth) {
    // ✅ Step 1: 사진 정보 조회 (photo_reference 얻기)
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

          // ✅ Step 2: photo_reference로 이미지 요청
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
   *
   * @param address 실제 주소 문자열
   * @return 위도/경도 정보 포함한 JSON 문자열
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
   *
   * @param latlng "위도,경도" 형식의 좌표 문자열
   * @return 해당 좌표의 주소 정보
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
   * 장소명 기반으로 경유지를 포함한 경로 추천 요청
   *
   * @param origin      출발지 (장소명, 예: "서울")
   * @param destination 도착지 (장소명, 예: "부산")
   * @param waypoints   경유지 목록 (예: "대전|대구"), 선택 사항
   * @param mode        이동 수단 (기본값 "transit")
   * @return Directions API 응답(JSON)
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
          // ❗경로가 없을 경우 경유지 없이 재시도
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

  /**
   * 🔹 8. 정확한 좌표 기반 실제 경로 요청
   * 지도에 라인으로 표시할 수 있는 경로 정보 반환
   *
   * @param origin      출발 좌표 (예: "37.5665,126.9780")
   * @param destination 도착 좌표 (예: "35.1796,129.0756")
   * @param mode        이동 수단 (기본값 "transit")
   * @return Directions API 응답(JSON)
   */
  @GetMapping("/route")
  public Mono<String> getRouteBetweenPoints(@RequestParam String origin,
                                            @RequestParam String destination,
                                            @RequestParam(defaultValue = "transit") String mode) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/directions/json")
            .queryParam("origin", origin)
            .queryParam("destination", destination)
            .queryParam("mode", mode)
            .queryParam("key", apiKey)
            .queryParam("language", "ko")
            .build())
        .retrieve()
        .bodyToMono(String.class)
        .onErrorResume(e -> {
          System.err.println("❌ Directions API 오류: " + e.getMessage());
          return Mono.just("{\"error\": \"경로 요청 실패\"}");
        });
  }
}

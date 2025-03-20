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

  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${google.places.api-key}")
  private String apiKey;

  /**
   * 🔹 1. 자동완성 (Autocomplete) - 중복 예외 처리 제거
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
        .bodyToMono(String.class);
  }

  /**
   * 🔹 2. 장소 검색 (Nearby Search) - 'type' 기본값 추가
   */
  @GetMapping("/nearby_search")
  public Mono<String> getNearbySearch(@RequestParam String location,
                                      @RequestParam int radius,
                                      @RequestParam(required = false, defaultValue = "tourist_attraction") String type) {
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
   * 🔹 4. 장소 사진 조회 (Place Photos) - photo_reference 캐싱 최적화
   */
  @GetMapping("/place_photo")
  public ResponseEntity<byte[]> getPlacePhoto(@RequestParam String placeId,
                                              @RequestParam(defaultValue = "400") int maxWidth) {
    String detailsUrl = String.format(
        "https://maps.googleapis.com/maps/api/place/details/json?place_id=%s&fields=photos&key=%s",
        URLEncoder.encode(placeId, StandardCharsets.UTF_8), apiKey);

    ResponseEntity<Map> detailsResponse = restTemplate.getForEntity(detailsUrl, Map.class);
    Map<String, Object> responseBody = detailsResponse.getBody();

    if (responseBody == null || !responseBody.containsKey("result")) {
      System.err.println("❌ 유효하지 않은 placeId: " + placeId);
      return ResponseEntity.badRequest().body("❌ 유효하지 않은 placeId입니다.".getBytes());
    }

    // ✅ 사진 정보가 없을 경우 예외 처리
    Map<String, Object> result = (Map<String, Object>) responseBody.get("result");
    if (!result.containsKey("photos")) {
      System.err.println("❌ 사진이 없는 장소입니다: " + placeId);
      return ResponseEntity.badRequest().body("❌ 해당 장소에는 사진이 없습니다.".getBytes());
    }

    List<Map<String, Object>> photos = (List<Map<String, Object>>) result.get("photos");
    if (photos.isEmpty()) {
      System.err.println("❌ 사진이 없는 장소입니다: " + placeId);
      return ResponseEntity.badRequest().body("❌ 해당 장소에는 사진이 없습니다.".getBytes());
    }

    String photoReference = (String) photos.get(0).get("photo_reference");

    String photoUrl = String.format(
        "https://maps.googleapis.com/maps/api/place/photo?maxwidth=%d&photo_reference=%s&key=%s",
        maxWidth, URLEncoder.encode(photoReference, StandardCharsets.UTF_8), apiKey);

    System.out.println("✅ 요청 URL: " + photoUrl);

    ResponseEntity<byte[]> imageResponse = restTemplate.exchange(photoUrl, HttpMethod.GET, null, byte[].class);
    return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageResponse.getBody());
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
  public ResponseEntity<Mono<String>> getRecommendedRoute(@RequestParam String origin,
                                                          @RequestParam String destination,
                                                          @RequestParam(required = false) String waypoints) {
    try {
      // ✅ 주소 → 좌표 변환 (좌표 변환 실패 시 null 체크)
      final String processedOrigin = getCoordinates(origin);
      final String processedDestination = getCoordinates(destination);
      final String processedWaypoints = (waypoints != null) ? getCoordinates(waypoints) : null;

      if (processedOrigin == null || processedDestination == null) {
        return ResponseEntity.badRequest().body(Mono.just("❌ 출발지 또는 목적지 좌표 변환 실패"));
      }
      if (processedWaypoints == null && waypoints != null) {
        return ResponseEntity.badRequest().body(Mono.just("❌ 경유지 좌표 변환 실패"));
      }

      return ResponseEntity.ok(
          webClient.get()
              .uri(uriBuilder -> {
                var uri = uriBuilder.path("/directions/json")
                    .queryParam("origin", processedOrigin)
                    .queryParam("destination", processedDestination)
                    .queryParam("mode", "driving") // ✅ 자동차 모드 우선
                    .queryParam("key", apiKey)
                    .queryParam("language", "ko");

                if (processedWaypoints != null) {
                  uri.queryParam("waypoints", processedWaypoints.replace("|", "%7C"));
                }

                return uri.build();
              })
              .retrieve()
              .bodyToMono(String.class)
              .flatMap(response -> {
                if (response.contains("\"status\" : \"ZERO_RESULTS\"")) {
                  System.err.println("🚨 자동차 경로를 찾을 수 없음. 대중교통 모드로 재시도...");
                  return webClient.get()
                      .uri(uriBuilder -> uriBuilder.path("/directions/json")
                          .queryParam("origin", processedOrigin)
                          .queryParam("destination", processedDestination)
                          .queryParam("mode", "transit") // ✅ 대중교통 모드 재시도
                          .queryParam("key", apiKey)
                          .queryParam("language", "ko")
                          .build())
                      .retrieve()
                      .bodyToMono(String.class);
                }
                return Mono.just(response);
              })
      );

    } catch (Exception e) {
      System.err.println("❌ 경로 요청 실패: " + e.getMessage());
      return ResponseEntity.status(500).body(Mono.just("🚨 내부 서버 오류 발생"));
    }
  }  // ✅ `getRecommendedRoute()` 닫는 `}` 추가

  /**
   * 🔹 주소를 좌표로 변환하는 메서드 (반드시 클래스 내에서 따로 선언해야 함!)
   */
  private String getCoordinates(String address) {
    try {
      System.out.println("🔍 좌표 변환 요청: " + address);

      // ✅ Google Places API 활용
      String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
      String placesUrl = String.format("https://maps.googleapis.com/maps/api/place/textsearch/json?query=%s&key=%s", encodedAddress, apiKey);

      System.out.println("🔗 Google Places API 요청 URL: " + placesUrl);

      ResponseEntity<Map> response = restTemplate.getForEntity(placesUrl, Map.class);
      Map<String, Object> body = response.getBody();

      if (body != null) {
        System.out.println("📥 Google Places API 응답: " + body);

        if (body.containsKey("results")) {
          List<Map<String, Object>> results = (List<Map<String, Object>>) body.get("results");
          if (!results.isEmpty()) {
            Map<String, Object> location = (Map<String, Object>) ((Map<String, Object>) results.get(0).get("geometry")).get("location");
            String coordinates = location.get("lat") + "," + location.get("lng");
            System.out.println("✅ 변환된 좌표: " + coordinates);
            return coordinates;
          }
        }
      }

      System.err.println("❌ 주소 변환 실패: " + address);
      return null;

    } catch (Exception e) {
      System.err.println("🚨 Google Places API 요청 실패: " + e.getMessage());
      return null;
    }
  }
}
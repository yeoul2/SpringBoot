package com.example.back.controller;

import okhttp3.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GooglePlacesService {
  private static final String API_KEY = "AIzaSyDzcuFvvgFd-iNVJ48s4dToutKtTJpQKDw"; // ✅ Google API 키

  public static void main(String[] args) throws IOException {
    OkHttpClient client = new OkHttpClient();

    String input = "서울역";
    String encodedInput = URLEncoder.encode(input, StandardCharsets.UTF_8);  // ✅ URL 인코딩 적용
    String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json"
        + "?input=" + encodedInput
        + "&language=ko"
        + "&key=" + API_KEY;

    Request request = new Request.Builder()
        .url(url)
        .header("Accept", "application/json")
        .header("Accept-Language", "ko")
        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
        .build();

    try (Response response = client.newCall(request).execute()) {
      System.out.println("📌 요청 URL: " + url);
      System.out.println("📌 응답 코드: " + response.code());

      if (response.body() != null) {
        // ✅ UTF-8로 변환하여 올바르게 출력
        String responseBody = new String(response.body().bytes(), StandardCharsets.UTF_8);
        System.out.println("📌 응답 바디: " + responseBody);
      } else {
        System.out.println("❌ 응답 바디가 없습니다.");
      }
    }
  }
}

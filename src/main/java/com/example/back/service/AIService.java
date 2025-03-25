  package com.example.back.service;

  import com.example.back.model.TravelSchedule;
  import org.springframework.beans.factory.annotation.Value;
  import org.springframework.stereotype.Service;
  import org.springframework.web.reactive.function.client.WebClient;
  import reactor.core.publisher.Mono;

  @Service
  public class AIService {

    private final WebClient webClient;

    // AI 서버 URL을 application.properties 또는 application.yml에서 읽어오기
    @Value("${ai.server.url}")
    private String aiServerUrl;

    public AIService(WebClient.Builder webClientBuilder) {
      // WebClient를 사용하여 AI 서버와 통신
      this.webClient = webClientBuilder.baseUrl(aiServerUrl).build();
    }

    // 여행 일정 생성 요청
    public Mono<TravelSchedule> generateSchedule(String city, int days, int people, String style) {
      // AI 서버로 전송할 JSON 데이터 생성
      String requestJson = String.format("{\"city\": \"%s\", \"days\": %d, \"people\": %d, \"style\": \"%s\"}",
          city, days, people, style);

      // WebClient를 사용하여 비동기 POST 요청 보내기
      return this.webClient.post()
          .uri("/generate-schedule")  // AI 서버의 엔드포인트
          .header("Content-Type", "application/json")
          .bodyValue(requestJson)  // 요청 본문에 JSON 데이터 포함
          .retrieve()  // 응답 받기
          .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
              response -> Mono.error(new RuntimeException("AI 서버 응답 오류")))
          .bodyToMono(TravelSchedule.class);  // 응답을 TravelSchedule DTO로 파싱
    }
  }

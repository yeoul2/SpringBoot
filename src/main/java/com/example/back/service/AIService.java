package com.example.back.service;

import com.example.back.model.ScheduleRequest;
import com.example.back.model.TravelSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AIService {

	private final WebClient.Builder webClientBuilder;

	@Value("${ai.server.url}")
	private String aiServerUrl;

	public Mono<List<TravelSchedule>> generateSchedule(String city, int days, int people, String style) {
		// 요청 DTO 생성
		ScheduleRequest request = new ScheduleRequest(city, days, people, style);

		// WebClient 생성 후 FastAPI 호출
		WebClient client = webClientBuilder.baseUrl(aiServerUrl).build();

		return client.post()
				.uri("/generate-schedule")
				.header("Content-Type", "application/json")
				.bodyValue(request)
				.retrieve()
				.onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
						response -> Mono.error(new RuntimeException("AI 서버 응답 오류")))
				.bodyToMono(new ParameterizedTypeReference<List<TravelSchedule>>() {
				});
	}
}

package com.example.back.controller;

import com.example.back.model.ScheduleRequest;
import com.example.back.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

  private final AIService aiService;

  public ScheduleController(AIService aiService) {
    this.aiService = aiService;
  }

  @PostMapping("/generate")
  public ResponseEntity<Object> generateSchedule(@RequestBody ScheduleRequest request) {
    return aiService.generateSchedule(
            request.getCity(),
            request.getDays(),
            request.getPeople(),
            request.getStyle()
        )
        .map(schedule -> ResponseEntity.ok((Object)schedule))  // 정상 응답일 때 TravelSchedule을 포함한 ResponseEntity
        .defaultIfEmpty(ResponseEntity.badRequest().body("❌ AI 서버 오류"))  // 실패 시 String을 포함한 ResponseEntity
        .block(); // Mono를 동기적으로 처리
  }
}

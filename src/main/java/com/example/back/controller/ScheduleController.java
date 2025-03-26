package com.example.back.controller;

import com.example.back.model.ScheduleRequest;
import com.example.back.model.TravelSchedule;
import com.example.back.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

  private final AIService aiService;

  public ScheduleController(AIService aiService) {
    this.aiService = aiService;
  }

  @PostMapping("/generate")
  public ResponseEntity<List<TravelSchedule>> generateSchedule(@RequestBody ScheduleRequest request) {
    return aiService.generateSchedule(
            request.getCity(),
            request.getDays(),
            request.getPeople(),
            request.getStyle()
        )
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.badRequest().build())
        .block();
  }
}

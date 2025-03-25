package com.example.back.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ScheduleRequest {
  private String city; // 여행 도시
  private int days;    // 여행 일수
  private int people;  // 여행 인원 수
  private String style; // 여행 스타일 (예: 모험, 휴식 등)
}

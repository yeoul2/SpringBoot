package com.example.back.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TravelSchedule {
  // Getter 및 Setter 메서드 추가
  private String city; // 여행 도시
  private int days;    // 여행 일수
  private int people;  // 여행 인원 수
  private String scheduleDetails; // 일정 상세 (예시)

}

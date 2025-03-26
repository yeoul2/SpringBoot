package com.example.back.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 🧾 ScheduleRequest
 * - 프론트엔드에서 AI 일정 생성을 요청할 때 사용하는 DTO 클래스입니다.
 * - 사용자의 입력값 (도시, 날짜, 인원, 스타일)을 기반으로 FastAPI 서버에 전달됩니다.
 */
@Data // 🔹 Getter, Setter, toString 등 자동 생성
@AllArgsConstructor // 🔹 전체 필드를 매개로 받는 생성자 자동 생성
@NoArgsConstructor // 🔹 기본 생성자 자동 생성
public class ScheduleRequest {
  /**
   * 📍 여행할 도시 또는 국가 이름
   * 예: "도쿄", "프랑스", "서울"
   */
  private String city;

  /**
   * 📆 여행 일수
   * 예: 3 → 3일 일정
   */
  private int days;

  /**
   * 👥 여행 인원 수
   * 예: 2 → 2명이서 여행
   */
  private int people;

  /**
   * 🎨 여행 스타일 (optional)
   * 예: "맛집", "휴양", "문화", "쇼핑"
   * 없을 경우 AI가 자동 추천
   */
  private String style;
}
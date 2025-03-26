package com.example.back.model;

import lombok.Data;

import java.util.List;

@Data
public class TravelSchedule {
  private String day; // 🔹 각 DAY를 의미 (예: "DAY 1", "DAY 2" 등)
  private List<ScheduleItem> activities; // 🔹 해당 DAY의 일정(activity) 목록

  @Data
  public static class ScheduleItem {
    private String time; // 🔹 일정 시작 시간 (예: "09:00")
    private String title; // 🔹 장소 이름 (예: "경복궁")
    private String desc;// 🔹 장소 설명 (예: "조선시대 궁궐 방문")
    private String from; // 🔹 출발 지점 이름 (예: "호텔")
    private String to; // 🔹 도착 지점 이름 (예: "경복궁")
    private String moveType; // 🔹 이동 수단 (예: "도보", "지하철", "버스", "자차")
    private String duration; // 🔹 이동 소요 시간 (예: "15분")
    private double latitude; // 🔹 해당 장소의 위도
    private double longitude; // 🔹 해당 장소의 경도
  }
}

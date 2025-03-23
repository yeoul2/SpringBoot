package com.example.back.model;

import lombok.Data;

import java.time.LocalTime;

@Data
public class CourseDetail {
    private int cdt_no; // 코스 상세 번호 (PK)
    private int cs_no; // 코스 번호 (FK)
    private String cdt_place; // 장소 이름
    private String cdt_place_type; // 장소 유형 (예: 관광지, 음식점)
    private int cdt_time_car; // 이동 시간 (차량)
    private int cdt_time_public; // 이동 시간 (대중교통)
    private LocalTime cdt_time;       // 방문 시간 (LocalTime으로 처리)
    private int cdt_day;              // 몇 번째 날

}

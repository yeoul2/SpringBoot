package com.example.back.model;

import java.time.LocalTime;

import lombok.Data;

@Data
public class CourseDetail {
    private LocalTime cdt_time;//코스디테일 시간
    private int cdt_no; // 코스 상세 번호 (PK)
    private int cs_no; // 코스 번호 (FK)
    private String cdt_place; // 장소 이름
    private String cdt_place_type; // 장소 유형 (예: 관광지, 음식점)
    private int cdt_time_car; // 이동 시간 (차량)
    private int cdt_time_public; // 이동 시간 (대중교통)
}

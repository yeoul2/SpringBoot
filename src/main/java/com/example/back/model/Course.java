package com.example.back.model;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class Course {
    private int cs_no; // 코스 번호 (PK)
    private String user_id; // 사용자 아이디 (FK)
    private String cs_name; // 코스 이름
    private String cs_country; // 여행 국가
    private String cs_city; // 여행 도시
    private Date cs_departure_date; // 출발 날짜
    private Date cs_return_date; // 복귀 날짜
    private int cs_people_num; // 여행 인원 수
    private String cs_theme; // 여행 테마
    private int cs_like_count; // 좋아요 개수

    // ✅ 코스 상세 정보 리스트 (1:N 관계)
    private List<CourseDetail> details;
}

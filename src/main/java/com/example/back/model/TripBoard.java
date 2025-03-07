package com.example.back.model;

import lombok.Data;

@Data
public class TripBoard {
   private int    tb_no       = 0;     // 번호
   private String tb_title    = "";    // 제목
   private String tb_place    = "";    // 장소
   private String tb_date     = "";    // 날짜(가는날짜)
   private String tb_date2    = "";    // 날짜(오는날짜)
   private double tb_star     = 0.0;   // 만족도
   private String tb_photo1   = "" ;   // 사진1
   private String tb_photo2   = "" ;   // 사진2
   private String tb_photo3   = "" ;   // 사진3
   private String tb_review   = "" ;   // 여행리뷰
   private String tb_public   = "" ;   // 공개설정("Y","N")
   private int    tb_likes    = 0 ;    // 좋아요
   private String user_id     = "" ;   // 아이디

}
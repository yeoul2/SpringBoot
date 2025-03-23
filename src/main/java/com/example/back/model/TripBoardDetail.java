package com.example.back.model;

import lombok.Data;

import java.time.LocalTime;

@Data
public class TripBoardDetail {
   private LocalTime tbd_time;
   private String tbd_place_type;
   private String tbd_place;
   private String tbd_content;
   private int tbd_time_car;
   private int tbd_time_public;
   private int tbd_day;
   private int tbd_no; // 필요하면
}

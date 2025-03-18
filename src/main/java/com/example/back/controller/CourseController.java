package com.example.back.controller;

import com.example.back.model.Course;
import com.example.back.model.CourseDetail;
import com.example.back.service.CourseService;
import com.example.back.utils.LocalDateTimeAdapter;
import com.example.back.utils.LocalTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;// GsonBuilder 추가(날짜 변환 처리)
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Log4j2
@RestController // Spring Boot REST API 컨트롤러
@RequestMapping("/api/course/") // API 기본 URL 설정
public class CourseController {

   @Autowired
   private CourseService courseService; // 코스 서비스 의존성 주입

      // Gson에 LocalDateTime 처리 추가
   private final Gson gson = new GsonBuilder()
           .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()) // LocalDateTime을 처리하는 TypeAdapter 등록
           .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
           .setDateFormat("yyyy-MM-dd") // 🟩 날짜 변환 설정 추가 (Date 변환 이슈 해결)
            .create();


   // ✅ 전체 코스 조회 (상세 정보 포함)
   @GetMapping("list")
   public String getCourseList(@RequestParam Map<String, Object> paramMap) {
      log.info("getCourseList 호출 성공");
      List<Map<String, Object>> list = courseService.getCourseList(paramMap);
      
      // 🟥 기존 코드: Gson 객체를 메서드 내부에서 생성
      // Gson g = new Gson();
      // return g.toJson(list);
      
      return gson.toJson(list); // 🟩 gson 사용하도록 변경
}

    // ✅ 특정 코스 조회 (상세 정보 포함)
   @GetMapping("detail")
   public String courseDetail(@RequestParam Map<String, Object> cmap) {   
      log.info("courseDetail 호출 성공: " + cmap);
      List<Map<String, Object>> course = courseService.courseDetail(cmap);
      
      // 🟥 기존 코드: 메서드 내부에서 Gson 객체 생성
      // Gson g = new Gson();
      // return g.toJson(course);
      
      return gson.toJson(course); // 🟩 gson 사용하도록 변경
   }

   // ✅ 코스 + 상세 정보 함께 추가
   @PostMapping("insert")
   public int courseInsert(@RequestBody List<Map<String, Object>> requestData) {
      log.info("courseInsert 호출 성공");
      if (requestData.size() < 2) {
         throw new RuntimeException("올바른 데이터 형식이 아닙니다.");
      }

      // 🟥 기존 코드: mapToCourse() 메서드를 사용하여 변환
      // Course course = mapToCourse(requestData.get(0));

      // 🟩 Gson을 사용하여 Map 데이터를 Course 객체로 변환
      Course course = gson.fromJson(gson.toJson(requestData.get(0)), Course.class);
      List<Map<String, Object>> details = (List<Map<String, Object>>) requestData.get(1).get("details");

      return courseService.courseInsertWithDetails(course, details);
   }


      // ✅ 코스 삭제 (상세 정보도 같이 삭제)
   @DeleteMapping("delete")
   public String deleteCourse(@RequestParam int cs_no) {
         log.info("deleteCourse 호출 성공: " + cs_no);
         return String.valueOf(courseService.deleteCourse(cs_no));
   }

}


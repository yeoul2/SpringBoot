package com.example.back.controller;

import com.example.back.model.Course;
import com.example.back.service.CourseService;
import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;
import java.util.Map;

@Log4j2
@RestController // Spring Boot REST API 컨트롤러
@RequestMapping("/api/course/") // API 기본 URL 설정
public class CourseController {

   @Autowired
   private CourseService courseService; // 코스 서비스 의존성 주입

   // ✅ 전체 코스 조회 (상세 정보 포함)
   @GetMapping("list")
   public String courseList(@RequestParam Map<String, Object> paramMap) {
      log.info("courseList 호출 성공");
      List<Map<String, Object>> list = courseService.getAllCourses(paramMap); // 전체 코스 목록 조회
      Gson g = new Gson();
      return g.toJson(list);
   }

   // ✅ 특정 코스 조회 (상세 정보 포함)
   @GetMapping("detail")
   public String courseDetail(@RequestParam int cs_no) {
      log.info("courseDetail 호출 성공: " + cs_no);
      Map<String, Object> course = courseService.getCourseById(cs_no); // 특정 코스 조회
      Gson g = new Gson();
      return g.toJson(course);
   }

   // ✅ 코스 + 상세 정보 함께 추가
   @PostMapping("insert")
   public int insertCourse(@RequestBody Map<String, Object> paramMap) {
      log.info("insertCourse 호출 성공");

      // ✅ Map<String, Object> → Course 객체 변환
      Course course = mapToCourse(paramMap);

      return courseService.insertCourseWithDetails(course); // 변환된 Course 객체 전달
   }

 /*   // ✅ 코스 + 상세 정보 함께 수정
   @PutMapping("update")
   public int updateCourse(@RequestBody Map<String, Object> paramMap) {
      log.info("updateCourse 호출 성공");

      // ✅ Map<String, Object> → Course 객체 변환
      Course course = mapToCourse(paramMap);

      return courseService.updateCourseWithDetails(course); // 변환된 Course 객체 전달
   } */

   // ✅ 코스 삭제 (상세 정보도 같이 삭제)
   @DeleteMapping("delete")
   public String deleteCourse(@RequestParam int cs_no) {
      log.info("deleteCourse 호출 성공: " + cs_no);
      return String.valueOf(courseService.deleteCourseWithDetails(cs_no)); // 코스 및 상세 정보 삭제
   }

   // ✅ Map<String, Object> → Course 변환 메서드
   private Course mapToCourse(Map<String, Object> paramMap) {
      Course course = new Course();
      course.setUser_id((String) paramMap.get("user_id"));
      course.setCs_name((String) paramMap.get("cs_name"));
      course.setCs_country((String) paramMap.get("cs_country"));
      course.setCs_city((String) paramMap.get("cs_city"));
      
      if (paramMap.get("cs_departure_date") != null) {
         course.setCs_departure_date(Date.valueOf((String) paramMap.get("cs_departure_date")));
      }
      if (paramMap.get("cs_return_date") != null) {
         course.setCs_return_date(Date.valueOf((String) paramMap.get("cs_return_date")));
      }

      course.setCs_people_num(Integer.parseInt(paramMap.get("cs_people_num").toString()));
      course.setCs_theme((String) paramMap.get("cs_theme"));
      course.setCs_like_count(Integer.parseInt(paramMap.get("cs_like_count").toString()));

      return course;
   }
}

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
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
            .create();

//좋아요 눌렀었는지 확인하기
   @PostMapping("csHasLiked")
   public boolean hasLiked(@RequestBody Map<String, Object> lmap) {
      log.info("csHasLiked호출 성공");
      boolean result = false;
      result = courseService.csHasLiked(lmap);
      return result;
   }
   //좋아요가 눌렀었는지 확인해서 좋아요 취소/하기 (좋아요 버튼 누름)
   @PostMapping("csToggleLike")
   public String csToggleLike(@RequestBody Map<String, Object> lmap) {
      log.info("csToggleLike 성공");
      String result = "";
      result = courseService.csToggleLike(lmap);
      return result;
   }
            


   @GetMapping("list")
   public String getCourseList(@RequestParam Map<String, Object> paramMap) {
      log.info("📌 getCourseList 호출 성공");

      // ✅ 페이지네이션을 위한 설정
      int page = Integer.parseInt(paramMap.getOrDefault("page", "1").toString()); 
      int pageSize = Integer.parseInt(paramMap.getOrDefault("pageSize", "6").toString()); 
      int offset = (page - 1) * pageSize;
      paramMap.put("offset", offset);
      paramMap.put("pageSize", pageSize);

      log.info("✅ 현재 페이지: " + page);
      log.info("✅ 페이지 크기: " + pageSize); // <-- 여기 추가
      log.info("✅ 정렬 기준: " + paramMap.get("order"));


      // ✅ 전체 개수 조회
      int totalCourses = courseService.getTotalCourseCount(paramMap);//총개수
      int totalPages = (int) Math.ceil((double) totalCourses / pageSize);//총 페이지 계산

      // ✅ 코스 목록 가져오기
      List<Map<String, Object>> list = courseService.getCourseList(paramMap);
      log.info("✅ 가져온 데이터 개수: " + list.size());


      // ✅ 응답 데이터 구성
      Map<String, Object> response = new HashMap<>();
      response.put("courses", list);//코스 리스트
      response.put("totalPages", totalPages);//프론트에 넘겨줄 값
      response.put("currentPage", page);

      String temp = gson.toJson(response);

      return temp;
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
   public String deleteCourse(@RequestParam int cs_no, @AuthenticationPrincipal UserDetails userDetails) {
      log.info("✅ 요청한 사용자 ID: {}", userDetails.getUsername());   
      log.info("deleteCourse 호출 성공: " + cs_no);
         return String.valueOf(courseService.deleteCourse(cs_no));
   }

   // 👩‍💻 user_id로 코스찾기
   @GetMapping("getUserCourse")
   /* public String getUsercourse(@RequestParam String user_id) {
      log.info("getUsercourse 호출 성공:" + user_id);
      List<Map<String,Object>> ulist = null;
      ulist = courseService.getUsercourse(user_id);
      String temp = gson.toJson(ulist);
      return temp;
   } */

   public String getUsercourse(@RequestParam("user_id") String user_id) {
      log.info("getUsercourse 호출 성공:" + user_id);
      List<Map<String,Object>> ulist = null;
      ulist = courseService.getUsercourse(user_id);
      String temp = gson.toJson(ulist);
      return temp;
   }

   // 👨‍👩‍👧‍👦코스공유하기(저장된 코스 공유하기 클릭시 작동)
   @PutMapping("shareCourse")
   public int shareCourse(@RequestParam int cs_no, @AuthenticationPrincipal UserDetails userDetails) {
      log.info("shareCourse 호출 성공"+ cs_no);
      int result = -1;
      result = courseService.shareCourse(cs_no);
      return result;
   }

}


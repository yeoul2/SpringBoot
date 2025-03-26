package com.example.back.controller;

import com.example.back.model.TripBoard;
import com.example.back.service.TripBoardService;
import com.example.back.utils.LocalDateTimeAdapter;
import com.example.back.utils.LocalTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api/board/")
public class TripBoardController {
   @Autowired
   private TripBoardService tripBoardService;

   // Gson에 LocalDateTime 처리 추가
   private final Gson gson = new GsonBuilder()
           .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()) // LocalDateTime을 처리하는 TypeAdapter 등록
           .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
            .create();

   //후기 게시글 갯수 조회
   @GetMapping("tripboardCount")
   public int tripBoardCount(@RequestParam Map<String, Object> tmap) {
      log.info("tripBoardCount 호출 성공");
      int result = -1;
      result = tripBoardService.tripboardCount(tmap);
      log.info("총 게시글 갯수: "+result);
      return result;
   }


   // 후기 조회 (전체)
   @GetMapping("tripboardList")
   public String tripboardList(@RequestParam Map<String, Object> tmap) {
      log.info("tripboardList 호출 성공");
      log.info(tmap);
      List<Map<String, Object>> list = null;
      list = tripBoardService.tripboardList(tmap);
      String temp = gson.toJson(list);
      return temp;
   }

   // 후기 상세 조회
   @GetMapping("tripboardDetail")
   public String tripboardDetail(@RequestParam Map<String, Object> tmap) {
      log.info("tripboardDetail 호출 성공");
      List<Map<String, Object>> list = null;
      list = tripBoardService.tripboardDetail(tmap);
      String temp = null;
      temp = gson.toJson(list);
      return temp;
   }

   // 후기 등록
   @PostMapping("tripboardInsert")
   public int tripboardInsert(@RequestBody List<Map<String, Object>> requestData) {
      log.info("tripboardInsert 호출 성공");
      if (requestData.size() < 2) {
         throw new RuntimeException("올바른 데이터 형식이 아닙니다.");
      }
      // 첫 번째 객체: 게시글 정보
      Gson gson = new Gson();
      // 게시글 정보 (TripBoard 객체로 변환)
      TripBoard board = gson.fromJson(gson.toJson(requestData.get(0)), TripBoard.class);

      // 두 번째 객체: 코스 리스트 변환
      List<Map<String, Object>> details = (List<Map<String, Object>>) requestData.get(1).get("course");

      log.info("📌 변환된 게시글 정보: " + board);
      log.info("📌 변환된 코스 리스트: " + details);

      // 게시글과 코스를 함께 저장
      return tripBoardService.tripboardInsert(board, details);
   }

   // 후기 수정
   @PutMapping("tripboardUpdate/{tb_no}")
   public int tripboardUpdate(@PathVariable int tb_no, @RequestBody List<Map<String, Object>> requestData) {
      log.info("boardUpdate호출 성공");
      log.info("tb_no: " + tb_no);
      log.info("requestData: " + requestData);
      if (requestData.size() < 2) {
         throw new RuntimeException("올바른 데이터 형식이 아닙니다.");
      }
      // 첫 번째 객체: 게시글 정보
      Gson gson = new Gson();
      TripBoard board = gson.fromJson(gson.toJson(requestData.get(0)), TripBoard.class);
      // 두 번째 객체: 코스 리스트
      List<Map<String, Object>> details = (List<Map<String, Object>>) requestData.get(1).get("course");

      // 게시글과 코스를 함께 저장
      return tripBoardService.tripboardUpdate(tb_no,board, details);
   }

   // 후기 삭제
   @DeleteMapping("tripboardDelete")
   public String tripboardDelete(@RequestParam(value = "tb_no", required = true) int tb_no) {
      log.info("tripboardDelete 호출 성공");
      int result = 1;
      result = tripBoardService.tripboardDelete(tb_no);
      return "" + result;
   }

   /* 댓글 구현 */

   // 댓글 등록
   @PostMapping("commentInsert")
   public int commentInsert(@RequestBody Map<String, Object> tmap) {
      log.info("commentInsert 호출 성공");
      int result = 1;
      result = tripBoardService.commentInsert(tmap);
      return result;
   }

   // 댓글 수정
   @PutMapping("commentUpdate")
   public int commentUpdate(@RequestBody Map<String, Object> tmap) {
      log.info("commentUpdate 호출 성공");
      int result = 1;
      result = tripBoardService.commentUpdate(tmap);
      return result;
   }

   // 댓글 삭제
   @DeleteMapping("commentDelete")
   public int commentDelete(@RequestParam(value = "tbc_no", required = true) int tbc_no) {
      log.info("commentDelete 호출 성공");
      int result = 1;
      result = tripBoardService.commentDelete(tbc_no);
      return result;
   }

   // 유저가 게시판에 좋아요를 눌렀는지
   // user_id, tb_no
   // 좋아요는 T/ 안눌렀으면 F
   @PostMapping("hasLiked")
   public boolean hasLiked(@RequestBody Map<String, Object> lmap) {
      log.info("hasLiked호출 성공");
      boolean result = false;
      result = tripBoardService.hasLiked(lmap);
      return result;
   }

   // 좋아요 토글
   // user_id, tb_no
   @PostMapping("toggleLike")
   public String toggleLike(@RequestBody Map<String, Object> lmap) {
      log.info("toggleLike호출 성공");
      String result = "";
      result = tripBoardService.toggleLike(lmap);
      return result;
   }

}

package com.example.back.controller;

import com.example.back.model.TripBoard;
import com.example.back.service.TripBoardService;
import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api")
public class TripBoardController {
   @Autowired
   private TripBoardService tripBoardService;

   // 후기 조회 (전체) 이후에 페이징 처리 필요함
   @GetMapping("board/tripboardList")
   public String tripboardList(@RequestParam Map<String, Object> tmap) {
      log.info("tripboardList 호출 성공");
      List<Map<String,Object>> list = null;
      list = tripBoardService.tripboardList(tmap);
      Gson g = new Gson();
      String temp = g.toJson(list);
      return temp;
   }

   // 후기 상세 조회
   @GetMapping("board/tripboardDetail")
   public String tripboardDetail(@RequestParam Map<String, Object> tmap) {
      log.info("tripboardDetail 호출 성공");
      List<Map<String, Object>> list = null;
      list = tripBoardService.tripboardDetail(tmap);
      Gson g = new Gson();
      String temp = null;
      temp = g.toJson(list);
      return temp;
   }

   // 후기 등록
   @PostMapping("board/tripboardInsert")
   public int tripboardInsert(@RequestBody TripBoard board) {
      log.info("tripboardInsert 호출 성공");
      log.info(board);
      int result = -1;
      result = tripBoardService.tripboardInsert(board);
      return result;
   }

   // 후기 수정
   @PutMapping("board/tripboardUpdate")
   public int tripboardUpdate(@RequestBody Map<String, Object> tmap) {
      log.info("boardUpdate호출 성공");
      int result = 1;
      result = tripBoardService.tripboardUpdate(tmap);
      return result;
   }

   // 후기 삭제
   @DeleteMapping("board/tripboardDelete")
   public String tripboardDelete(@RequestParam(value="tb_no", required = true) int tb_no) {
      log.info("tripboardDelete 호출 성공");
      int result = 1;
      result = tripBoardService.tripboardDelete(tb_no);
      return "" + result;
   }

}

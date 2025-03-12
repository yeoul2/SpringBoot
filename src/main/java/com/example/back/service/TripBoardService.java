package com.example.back.service;

import com.example.back.dao.TripBoardDao;
import com.example.back.model.TripBoard;
import com.google.gson.Gson;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class TripBoardService {
   @Autowired
   private TripBoardDao tripBoardDao;

   /* 게시판 구현 */

   // 게시판 갯수 조회
   public int tripboardCount(Map<String, Object> tmap) {
      log.info("tripboardCount 호출 성공");
      int result = -1;
      result = tripBoardDao.tripboardCount(tmap);
      return result;
   }
   // 게시판 조회
   public List<Map<String, Object>> tripboardList(Map<String, Object> tmap) {
      log.info("tripboardList 호출 성공");

      // 페이지 정보 계산
      int page = Integer.parseInt(tmap.getOrDefault("page", "1").toString());
      int offset = (page-1) * 8;
      tmap.put("offset", offset);  // 쿼리에 사용할 offset 추가
      List<Map<String, Object>> list = null;
      list = tripBoardDao.tripboardList(tmap);
      return list;
   }

   // 게시판 상세 가져오기
   public List<Map<String, Object>> tripboardDetail(Map<String, Object> tmap) {
      log.info("tripboardDetail 호출 성공");
      List<Map<String, Object>> list = null;
      list = tripBoardDao.tripboardDetial(tmap);

      // 보드디테일가져오기(코스)
      List<Map<String, Object>> tripboardDetailList = tripBoardDao.tripboardDetailList(tmap);
      if (tripboardDetailList != null && tripboardDetailList.size() > 0) {
         Map<String, Object> tbdmap = new HashMap<>();
         tbdmap.put("course", tripboardDetailList);
         list.add(1, tbdmap);
      }

      // 댓글 가져오기
      List<Map<String, Object>> commList = tripBoardDao.commentList(tmap);
      if (commList != null && commList.size() > 0) {
         Map<String, Object> cmap = new HashMap<>();
         cmap.put("comments", commList);
         list.add(2, cmap);
      }
      return list;
   }

   // 게시판 작성 (보드 디테일 함께 처리)
   @Transactional
   public int tripboardInsert(TripBoard board, List<Map<String, Object>> details) {
      log.info("tripboardInsert 호출 성공");

      // 게시글 추가
      int tb_no = tripBoardDao.tripboardInsert(board);
      if (tb_no ==  0) {
         throw new RuntimeException("게시글 등록 실패");
      }

      // 코스 리스트 추가
      for (Map<String, Object> detail : details) {
         detail.put("tb_no", tb_no); // tb_no 값을 각 코스에 추가
         int result = tripBoardDao.tripboardDetailInsert(detail);
         if (result != 1) {
            throw new RuntimeException("코스 등록 실패");
         }
      }

      return tb_no;
   }

   // 게시판 수정
   @Transactional
   public int tripboardUpdate(TripBoard board, List<Map<String, Object>> details) {
      log.info("tripboardUpdate 호출 성공");
      int result1 = -1;
      // 1. 게시글 정보 추출 및 수정
      result1 = tripBoardDao
            .tripboardUpdate(new Gson().fromJson(new Gson().toJson(board), Map.class));
      if (result1 !=1) {
         throw new RuntimeException("게시글 업데이트 실패");
      }

      int tb_no = board.getTb_no();

      // 2. 기존 코스 삭제
      int result2 = -1;
      result2 = tripBoardDao.tripboardDetailDelete(tb_no);
      if (result2 == 0) {
         throw new RuntimeException("기존 코스 삭제 실패");
      }
      // 3. 새로운 코스 추가
      int result3 = -1;
      for (Map<String, Object> detail : details) {
         detail.put("tb_no", tb_no); // tb_no 값을 각 코스에 추가
         result3 = tripBoardDao.tripboardDetailInsert(detail);
         if (result3 != 1) {
            throw new RuntimeException("코스 등록 실패");
         }
      }

      return 1; // 성공 반환
   }

   // 게시판 삭제
   public int tripboardDelete(int tb_no) {
      log.info("tripboardDelete 호출 성공");
      int result = -1;
      result = tripBoardDao.tripboardDelete(tb_no);
      return result;
   }

   /* 댓글 구현 */

   public int commentInsert(Map<String, Object> tmap) {
      int result = -1;
      result = tripBoardDao.commentInsert(tmap);
      return result;
   }

   public int commentUpdate(Map<String, Object> tmap) {
      int result = -1;
      result = tripBoardDao.commentUpdate(tmap);
      return result;
   }

   public int commentDelete(int tbc_no) {
      int result = -1;
      result = tripBoardDao.commentDelete(tbc_no);
      return result;
   }

   // 유저가 게시판에 좋아요 눌렀는지
   public boolean hasLiked(Map<String, Object> lmap) {
      boolean result = false;
      result = tripBoardDao.hasLiked(lmap);
      return result;
   }

   @Transactional
   public String toggleLike(Map<String, Object> lmap) {
      boolean hasLiked = tripBoardDao.hasLiked(lmap); // 좋아요 눌렀는지 확인
      int result1 = -1;
      int result2 = -1;

      if (hasLiked) { // 좋아요가 이미 눌러져 있으면 취소
         result1 = tripBoardDao.disLike(lmap); // 유저 좋아요 삭제
         result2 = tripBoardDao.disLikeboard((int) (lmap.get("tb_no"))); // 게시글 좋아요 수 감소
      } else { // 좋아요가 안 눌려 있으면 추가
         result1 = tripBoardDao.addLike(lmap); // 유저 좋아요 추가
         result2 = tripBoardDao.likeAddboard((int) (lmap.get("tb_no"))); // 게시글 좋아요 수 증가
      }

      // 하나라도 실패하면 예외 발생 → 롤백
      if (result1 != 1 || result2 != 1) {
         throw new RuntimeException("좋아요 처리 중 오류 발생");
      }
      return "성공";
   }

}

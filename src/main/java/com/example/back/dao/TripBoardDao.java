package com.example.back.dao;

import com.example.back.model.TripBoard;
import lombok.extern.log4j.Log4j2;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Log4j2
@Repository
public class TripBoardDao {
   @Autowired
   private SqlSessionTemplate sqlSessionTemplate;

   public List<Map<String, Object>> tripboardList(Map<String, Object> tmap) {
      log.info("tripboardList 호출 성공");
      List<Map<String, Object>> list = null;
      list = sqlSessionTemplate.selectList("tripboardList", tmap);
      log.info("게시글 개수: "+list.size());
      return list;
   }

   public List<Map<String, Object>> tripboardDetial(Map<String, Object> tmap) {
      log.info("tripboardDetial 호출 성공");
      List<Map<String, Object>> list = null;
      list = sqlSessionTemplate.selectList("tripboardDetial", tmap);
      return list;
   }

   public int tripboardInsert(TripBoard board) {
      log.info("tripboardInsert 호출 성공");
      int result = sqlSessionTemplate.insert("tripboardInsert", board);
      return result > 0 ? board.getTb_no() : -1; // 생성된 tb_no 반환
   }

   public int tripboardUpdate(Map<String, Object> tmap) {
      log.info("tripboardUpdate 호출 성공");
      int result = -1;
      log.info(tmap);
      result = sqlSessionTemplate.update("tripboardUpdate", tmap);
      log.info(result);
      return result;
   }

   public int tripboardDelete(int tb_no) {
      log.info("tripboardDelete 호출 성공");
      int result = -1;
      result = sqlSessionTemplate.delete("tripboardDelete", tb_no);
      return result;
   }


   /* 댓글 구현 */

   public List<Map<String, Object>> commentList(Map<String, Object> tmap) {
      log.info("commentList 호출 성공");
      List<Map<String, Object>> commentList = null;
      commentList = sqlSessionTemplate.selectList("commentList", tmap);
      return commentList;
   }

   public int commentInsert(Map<String, Object> tmap) {
      log.info("commentInsert 호출 성공");
      int result = -1;
      result = sqlSessionTemplate.insert("commentInsert", tmap);
      return result;
   }

   public int commentUpdate(Map<String, Object> tmap) {
      log.info("commentUpdate 호출 성공");
      int result = -1;
      result = sqlSessionTemplate.update("commentUpdate", tmap);
      return result;
   }

   public int commentDelete(int tbc_no) {
      log.info("commentDelete 호출 성공");
      int result = -1;
      result = sqlSessionTemplate.delete("commentDelete", tbc_no);
      return result;
   }

   /* 유저가 게시판에 좋아요를 눌렀는지 */
   public boolean hasLiked(Map<String, Object> lmap) {
      log.info("hasLiked 호출 성공");
      boolean result = false;
      result = sqlSessionTemplate.selectOne("hasLiked", lmap);
      return result;
   }

   /* 게시판 좋아요 증가 */
   public int likeAddboard(int tb_no) {
      log.info("likeAddboard 호출 성공");
      int result = -1;
      result = sqlSessionTemplate.update("likeAddboard", tb_no);
      return result;
   }
   /* 유저 좋아요 정보 추가 */
   public int addLike(Map<String, Object> lmap) {
      log.info("addLike 호출 성공");
      int result = -1;
      result = sqlSessionTemplate.insert("addLike", lmap);
      return result;
   }
   /*  게시판 좋아요 감소 */
   public int disLikeboard(int tb_no) {
      log.info("disLikeboard 호출 성공");
      int result = -1;
      result = sqlSessionTemplate.update("disLikeboard", tb_no);
      return result;
   }
   /* 유저 좋아요 제거 */
   public int disLike(Map<String, Object> lmap) {
      log.info("disLike 호출 성공");
      int result = -1;
      result = sqlSessionTemplate.delete("disLike", lmap);
      return result;
   }

   /* 코스 조회(보드디테일) */
   public List<Map<String, Object>> tripboardDetailList(Map<String, Object> tmap) {
      log.info("tripboardDetailList 호출 성공");
      List<Map<String, Object>> courseList = null;
      courseList = sqlSessionTemplate.selectList("tripboardDetailList", tmap);
      return courseList;
   }
   /* 코스 추가(보드디테일) */
   public int tripboardDetailInsert(Map<String, Object> detail) {
      log.info("tripboardDetailInsert 호출 성공");
      int result = -1;
      result = sqlSessionTemplate.insert("tripboardDetailInsert", detail);
      return result;
   }
   /* 코스 삭제 (보드 디테일) */
   public int tripboardDetailDelete(int tb_no) {
      log.info("tripboardDetailDelete 호출 성공");
      return sqlSessionTemplate.delete("tripboardDetailDelete", tb_no);
   }
}

package com.example.back.service;

import com.example.back.dao.TripBoardDao;
import com.example.back.model.TripBoard;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class TripBoardService {
   @Autowired
   private TripBoardDao tripBoardDao;

   public List<Map<String, Object>> tripboardList(Map<String, Object> tmap) {
      log.info("tripboardList 호출 성공");
      List<Map<String, Object>> list = null;
      list = tripBoardDao.tripboardList(tmap);
      return list;
   }

   public List<Map<String, Object>> tripboardDetail(Map<String, Object> tmap) {
      log.info("tripboardDetail 호출 성공");
      List<Map<String, Object>> list = null;
      list = tripBoardDao.tripboardDetial(tmap);
      // 댓글 가져오기
      List<Map<String, Object>> commList = tripBoardDao.commentList(tmap);
      if(commList != null && commList.size() > 0) {
         Map<String,Object> cmap = new HashMap<>();
         cmap.put("comments", commList);
         list.add(1, cmap);
      }
      return list;
   }

   public int tripboardInsert(TripBoard board) {
      log.info("tripboardInsert 호출 성공");
      int result = -1;
      result = tripBoardDao.tripboardInsert(board);
      return result;
   }

   public int tripboardUpdate(Map<String, Object> tmap) {
      log.info("tripboardUpdate 호출 성공");
      int result = -1;
      result = tripBoardDao.tripboardUpdate(tmap);
      return result;
   }

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
}

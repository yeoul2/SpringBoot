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
      list = sqlSessionTemplate.selectList("com.example.back.dao.TripBoardDao.tripboardList", tmap);
      log.info("게시글 개수: "+list.size());
      return list;
   }

   public List<Map<String, Object>> tripboardDetial(Map<String, Object> tmap) {
      log.info("tripboardDetial 호출 성공");
      List<Map<String, Object>> list = null;
      list = sqlSessionTemplate.selectList("com.example.back.dao.TripBoardDao.tripboardDetial", tmap);
      return list;
   }

   public int tripboardInsert(TripBoard board) {
      log.info("tripboardInsert 호출 성공");
      int result = -1;
      result = sqlSessionTemplate.insert("com.example.back.dao.TripBoardDao.tripboardInsert", board);
      return result;
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
}

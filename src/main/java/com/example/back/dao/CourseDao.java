package com.example.back.dao;

import com.example.back.model.Course;
import com.example.back.model.CourseDetail;
import lombok.extern.log4j.Log4j2;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Log4j2
@Repository
@Mapper
public class CourseDao {
      @Autowired
      private SqlSessionTemplate sqlSessionTemplate;

      // 좋아요 추가 (cs_like_count 증가)
      public int addLike(int cs_no) {
            return sqlSessionTemplate.update("com.example.back.dao.CourseDao.addLike", cs_no);
      }
      
      // 좋아요 취소 (cs_like_count 감소, 0 이하 방지)
      public int removeLike(int cs_no) {
            return sqlSessionTemplate.update("com.example.back.dao.CourseDao.removeLike", cs_no);
      }
      
      

      // ✅ 코스 목록 조회
      public List<Map<String, Object>> getCourseList(Map<String, Object> paramMap) {
            log.info("getCourseList 호출 성공");
            List<Map<String, Object>> list = sqlSessionTemplate
                        .selectList("com.example.back.dao.CourseDao.getCourseList", paramMap);
            log.info("코스 개수: " + list.size());
            return list;
      }

      public int getCourseCount(Map<String, Object> paramMap) {
            log.info("📌 getCourseCount 호출 성공");
            int result = sqlSessionTemplate.selectOne("com.example.back.dao.CourseDao.getCourseCount", paramMap);
            log.info("코스 개수: " + result);
            return result;
      }

      // ✅ 특정 코스 상세 조회
      public List<Map<String, Object>> courseDetail(Map<String, Object> cmap) {
            log.info("courseDetail 호출 성공");
            List<Map<String, Object>> clist = null;
            clist = sqlSessionTemplate.selectList("com.example.back.dao.CourseDao.courseDetail", cmap);
            return clist;
      }

      // ✅ 코스 추가
      public int courseInsert(Course course) {
            log.info("courseInsert 호출 성공");
            int result = sqlSessionTemplate.insert("com.example.back.dao.CourseDao.courseInsert", course);
            log.info("추가된 코스 번호: " + course.getCs_no());
            return result > 0 ? course.getCs_no() : -1;
      }

      // ✅ 코스 삭제
      public int deleteCourse(int cs_no) {
            log.info("deleteCourse 호출 성공");
            int result = sqlSessionTemplate.delete("com.example.back.dao.CourseDao.deleteCourse", cs_no);
            log.info("삭제된 코스 번호: " + cs_no);
            return result;
      }

      /* ✅ 코스 상세 정보 (cs_detail 관련) */

      // ✅ 특정 코스의 상세 정보 조회
      public List<Map<String, Object>> courseDetails(Map<String, Object> cmap) {
            log.info("courseDetails 호출 성공");
            List<Map<String, Object>> clist = sqlSessionTemplate.selectList("com.example.back.dao.CourseDao.courseDetails", cmap);
            return clist;
      }

      // ✅ 코스 상세 정보 추가
      public int courseInsertDetail(Map<String, Object> courseDetail) {
            log.info("courseInsertDetail 호출 성공");
            int result = sqlSessionTemplate.insert("com.example.back.dao.CourseDao.courseInsertDetail", courseDetail);
            return result;
      }

      // ✅ user_id로 코스찾기
      public List<Map<String, Object>> getUsercourse(String user_id) {
            log.info("getUsercourse 호출 성공:" + user_id);
            List<Map<String, Object>> ulist = null;
            ulist = sqlSessionTemplate.selectList("com.example.back.dao.CourseDao.getUsercourse", user_id);
            return ulist;
      }

      public int shareCourse(int cs_no) {
            log.info("shareCourse호출 성공"+cs_no);
            int result = -1;
            result = sqlSessionTemplate.update("shareCourse",cs_no);
            return result;
      }
}

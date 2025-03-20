package com.example.back.dao;

import com.example.back.model.Course;
import com.example.back.model.CourseDetail;
import lombok.extern.log4j.Log4j2;

import org.apache.ibatis.annotations.Mapper;
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
            return sqlSessionTemplate.selectOne("com.example.back.dao.CourseDao.getCourseCount", paramMap);
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

}

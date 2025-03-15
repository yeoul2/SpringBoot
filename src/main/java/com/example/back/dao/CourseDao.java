package com.example.back.dao;

import com.example.back.model.Course;
import com.example.back.model.CourseDetail;
import lombok.extern.log4j.Log4j2;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Log4j2
@Repository
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

      // ✅ 특정 코스 상세 조회
      public Map<String, Object> getCourseDetail(int cs_no) {
            log.info("getCourseDetail 호출 성공");
            return sqlSessionTemplate.selectOne("com.example.back.dao.CourseDao.getCourseDetail", cs_no);
      }

      // ✅ 코스 추가
      public int insertCourse(Course course) {
            log.info("insertCourse 호출 성공");
            int result = sqlSessionTemplate.insert("com.example.back.dao.CourseDao.insertCourse", course);
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
      public List<Map<String, Object>> getCourseDetails(int cs_no) {
            log.info("getCourseDetails 호출 성공");
            return sqlSessionTemplate.selectList("com.example.back.dao.CourseDao.getCourseDetails", cs_no);
      }

      // ✅ 코스 상세 정보 추가
      public int insertCourseDetail(Map<String, Object> courseDetail) {
            log.info("insertCourseDetail 호출 성공");
            int result = sqlSessionTemplate.insert("com.example.back.dao.CourseDao.insertCourseDetail", courseDetail);
            return result;
      }


      // ✅ 코스 상세 정보 삭제
      public int deleteCourseDetails(int cs_no) {
            log.info("deleteCourseDetails 호출 성공");
            int result = sqlSessionTemplate.delete("com.example.back.dao.CourseDao.deleteCourseDetails", cs_no);
            log.info("삭제된 상세 정보 개수: " + result);
            return result;
      }
}

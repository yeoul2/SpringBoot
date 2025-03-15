package com.example.back.service;

import com.example.back.dao.CourseDao;
import com.example.back.model.Course;
import com.example.back.model.CourseDetail;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Log4j2
@Service // Spring Boot의 서비스 계층으로 등록
public class CourseService {

    @Autowired
    private CourseDao courseDao;

    // ✅ 전체 코스 조회 (상세 정보 포함)
    public List<Map<String, Object>> getAllCourses(Map<String, Object> paramMap) {
        log.info("getAllCourses 호출 성공");
        return courseDao.getCourseList(paramMap);
    }

    // ✅ 특정 코스 조회 (상세 정보 포함)
    public Map<String, Object> getCourseById(int cs_no) {
        log.info("getCourseById 호출 성공: " + cs_no);
        return courseDao.getCourseDetail(cs_no);
    }

    // ✅ 코스 + 상세 정보 함께 저장
    public int insertCourseWithDetails(Course course, List<Map<String, Object>> details) {
        log.info("insertCourseWithDetails 호출 성공");
        int cs_no = courseDao.insertCourse(course); // 코스 저장
        if (cs_no > 0) {
            for (Map<String, Object> detail : details) {
                detail.put("cs_no", cs_no);
                int result = courseDao.insertCourseDetail(detail);// 상세 정보 저장
                if (result != 1) {
                   throw new RuntimeException("코스 등록 실패");
                }
            }
        }
        log.info("insertCourseWithDetails 완료: 코스 번호 " + course.getCs_no());
        return cs_no;
    }

 /*    // ✅ 코스 + 상세 정보 함께 수정
    public int updateCourseWithDetails(Map<String, Object> paramMap) {
        log.info("updateCourseWithDetails 호출 성공");
        int result = courseDao.updateCourse(paramMap); // 코스 정보 수정
        if (result > 0) {
            log.info("코스 수정 성공, 상세 정보 업데이트 진행");
            List<Map<String, Object>> details = (List<Map<String, Object>>) paramMap.get("details");
            for (Map<String, Object> detail : details) {
                courseDao.updateCourseDetail(detail); // 상세 정보 수정
            }
        }
        log.info("updateCourseWithDetails 완료");
        return result;
    } */

    // ✅ 코스 삭제 (상세 정보도 같이 삭제)
    public int deleteCourseWithDetails(int cs_no) {
        log.info("deleteCourseWithDetails 호출 성공: " + cs_no);
        courseDao.deleteCourseDetails(cs_no); // 상세 정보 삭제
        int result = courseDao.deleteCourse(cs_no); // 코스 삭제
        log.info("deleteCourseWithDetails 완료: " + cs_no);
        return result;
    }
}

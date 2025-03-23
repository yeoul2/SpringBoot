package com.example.back.service;

import com.example.back.dao.CourseDao;
import com.example.back.model.Course;
import com.example.back.model.CourseDetail;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service // Spring Boot의 서비스 계층으로 등록
public class CourseService {

    @Autowired
    private CourseDao courseDao;

    @Transactional
    public boolean toggleLike(int cs_no, String action) {
        if ("like".equalsIgnoreCase(action)) {
            log.info("좋아요 추가 실행 - cs_no: {}", cs_no);
            return courseDao.addLike(cs_no) == 1;  // 좋아요 추가 (cs_like_count 증가)
        } else if ("unlike".equalsIgnoreCase(action)) {
            log.info("좋아요 취소 실행 - cs_no: {}", cs_no);
            return courseDao.removeLike(cs_no) == 1;  // 좋아요 취소 (cs_like_count 감소)
        }
        return false;
    }
    

    
    // ✅ 전체 코스 조회 (상세 정보 포함)
    public List<Map<String, Object>> getCourseList(Map<String, Object> paramMap) {
        log.info("getCourseList 호출 성공");
        List<Map<String, Object>> list = null;
        list = courseDao.getCourseList(paramMap);
        return list;
    }

    public int getTotalCourseCount(Map<String, Object> paramMap) {
        return courseDao.getCourseCount(paramMap);
    }

    // ✅ 특정 코스 조회 (상세 정보 포함)
    public List<Map<String, Object>> courseDetail(Map<String, Object> cmap) {
        log.info("courseDetail 호출 성공: " + cmap);
        // 코스 가져오기
        List<Map<String, Object>> clist = courseDao.courseDetail(cmap);

        // 코스 디테일 가져오기
        List<Map<String, Object>> dlist = courseDao.courseDetails(cmap);
        if(dlist != null && dlist.size() > 0) {
            Map<String, Object> dmap = new HashMap<>();
            dmap.put("details", dlist);
            clist.add(1, dmap);
        }
        return clist;
    }

    // ✅ 코스 + 상세 정보 함께 저장
    public int courseInsertWithDetails(Course course, List<Map<String, Object>> details) {
        log.info("courseInsertWithDetails 호출 성공");
        int cs_no = courseDao.courseInsert(course); // 코스 저장
        if (cs_no > 0) {
            for (Map<String, Object> detail : details) {
                detail.put("cs_no", cs_no);
                int result = courseDao.courseInsertDetail(detail);// 상세 정보 저장
                if (result != 1) {
                   throw new RuntimeException("코스 등록 실패");
                }
            }
        }
        log.info("courseInsertWithDetails 완료: 코스 번호 " + course.getCs_no());
        return cs_no;
    }

    // ✅ 코스 삭제 (상세 정보도 같이 삭제)
    public int deleteCourse(int cs_no) {
        log.info("deleteCourse 호출 성공: " + cs_no);
        int result = courseDao.deleteCourse(cs_no); // 코스 삭제
        log.info("deleteCourseWithDetails 완료: " + cs_no);
        return result;
    }

    // ✅ user_id로 코스찾기
    public List<Map<String, Object>> getUsercourse(String user_id) {
        log.info("getUsercourse 호출 성공:" + user_id);
        List<Map<String, Object>> ulist = null;
        ulist = courseDao.getUsercourse(user_id);
        return ulist;
   }

    public int shareCourse(int cs_no) {
        log.info("shareCourse호출 성공" + cs_no);
        int result = -1;
        result = courseDao.shareCourse(cs_no);
        return result;
    }
}

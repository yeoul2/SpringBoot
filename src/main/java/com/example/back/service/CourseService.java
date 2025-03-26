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

    
    public boolean csHasLiked(Map<String,Object> lmap) {//좋아요가 눌렸는지
        boolean result = false;
        result = courseDao.csHasLiked(lmap);
        return result;
    }

    @Transactional
    public String csToggleLike(Map<String,Object> lmap) {
        boolean csHasLiked = courseDao.csHasLiked(lmap);//좋아요 눌렀었는지 확인하기^^
        // T이면 이미 좋아요 눌러져있음, F이면 좋아요 눌러져있지 않음
        int result1 = -1;
        int result2 = -1;

        if(csHasLiked) { // 이미 좋아요 눌려있는거임 -> 좋아요 취소시키기
            result1 = courseDao.removeDeleteLikesTable(lmap);//좋아요 취소
            result2 = courseDao.removeLikeCourse(Integer.parseInt((lmap.get("cs_no").toString())));//좋아요 수 감소
        } else { // 이미 좋아요 안 눌려져 있는거임 -> 좋아요 하기
            result1 = courseDao.addLikesTable(lmap);//좋아요하기
            result2 = courseDao.addLikeCourse(Integer.parseInt(lmap.get("cs_no").toString()));//좋아요 수 증가
        }
        if(result1 !=1 || result2 !=1){
            throw new RuntimeException("좋아요 처리 중 오류 발생");
        }
        return "성공";
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

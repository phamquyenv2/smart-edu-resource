/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.controllers.client;

import com.paq.pojo.response.ResCourseDTO;
import com.paq.pojo.response.ResCourseLessonDTO;
import com.paq.pojo.response.ResEnrollmentDTO;
import com.paq.pojo.response.ResResponse;
import com.paq.service.CourseLessonService;
import com.paq.service.StudentCourseService;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author Admin
 */
@RestController
@RequestMapping("/api")
public class ApiStudentCourseController {

    @Autowired
    private StudentCourseService studentCourseService;

    @Autowired
    private CourseLessonService lessonService;

    @GetMapping("/student/courses")
    public ResponseEntity<ResResponse<List<ResCourseDTO>>> getCourses(
            @RequestParam Map<String, String> params) {
        ResResponse<List<ResCourseDTO>> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Get courses successfully");
        res.setData(this.studentCourseService.getCourses(params));

        return ResponseEntity.ok(res);
    }

    @GetMapping("/student/courses/{id}")
    public ResponseEntity<ResResponse<ResCourseDTO>> getCourseDetail(
            @PathVariable(value = "id") int id) {

        ResResponse<ResCourseDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Get course detail successfully");
        res.setData(this.studentCourseService.getCourseById(id));

        return ResponseEntity.ok(res);
    }

    @GetMapping("/student/courses/count")
    public ResponseEntity<ResResponse<Long>> countCourses(
            @RequestParam Map<String, String> params) {

        ResResponse<Long> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Count courses successfully");
        res.setData(this.studentCourseService.countCourses(params));

        return ResponseEntity.ok(res);
    }

    @GetMapping("/student/courses/{courseId}/lessons")
    public ResponseEntity<ResResponse<List<ResCourseLessonDTO>>> getCourseLessons(
            @PathVariable("courseId") int courseId) {
        ResResponse<List<ResCourseLessonDTO>> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Get course lessons successfully");
        res.setData(this.lessonService.getLessonsByCourseId(courseId));

        return ResponseEntity.ok(res);
    }

    @PostMapping("/secure/student/courses/{id}/enroll")
    public ResponseEntity<ResResponse<ResEnrollmentDTO>> enrollCourse(
            @PathVariable(value = "id") int id,
            Principal principal) {

        ResResponse<ResEnrollmentDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Enroll course successfully");
        res.setData(this.studentCourseService.enrollCourse(principal.getName(), id));

        return ResponseEntity.ok(res);
    }

    @GetMapping("/secure/student/my-courses")
    public ResponseEntity<ResResponse<List<ResEnrollmentDTO>>> getMyCourses(
            Principal principal) {

        ResResponse<List<ResEnrollmentDTO>> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Get my courses successfully");
        res.setData(this.studentCourseService.getMyCourses(principal.getName()));

        return ResponseEntity.ok(res);
    }
}

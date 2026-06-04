/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.service.impl;

import com.paq.pojo.Course;
import com.paq.pojo.CourseLesson;
import com.paq.pojo.Enrollment;
import com.paq.pojo.Student;
import com.paq.pojo.User;
import com.paq.pojo.response.ResCourseDTO;
import com.paq.pojo.response.ResEnrollmentDTO;
import com.paq.repository.CourseLessonRepository;
import com.paq.repository.CourseRepository;
import com.paq.repository.EnrollmentRepository;
import com.paq.service.StudentCourseService;
import com.paq.service.UserService;
import com.paq.utils.DTOMapper;
import com.paq.utils.constant.EnrollmentStatusEnum;
import com.paq.utils.error.IdInvalidException;
import com.paq.utils.error.PermissionException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Admin
 */
@Service
@Transactional
public class StudentCourseServiceImpl implements StudentCourseService {

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private CourseLessonRepository courseLessonRepo;

    @Autowired
    private EnrollmentRepository enrollmentRepo;

    @Autowired
    private UserService userService;

    @Override
    public List<ResCourseDTO> getCourses(Map<String, String> params) {
        return this.courseRepo.getCourses(params)
                .stream()
                .map(c -> DTOMapper.toResCourseDTO(c))
                .collect(Collectors.toList());
    }

    @Override
    public ResCourseDTO getCourseById(int id) {
        Course c = this.courseRepo.getCourseById(id);
        if (c == null) {
            throw new IdInvalidException("Course không tồn tại");
        }

        ResCourseDTO dto = DTOMapper.toResCourseDTO(c);
        List<CourseLesson> lessons = this.courseLessonRepo.getLessonsByCourseId(id);
        dto.setChapters(DTOMapper.toResCourseChapterList(lessons, false));
        dto.setTotalLessons(lessons.size());
        dto.setTotalChapters(dto.getChapters() != null ? dto.getChapters().size() : 0);

        return dto;
    }

    @Override
    public ResEnrollmentDTO enrollCourse(String username, int courseId) {
        User user = this.userService.getUserByUsername(username);

        if (user == null || user.getStudent() == null) {
            throw new PermissionException("Tài khoản hiện tại không phải sinh viên!");
        }

        Student student = user.getStudent();
        Course course = this.courseRepo.getCourseById(courseId);

        if (course == null) {
            throw new IdInvalidException("Không tìm thấy khóa học!");
        }

        if (this.enrollmentRepo.existsByStudentAndCourse(student.getId(), courseId)) {
            throw new IllegalArgumentException("Bạn đã đăng ký khóa học này rồi!");
        }

        Enrollment e = new Enrollment();
        e.setStudentId(student);
        e.setCourseId(course);
        e.setEnrollDate(new Date());
        e.setOverallProgress(0.0);
        e.setTotalStudyTime(0);
        e.setStatus(EnrollmentStatusEnum.SUCCESS);

        Enrollment saved = this.enrollmentRepo.addEnrollment(e);

        return DTOMapper.toResEnrollmentDTO(saved);
    }

    @Override
    public List<ResEnrollmentDTO> getMyCourses(String username) {
        User user = this.userService.getUserByUsername(username);

        if (user == null || user.getStudent() == null) {
            throw new PermissionException("Tài khoản hiện tại không phải sinh viên!");
        }

        return this.enrollmentRepo.getEnrollmentsByStudentId(user.getStudent().getId())
                .stream()
                .map(e -> DTOMapper.toResEnrollmentDTO(e))
                .collect(Collectors.toList());
    }

    @Override
    public Long countCourses(Map<String, String> params) {
        return this.courseRepo.countCourses(params);
    }

}

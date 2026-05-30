/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.service.impl;

import com.paq.pojo.Course;
import com.paq.pojo.Enrollment;
import com.paq.pojo.Student;
import com.paq.pojo.User;
import com.paq.pojo.response.ResCourseDTO;
import com.paq.pojo.response.ResEnrollmentDTO;
import com.paq.repository.CourseRepository;
import com.paq.repository.EnrollmentRepository;
import com.paq.service.StudentCourseService;
import com.paq.service.UserService;
import com.paq.utils.DTOMapper;
import com.paq.utils.error.IdInvalidException;
import com.paq.utils.error.PermissionException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Admin
 */
@Service
public class StudentCourseServicerImpl implements StudentCourseService {

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private EnrollmentRepository enrollmentRepo;

    @Autowired
    private UserService userService;

    @Override
    public List<ResCourseDTO> getCourses() {
        return this.courseRepo.getCourses()
                .stream()
                .map(c -> DTOMapper.toCourseDTO(c))
                .collect(Collectors.toList());
    }

    @Override
    public ResCourseDTO getCourseById(int id) {
        Course c = this.courseRepo.getCourseById(id);
        if (c == null) {
            throw new IdInvalidException("Course không tồn tại");
        }

        return DTOMapper.toCourseDTO(c);
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
        e.setStatus("ACTIVE");

        Enrollment saved = this.enrollmentRepo.addEnrollment(e);

        return DTOMapper.toEnrollmentDTO(saved);
    }

    @Override
    public List<ResEnrollmentDTO> getMyCourses(String username) {
        User user = this.userService.getUserByUsername(username);

        if (user == null || user.getStudent() == null) {
            throw new PermissionException("Tài khoản hiện tại không phải sinh viên!");
        }

        return this.enrollmentRepo.getEnrollmentsByStudentId(user.getStudent().getId())
                .stream()
                .map(e -> DTOMapper.toEnrollmentDTO(e))
                .collect(Collectors.toList());
    }

}

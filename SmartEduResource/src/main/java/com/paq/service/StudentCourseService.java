/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.service;

import com.paq.pojo.response.ResCourseDTO;
import com.paq.pojo.response.ResEnrollmentDTO;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Admin
 */
public interface StudentCourseService {

    List<ResCourseDTO> getCourses(Map<String, String> params);

    ResCourseDTO getCourseById(int id);

    ResEnrollmentDTO enrollCourse(String username, int courseId);

    List<ResEnrollmentDTO> getMyCourses(String username);

    Long countCourses(Map<String, String> params);
}

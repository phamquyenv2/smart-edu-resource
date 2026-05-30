/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.service.impl;

import com.paq.pojo.Enrollment;
import com.paq.pojo.LearningLog;
import com.paq.pojo.Resource;
import com.paq.pojo.response.ResLearningLogDTO;
import com.paq.repository.EnrollmentRepository;
import com.paq.repository.LearningLogRepository;
import com.paq.repository.ResourceRepository;
import com.paq.service.StudentLearningService;
import com.paq.utils.DTOMapper;
import com.paq.utils.error.IdInvalidException;
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
public class StudentLearningServiceImpl implements StudentLearningService {

    @Autowired
    private LearningLogRepository learningLogRepo;

    @Autowired
    private ResourceRepository resourcementRepo;

    @Autowired
    private EnrollmentRepository enrollmentRepo;

    @Override
    public ResLearningLogDTO startLearning(String username, int resourceId) {
        Resource resource = this.resourcementRepo.getResourceById(resourceId);

        if (resource == null || Boolean.TRUE.equals(resource.getIsDeleted())) {
            throw new IdInvalidException("Không tìm thấy học liệu!");
        }

        List<Enrollment> enrollments = this.enrollmentRepo.getEnrollmentsByUsername(username);

        if (enrollments == null || enrollments.isEmpty()) {
            throw new IllegalArgumentException("Bạn chưa đăng ký khóa học nào!");
        }

        Enrollment enrollment = enrollments.get(0);

        LearningLog log = new LearningLog();
        log.setResourceId(resource);
        log.setEnrollmentId(enrollment);
        log.setStartTime(new Date());
        log.setCompletionStatus(0);

        return DTOMapper.toLearningLogDTO(this.learningLogRepo.addLearningLog(log));
    }

    @Override
    public ResLearningLogDTO completeLearning(String username, int resourceId) {
        Resource resource = this.resourcementRepo.getResourceById(resourceId);

        if (resource == null || Boolean.TRUE.equals(resource.getIsDeleted())) {
            throw new IdInvalidException("Không tìm thấy học liệu!");
        }

        List<Enrollment> enrollments = this.enrollmentRepo.getEnrollmentsByUsername(username);

        if (enrollments == null || enrollments.isEmpty()) {
            throw new IllegalArgumentException("Bạn chưa đăng ký khóa học nào!");
        }

        Enrollment enrollment = enrollments.get(0);
        LearningLog log = new LearningLog();
        log.setResourceId(resource);
        log.setEnrollmentId(enrollment);
        log.setStartTime(new Date());
        log.setEndTime(new Date());
        log.setCompletionStatus(1);

        return DTOMapper.toLearningLogDTO(this.learningLogRepo.addLearningLog(log));
    }

    @Override
    public List<ResLearningLogDTO> getHistory(String username) {
        return this.learningLogRepo.getLearningLogsByUsername(username)
                .stream()
                .map(l -> DTOMapper.toLearningLogDTO(l))
                .collect(Collectors.toList());
    }

}

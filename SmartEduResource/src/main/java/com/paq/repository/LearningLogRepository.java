/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.repository;

import com.paq.pojo.LearningLog;
import java.util.List;

/**
 *
 * @author Admin
 */
public interface LearningLogRepository {

    LearningLog addLearningLog(LearningLog log);

    LearningLog getCompletedLog(int enrollmentId, int resourceId);

    List<Integer> getCompletedResourceIdsByEnrollmentId(int enrollmentId);

    long countCompletedResourcesByEnrollmentId(int enrollmentId);

    List<LearningLog> getLearningLogsByUsername(String username);
}

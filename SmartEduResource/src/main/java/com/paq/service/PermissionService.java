package com.paq.service;

import com.paq.pojo.User;

public interface PermissionService {

    User getCurrentUser();

    void requireAdmin();

    void requireLecturerOrAdmin();

    void requireStudent();

    void requireCurrentUserOrAdmin(Integer userId);

    void requireResourceOwnerOrAdmin(Integer resourceId);

    void requireCourseLecturerOrAdmin(Integer courseId);

    void requireEnrollmentOrAdmin(Integer courseId);

    void requireCourseAccess(Integer courseId);

    void requireQuizAccess(Integer quizId);

    void requirePaymentOwnerOrAdmin(Integer paymentId);

    void requireQuizOwnerOrAdmin(Integer quizId);

    void requireChatRoomManager(Integer roomId);

    void requireChatRoomAccess(Integer roomId);

    boolean canManageChatRooms(User user);

    boolean canAccessCourse(User user, Integer courseId);
}

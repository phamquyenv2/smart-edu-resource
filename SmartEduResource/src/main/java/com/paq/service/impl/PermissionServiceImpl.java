package com.paq.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paq.pojo.ChatRoom;
import com.paq.pojo.Course;
import com.paq.pojo.Payment;
import com.paq.pojo.Quiz;
import com.paq.pojo.Resource;
import com.paq.pojo.User;
import com.paq.repository.ChatParticipantRepository;
import com.paq.repository.ChatRoomRepository;
import com.paq.repository.CourseLessonRepository;
import com.paq.repository.CourseRepository;
import com.paq.repository.EnrollmentRepository;
import com.paq.repository.PaymentRepository;
import com.paq.repository.QuizRepository;
import com.paq.repository.ResourceRepository;
import com.paq.repository.UserRepository;
import com.paq.service.PermissionService;
import com.paq.utils.constant.ChatRoomTypeEnum;
import com.paq.utils.constant.RoleEnum;
import com.paq.utils.error.IdInvalidException;
import com.paq.utils.error.PermissionException;

@Service
@Transactional
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ResourceRepository resourceRepo;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private CourseLessonRepository courseLessonRepo;

    @Autowired
    private EnrollmentRepository enrollmentRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    @Autowired
    private QuizRepository quizRepo;

    @Autowired
    private ChatRoomRepository chatRoomRepo;

    @Autowired
    private ChatParticipantRepository chatParticipantRepo;

    @Override
    public void requireAdmin() {
        if (!this.isAdmin(this.getCurrentUser())) {
            throw new PermissionException("Bạn không có quyền admin");
        }
    }

    @Override
    public void requireLecturerOrAdmin() {
        User user = this.getCurrentUser();
        if (!this.isAdmin(user) && !this.isLecturer(user)) {
            throw new PermissionException("Bạn không có quyền giảng viên hoặc admin");
        }
    }

    @Override
    public void requireStudent() {
        if (!this.isStudent(this.getCurrentUser())) {
            throw new PermissionException("Chỉ sinh viên mới có quyền thực hiện thao tác này");
        }
    }

    @Override
    public void requireCurrentUserOrAdmin(Integer userId) {
        User user = this.getCurrentUser();
        if (!this.isAdmin(user) && (userId == null || !userId.equals(user.getId()))) {
            throw new PermissionException("Bạn không có quyền thao tác với người dùng này");
        }
    }

    @Override
    public void requireResourceOwnerOrAdmin(Integer resourceId) {
        User user = this.getCurrentUser();
        Resource resource = this.resourceRepo.getResourceById(resourceId);
        if (resource == null) {
            throw new IdInvalidException("Resource khong ton tai");
        }

        if (!this.isAdmin(user) && !(this.isLecturer(user) && this.isOwner(user, resource.getUploadBy()))) {
            throw new PermissionException("Bạn không có quyền thao tác với học liệu này");
        }
    }

    @Override
    public void requireCourseLecturerOrAdmin(Integer courseId) {
        User user = this.getCurrentUser();
        Course course = this.courseRepo.getCourseById(courseId);
        if (course == null) {
            throw new IdInvalidException("Course không tồn tại");
        }

        if (this.isAdmin(user)) {
            return;
        }

        if (!this.isLecturer(user)
                || (!this.isCourseLecturer(course, user) && !this.isOwner(user, course.getCreatedBy()))) {
            throw new PermissionException("Bạn không có quyền thao tác với khóa học này");
        }
    }

    @Override
    public void requireEnrollmentOrAdmin(Integer courseId) {
        User user = this.getCurrentUser();
        Course course = this.courseRepo.getCourseById(courseId);
        if (course == null) {
            throw new IdInvalidException("Course không tồn tại");
        }

        if (this.isAdmin(user)) {
            return;
        }

        if (!this.canAccessCourse(user, courseId)) {
            throw new PermissionException("Bạn chưa ghi danh khóa học này");
        }
    }

    @Override
    public void requireCourseAccess(Integer courseId) {
        User user = this.getCurrentUser();
        Course course = this.courseRepo.getCourseById(courseId);
        if (course == null) {
            throw new IdInvalidException("Course khong ton tai");
        }

        if (!this.canAccessCourse(user, courseId)) {
            throw new PermissionException("Ban khong co quyen truy cap khoa hoc nay");
        }
    }

    @Override
    public void requireQuizAccess(Integer quizId) {
        Quiz quiz = this.quizRepo.getQuizById(quizId);
        if (quiz == null || Boolean.TRUE.equals(quiz.getIsDeleted())) {
            throw new IdInvalidException("Quiz khong ton tai");
        }

        if (quiz.getCourseId() == null || quiz.getCourseId().getId() == null) {
            throw new PermissionException("Quiz khong thuoc khoa hoc nao");
        }

        this.requireCourseAccess(quiz.getCourseId().getId());
    }

    @Override
    public void requirePaymentOwnerOrAdmin(Integer paymentId) {
        User user = this.getCurrentUser();
        Payment payment = this.paymentRepo.getPaymentById(paymentId);
        if (payment == null) {
            throw new IdInvalidException("Payment không tồn tại");
        }

        User paymentOwner = payment.getEnrollmentId().getStudentId().getUserId();
        if (!this.isAdmin(user) && !this.isOwner(user, paymentOwner)) {
            throw new PermissionException("Bạn không có quyền thao tác với thanh toán này");
        }
    }

    @Override
    public void requireQuizOwnerOrAdmin(Integer quizId) {
        User user = this.getCurrentUser();
        Quiz quiz = this.quizRepo.getQuizById(quizId);
        if (quiz == null || Boolean.TRUE.equals(quiz.getIsDeleted())) {
            throw new IdInvalidException("Quiz không tồn tại");
        }

        if (!this.isAdmin(user) && !this.isOwner(user, quiz.getCreatedBy())) {
            throw new PermissionException("Bạn không có quyền thao tác với bài kiểm tra này");
        }
    }

    @Override
    public void requireChatRoomManager(Integer roomId) {
        User user = this.getCurrentUser();
        ChatRoom room = this.chatRoomRepo.getRoomById(roomId);
        if (room == null) {
            throw new IdInvalidException("Chat room không tồn tại");
        }

        if (this.isChatRoomManager(user, room)) {
            return;
        }

        throw new PermissionException("Bạn không có quyền quản lý phòng chat này");
    }

    @Override
    public void requireChatRoomAccess(Integer roomId) {
        User user = this.getCurrentUser();
        ChatRoom room = this.chatRoomRepo.getRoomById(roomId);
        if (room == null) {
            throw new IdInvalidException("Chat room không tồn tại");
        }

        if (this.isChatRoomManager(user, room)
                || this.chatParticipantRepo.getParticipantByRoomIdAndUserId(roomId, user.getId()) != null
                || (room.getType() == ChatRoomTypeEnum.CLASS
                && room.getCourseId() != null
                && this.enrollmentRepo.existsByCourseIdAndUserId(room.getCourseId().getId(), user.getId()))) {
            return;
        }

        throw new PermissionException("Bạn không có quyền xem phòng chat này");
    }

    @Override
    public boolean canManageChatRooms(User user) {
        return this.isAdmin(user);
    }

    @Override
    public boolean canAccessCourse(User user, Integer courseId) {
        if (user == null || courseId == null) {
            return false;
        }

        Course course = this.courseRepo.getCourseById(courseId);
        if (course == null) {
            return false;
        }

        if (this.isAdmin(user) || (this.isLecturer(user) && this.isCourseLecturer(course, user))) {
            return true;
        }

        if (!this.isStudent(user) || user.getStudent() == null) {
            return false;
        }

        Integer studentId = user.getStudent().getId();
        boolean hasEnrollment = this.courseLessonRepo.hasSuccessfulEnrollment(courseId, studentId);
        boolean hasPayment = !Boolean.TRUE.equals(course.getIsPaid())
                || this.courseLessonRepo.hasSuccessfulPayment(courseId, studentId);

        return hasEnrollment && hasPayment;
    }

    @Override
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new PermissionException("Bạn chưa đăng nhập");
        }

        User user = this.userRepo.getUserByUsername(auth.getName());
        if (user == null || Boolean.FALSE.equals(user.getIsActive())) {
            throw new PermissionException("Tài khoản không hợp lệ");
        }

        return user;
    }

    private boolean isAdmin(User user) {
        return user != null && user.getRole() == RoleEnum.ADMIN;
    }

    private boolean isLecturer(User user) {
        return user != null && user.getRole() == RoleEnum.LECTURER;
    }

    private boolean isStudent(User user) {
        return user != null && user.getRole() == RoleEnum.STUDENT;
    }

    private boolean isOwner(User user, User owner) {
        return user != null && owner != null && user.getId().equals(owner.getId());
    }

    private boolean isCourseLecturer(Course course, User user) {
        return course != null
                && course.getLecturerId() != null
                && course.getLecturerId().getUserId() != null
                && this.isOwner(user, course.getLecturerId().getUserId());
    }

    private boolean isChatRoomManager(User user, ChatRoom room) {
        if (this.isAdmin(user) || this.isOwner(user, room.getCreatedBy())) {
            return true;
        }

        return room.getCourseId() != null && this.isLecturer(user) && this.isCourseLecturer(room.getCourseId(), user);
    }

}

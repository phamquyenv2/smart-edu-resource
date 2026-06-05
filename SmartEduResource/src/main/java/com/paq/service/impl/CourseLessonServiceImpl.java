package com.paq.service.impl;

import com.paq.pojo.Course;
import com.paq.pojo.CourseLesson;
import com.paq.pojo.Enrollment;
import com.paq.pojo.Quiz;
import com.paq.pojo.Resource;
import com.paq.pojo.Student;
import com.paq.pojo.User;
import com.paq.pojo.request.ReqCourseLessonDTO;
import com.paq.pojo.response.ResCourseLearnDTO;
import com.paq.pojo.response.ResCourseLessonDTO;
import com.paq.repository.CourseLessonRepository;
import com.paq.repository.CourseRepository;
import com.paq.repository.EnrollmentRepository;
import com.paq.repository.LearningLogRepository;
import com.paq.repository.QuizRepository;
import com.paq.repository.UserRepository;
import com.paq.service.CourseLessonService;
import com.paq.service.NotificationPublisherService;
import com.paq.service.PermissionService;
import com.paq.utils.DTOMapper;
import com.paq.utils.constant.RoleEnum;
import com.paq.utils.error.IdInvalidException;
import com.paq.utils.error.PermissionException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CourseLessonServiceImpl implements CourseLessonService {

    @Autowired
    private CourseLessonRepository lessonRepo;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private EnrollmentRepository enrollmentRepo;

    @Autowired
    private LearningLogRepository learningLogRepo;

    @Autowired
    private QuizRepository quizRepo;

    @Autowired
    private NotificationPublisherService notificationPublisher;

    @Autowired
    private PermissionService permissionService;

    @Override
    public ResCourseLearnDTO getLearnPage(int courseId, String username) {
        Course course = this.courseRepo.getCourseById(courseId);
        if (course == null) {
            throw new IdInvalidException("Course không tồn tại");
        }

        User user = this.userRepo.getUserByUsername(username);
        if (user == null || Boolean.FALSE.equals(user.getIsActive())) {
            throw new PermissionException("Tài khoản không hợp lệ");
        }

        Student student = this.userRepo.getStudentByUserId(user.getId());
        if (student == null) {
            throw new PermissionException("Không tìm thấy hồ sơ sinh viên");
        }

        this.permissionService.requireCourseAccess(courseId);

        List<CourseLesson> lessons = this.lessonRepo.getLessonsByCourseId(courseId);
        ResCourseLearnDTO dto = DTOMapper.toResCourseLearnDTO(course, lessons, true, "SUCCESS");
        this.markCompletedLessons(dto, courseId, student.getId());

        return dto;
    }

    @Override
    public List<ResCourseLessonDTO> getLessonsByCourseId(int courseId) {
        return this.lessonRepo.getLessonsByCourseId(courseId)
                .stream()
                .map(DTOMapper::toResCourseLessonDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ResCourseLessonDTO getLessonById(int id) {
        CourseLesson lesson = this.lessonRepo.getLessonById(id);
        if (lesson == null) {
            throw new IdInvalidException("Bài học không tồn tại");
        }
        return DTOMapper.toResCourseLessonDTO(lesson);
    }

    @Override
    public ResCourseLessonDTO createLesson(ReqCourseLessonDTO request) {
        this.requireLecturerOrAdmin();

        Course course = this.courseRepo.getCourseById(request.getCourseId());
        if (course == null) {
            throw new IdInvalidException("Course không tồn tại");
        }

        this.validateLessonPosition(request.getCourseId(), request.getChapterNum(), request.getLessonNum(), null);

        CourseLesson lesson = new CourseLesson();
        lesson.setIsDeleted(false);
        this.copyFields(lesson, request, course);
        this.lessonRepo.addLesson(lesson);
        this.notificationPublisher.notifyCourseStudents(
                course.getId(),
                "Khóa học có bài học mới",
                "Bài học \"" + lesson.getTitle() + "\" vừa được thêm vào khóa học " + course.getName() + ".");
        return DTOMapper.toResCourseLessonDTO(lesson);
    }

    @Override
    public ResCourseLessonDTO updateLesson(int id, ReqCourseLessonDTO request) {
        this.requireLecturerOrAdmin();

        CourseLesson lesson = this.lessonRepo.getLessonById(id);
        if (lesson == null) {
            throw new IdInvalidException("Bài học không tồn tại");
        }

        Course course = this.courseRepo.getCourseById(request.getCourseId());
        if (course == null) {
            throw new IdInvalidException("Course không tồn tại");
        }

        this.validateLessonPosition(request.getCourseId(), request.getChapterNum(), request.getLessonNum(), id);

        this.copyFields(lesson, request, course);
        this.lessonRepo.updateLesson(lesson);
        return DTOMapper.toResCourseLessonDTO(lesson);
    }

    @Override
    public void deleteLesson(int id) {
        this.requireLecturerOrAdmin();
        CourseLesson lesson = this.lessonRepo.getLessonById(id);
        if (lesson == null) {
            throw new IdInvalidException("Bài học không tồn tại");
        }
        this.lessonRepo.deleteLesson(id);
    }

    private void copyFields(CourseLesson lesson, ReqCourseLessonDTO request, Course course) {
        lesson.setTitle(request.getTitle());
        lesson.setChapterNum(request.getChapterNum());
        lesson.setLessonNum(request.getLessonNum());
        lesson.setIsFree(Boolean.TRUE.equals(request.getIsFree()));
        lesson.setCourseId(course);
        lesson.setResourceId(request.getResourceId() != null ? new Resource(request.getResourceId()) : null);
        lesson.setQuizId(request.getQuizId() != null ? new Quiz(request.getQuizId()) : null);
    }

    private void validateLessonPosition(int courseId, Integer chapterNum, Integer lessonNum, Integer excludeLessonId) {
        if (chapterNum == null || chapterNum <= 0 || lessonNum == null || lessonNum <= 0) {
            throw new IllegalArgumentException("Chương và bài số phải là số nguyên lớn hơn 0");
        }
        List<CourseLesson> existingLessons = this.lessonRepo.getLessonsByCourseId(courseId);
        for (CourseLesson l : existingLessons) {
            if (l.getChapterNum() != null && l.getLessonNum() != null
                    && l.getChapterNum().equals(chapterNum) && l.getLessonNum().equals(lessonNum)) {
                if (excludeLessonId == null || !l.getId().equals(excludeLessonId)) {
                    throw new IllegalArgumentException(String.format("Vị trí bài học %d.%d đã tồn tại! Vui lòng chọn số bài khác.", chapterNum, lessonNum));
                }
            }
        }
    }

    private void markCompletedLessons(ResCourseLearnDTO dto, int courseId, int studentId) {
        if (dto == null || dto.getChapters() == null) {
            return;
        }

        Enrollment enrollment = this.enrollmentRepo.findByCourseAndStudent(courseId, studentId);
        if (enrollment == null) {
            return;
        }

        Set<Integer> completedResourceIds = Set.copyOf(
                this.learningLogRepo.getCompletedResourceIdsByEnrollmentId(enrollment.getId())
        );
        Set<Integer> completedQuizIds = Set.copyOf(
                this.quizRepo.getSubmittedQuizIdsByStudentAndCourse(studentId, courseId)
        );

        dto.getChapters().forEach(chapter -> chapter.getLessons().forEach(lesson -> {
            boolean resourceCompleted = lesson.getResourceId() != null
                    && completedResourceIds.contains(lesson.getResourceId());
            boolean quizCompleted = lesson.getQuizId() != null
                    && completedQuizIds.contains(lesson.getQuizId());
            boolean hasResource = lesson.getResourceId() != null;
            boolean hasQuiz = lesson.getQuizId() != null;

            lesson.setResourceCompleted(resourceCompleted);
            lesson.setQuizCompleted(quizCompleted);
            lesson.setCompleted((!hasResource || resourceCompleted) && (!hasQuiz || quizCompleted));
        }));
    }

    private void requireLecturerOrAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new PermissionException("Bạn chưa đăng nhập");
        }
        User user = this.userRepo.getUserByUsername(auth.getName());
        if (user == null || Boolean.FALSE.equals(user.getIsActive())) {
            throw new PermissionException("Tài khoản không hợp lệ");
        }
        RoleEnum role = user.getRole();
        if (role != RoleEnum.ADMIN && role != RoleEnum.LECTURER) {
            throw new PermissionException("Chỉ giảng viên hoặc admin mới có quyền thực hiện thao tác này");
        }
    }
}

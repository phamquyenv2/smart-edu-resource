package com.paq.repository.impl;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.paq.repository.DashboardRepository;
import com.paq.utils.constant.EnrollmentStatusEnum;
import com.paq.utils.constant.PaymentStatusEnum;
import com.paq.utils.constant.RoleEnum;

@Repository
@Transactional
public class DashboardRepositoryImpl implements DashboardRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public long countActiveUsers() {
        return this.count("SELECT COUNT(u.id) FROM User u WHERE u.isActive = true OR u.isActive IS NULL");
    }

    @Override
    public long countUsersByRole(RoleEnum role) {
        Session session = this.factory.getObject().getCurrentSession();
        Long result = session.createQuery(
                "SELECT COUNT(u.id) FROM User u WHERE u.role = :role AND (u.isActive = true OR u.isActive IS NULL)",
                Long.class)
                .setParameter("role", role)
                .getSingleResult();
        return this.safeLong(result);
    }

    @Override
    public long countStudents() {
        return this.count("SELECT COUNT(s.id) FROM Student s JOIN s.userId u "
                + "WHERE u.isActive = true OR u.isActive IS NULL");
    }

    @Override
    public long countLecturers() {
        return this.count("SELECT COUNT(l.id) FROM Lecturer l JOIN l.userId u "
                + "WHERE u.isActive = true OR u.isActive IS NULL");
    }

    @Override
    public long countPendingLecturers() {
        return this.count("SELECT COUNT(l.id) FROM Lecturer l JOIN l.userId u "
                + "WHERE (u.isActive = true OR u.isActive IS NULL) "
                + "AND (l.isApprove = false OR l.isApprove IS NULL)");
    }

    @Override
    public long countCourses() {
        return this.count("SELECT COUNT(c.id) FROM Course c WHERE c.isDeleted = false OR c.isDeleted IS NULL");
    }

    @Override
    public long countResources() {
        return this.count("SELECT COUNT(r.id) FROM Resource r WHERE r.isDeleted = false OR r.isDeleted IS NULL");
    }

    @Override
    public long countQuizzes() {
        return this.count("SELECT COUNT(q.id) FROM Quiz q WHERE q.isDeleted = false OR q.isDeleted IS NULL");
    }

    @Override
    public long countEnrollments() {
        return this.count("SELECT COUNT(e.id) FROM Enrollment e JOIN e.courseId c "
                + "WHERE c.isDeleted = false OR c.isDeleted IS NULL");
    }

    @Override
    public long countQuizAttempts() {
        return this.count("SELECT COUNT(qa.id) FROM QuizAttempt qa JOIN qa.quizId q JOIN q.courseId c "
                + "WHERE (q.isDeleted = false OR q.isDeleted IS NULL) "
                + "AND (c.isDeleted = false OR c.isDeleted IS NULL)");
    }

    @Override
    public double getAverageLearningProgress() {
        return this.safeDouble(this.aggregateDouble(
                "SELECT AVG(e.overallProgress) FROM Enrollment e JOIN e.courseId c "
                + "WHERE c.isDeleted = false OR c.isDeleted IS NULL"));
    }

    @Override
    public long getTotalStudyTime() {
        return this.safeLong(this.aggregateLong(
                "SELECT SUM(e.totalStudyTime) FROM Enrollment e JOIN e.courseId c "
                + "WHERE c.isDeleted = false OR c.isDeleted IS NULL"));
    }

    @Override
    public double getAverageQuizScore() {
        return this.safeDouble(this.aggregateDouble(
                "SELECT AVG(qa.score) FROM QuizAttempt qa JOIN qa.quizId q JOIN q.courseId c "
                + "WHERE (q.isDeleted = false OR q.isDeleted IS NULL) "
                + "AND (c.isDeleted = false OR c.isDeleted IS NULL)"));
    }

    @Override
    public long getTotalRevenue() {
        Session session = this.factory.getObject().getCurrentSession();
        Long result = session.createQuery(
                "SELECT SUM(p.amount) FROM Payment p JOIN p.enrollmentId e JOIN e.courseId c "
                + "WHERE p.status = :status "
                + "AND (c.isDeleted = false OR c.isDeleted IS NULL)",
                Long.class)
                .setParameter("status", PaymentStatusEnum.SUCCESS)
                .getSingleResult();
        return this.safeLong(result);
    }

    @Override
    public long countPaymentsByStatus(PaymentStatusEnum status) {
        Session session = this.factory.getObject().getCurrentSession();
        Long result = session.createQuery(
                "SELECT COUNT(p.id) FROM Payment p JOIN p.enrollmentId e JOIN e.courseId c "
                + "WHERE p.status = :status "
                + "AND (c.isDeleted = false OR c.isDeleted IS NULL)",
                Long.class)
                .setParameter("status", status)
                .getSingleResult();
        return this.safeLong(result);
    }

    @Override
    public long countLecturerCourses(int userId) {
        return this.countByUser(
                "SELECT COUNT(DISTINCT c.id) FROM Course c WHERE "
                + this.lecturerCoursePredicate("c"),
                userId);
    }

    @Override
    public long countLecturerStudents(int userId) {
        return this.countByUser(
                "SELECT COUNT(DISTINCT e.studentId.id) FROM Enrollment e "
                + "JOIN e.courseId c JOIN e.studentId s JOIN s.userId u WHERE "
                + this.lecturerCoursePredicate("c")
                + " AND (u.isActive = true OR u.isActive IS NULL)",
                userId);
    }

    @Override
    public long countLecturerResources(int userId) {
        return this.countByUser(
                "SELECT COUNT(r.id) FROM Resource r WHERE r.uploadBy.id = :userId "
                + "AND (r.isDeleted = false OR r.isDeleted IS NULL)",
                userId);
    }

    @Override
    public long countLecturerQuizzes(int userId) {
        return this.countByUser(
                "SELECT COUNT(DISTINCT q.id) FROM Quiz q JOIN q.courseId c WHERE "
                + this.lecturerCoursePredicate("c") + " AND (q.isDeleted = false OR q.isDeleted IS NULL)",
                userId);
    }

    @Override
    public long countLecturerEnrollments(int userId) {
        return this.countByUser(
                "SELECT COUNT(e.id) FROM Enrollment e JOIN e.courseId c WHERE "
                + this.lecturerCoursePredicate("c"),
                userId);
    }

    @Override
    public long countLecturerQuizAttempts(int userId) {
        return this.countByUser(
                "SELECT COUNT(qa.id) FROM QuizAttempt qa JOIN qa.quizId q JOIN q.courseId c WHERE "
                + this.lecturerCoursePredicate("c") + " AND (q.isDeleted = false OR q.isDeleted IS NULL)",
                userId);
    }

    @Override
    public double getLecturerAverageLearningProgress(int userId) {
        return this.safeDouble(this.aggregateDoubleByUser(
                "SELECT AVG(e.overallProgress) FROM Enrollment e JOIN e.courseId c WHERE "
                + this.lecturerCoursePredicate("c"),
                userId));
    }

    @Override
    public long getLecturerTotalStudyTime(int userId) {
        return this.safeLong(this.aggregateLongByUser(
                "SELECT SUM(e.totalStudyTime) FROM Enrollment e JOIN e.courseId c WHERE "
                + this.lecturerCoursePredicate("c"),
                userId));
    }

    @Override
    public double getLecturerAverageQuizScore(int userId) {
        return this.safeDouble(this.aggregateDoubleByUser(
                "SELECT AVG(qa.score) FROM QuizAttempt qa JOIN qa.quizId q JOIN q.courseId c WHERE "
                + this.lecturerCoursePredicate("c") + " AND (q.isDeleted = false OR q.isDeleted IS NULL)",
                userId));
    }

    @Override
    public long getLecturerTotalRevenue(int userId) {
        Session session = this.factory.getObject().getCurrentSession();
        Long result = session.createQuery(
                "SELECT SUM(p.amount) FROM Payment p JOIN p.enrollmentId e JOIN e.courseId c WHERE "
                + this.lecturerCoursePredicate("c") + " AND p.status = :status",
                Long.class)
                .setParameter("userId", userId)
                .setParameter("status", PaymentStatusEnum.SUCCESS)
                .getSingleResult();
        return this.safeLong(result);
    }

    @Override
    public long countLecturerPaymentsByStatus(int userId, PaymentStatusEnum status) {
        Session session = this.factory.getObject().getCurrentSession();
        Long result = session.createQuery(
                "SELECT COUNT(p.id) FROM Payment p JOIN p.enrollmentId e JOIN e.courseId c WHERE "
                + this.lecturerCoursePredicate("c") + " AND p.status = :status",
                Long.class)
                .setParameter("userId", userId)
                .setParameter("status", status)
                .getSingleResult();
        return this.safeLong(result);
    }

    private long count(String hql) {
        Session session = this.factory.getObject().getCurrentSession();
        Query<Long> q = session.createQuery(hql, Long.class);
        Long result = q.getSingleResult();
        return this.safeLong(result);
    }

    private Double aggregateDouble(String hql) {
        Session session = this.factory.getObject().getCurrentSession();
        Query<Double> q = session.createQuery(hql, Double.class);
        return q.getSingleResult();
    }

    private Long aggregateLong(String hql) {
        Session session = this.factory.getObject().getCurrentSession();
        Query<Long> q = session.createQuery(hql, Long.class);
        return q.getSingleResult();
    }

    private long countByUser(String hql, int userId) {
        Session session = this.factory.getObject().getCurrentSession();
        Long result = session.createQuery(hql, Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
        return this.safeLong(result);
    }

    private Double aggregateDoubleByUser(String hql, int userId) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.createQuery(hql, Double.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    private Long aggregateLongByUser(String hql, int userId) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.createQuery(hql, Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    private String lecturerCoursePredicate(String courseAlias) {
        return "(" + courseAlias + ".isDeleted = false OR " + courseAlias + ".isDeleted IS NULL) "
                + "AND (" + courseAlias + ".createdBy.id = :userId "
                + "OR " + courseAlias + ".lecturerId.userId.id = :userId)";
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private double safeDouble(Double value) {
        return value == null ? 0D : value;
    }

    @Override
    public long countStudentEnrollments(String username) {
        Session session = this.factory.getObject().getCurrentSession();

        Long result = session.createQuery(
                "SELECT COUNT(e.id) "
                + "FROM Enrollment e "
                + "WHERE e.studentId.userId.username = :username "
                + "AND e.status = :status",
                Long.class)
                .setParameter("username", username)
                .setParameter("status", EnrollmentStatusEnum.SUCCESS)
                .getSingleResult();

        return this.safeLong(result);
    }

    @Override
    public long countStudentCompletedResources(String username) {
        Session session = this.factory.getObject().getCurrentSession();

        Long result = session.createQuery(
                "SELECT COUNT(l.id) "
                + "FROM LearningLog l "
                + "WHERE l.enrollmentId.studentId.userId.username = :username "
                + "AND l.completionStatus = 1",
                Long.class)
                .setParameter("username", username)
                .getSingleResult();

        return this.safeLong(result);
    }

    @Override
    public long countStudentLearningLogs(String username) {
        Session session = this.factory.getObject().getCurrentSession();

        Long result = session.createQuery(
                "SELECT COUNT(l.id) "
                + "FROM LearningLog l "
                + "WHERE l.enrollmentId.studentId.userId.username = :username",
                Long.class)
                .setParameter("username", username)
                .getSingleResult();

        return this.safeLong(result);
    }

    @Override
    public long countStudentQuizAttempts(String username) {
        Session session = this.factory.getObject().getCurrentSession();

        Long result = session.createQuery(
                "SELECT COUNT(qa.id) "
                + "FROM QuizAttempt qa "
                + "WHERE qa.studentId.userId.username = :username",
                Long.class)
                .setParameter("username", username)
                .getSingleResult();

        return this.safeLong(result);
    }

    @Override
    public double getStudentAverageQuizScore(String username) {
        Session session = this.factory.getObject().getCurrentSession();

        Double result = session.createQuery(
                "SELECT AVG(qa.score) "
                + "FROM QuizAttempt qa "
                + "WHERE qa.studentId.userId.username = :username",
                Double.class)
                .setParameter("username", username)
                .getSingleResult();

        return this.safeDouble(result);
    }

    @Override
    public long getStudentTotalStudyTime(String username) {
        Session session = this.factory.getObject().getCurrentSession();

        Long result = session.createQuery(
                "SELECT SUM(e.totalStudyTime) "
                + "FROM Enrollment e "
                + "WHERE e.studentId.userId.username = :username",
                Long.class)
                .setParameter("username", username)
                .getSingleResult();

        return this.safeLong(result);
    }
}

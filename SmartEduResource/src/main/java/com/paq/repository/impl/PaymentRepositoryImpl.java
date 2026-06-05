package com.paq.repository.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.paq.pojo.Payment;
import com.paq.repository.PaymentRepository;
import com.paq.utils.constant.PaymentMethodEnum;
import com.paq.utils.constant.PaymentStatusEnum;

import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Repository
@PropertySource("classpath:configs.properties")
@Transactional
public class PaymentRepositoryImpl implements PaymentRepository {

    @Autowired
    private Environment env;

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<Payment> getPayments(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = session.getCriteriaBuilder();
        CriteriaQuery<Payment> q = b.createQuery(Payment.class);
        Root<Payment> root = q.from(Payment.class);

        Join<Object, Object> enrollment = root.join("enrollmentId", JoinType.INNER);
        Join<Object, Object> course = enrollment.join("courseId", JoinType.INNER);
        Join<Object, Object> student = enrollment.join("studentId", JoinType.INNER);
        Join<Object, Object> user = student.join("userId", JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(b.or(b.isFalse(course.get("isDeleted")), b.isNull(course.get("isDeleted"))));

        if (params != null) {
            String status = params.get("status");
            if (status != null && !status.isEmpty()) {
                predicates.add(b.equal(root.get("status"), PaymentStatusEnum.valueOf(status)));
            }

            String userId = params.get("userId");
            if (userId != null && !userId.isEmpty()) {
                predicates.add(b.equal(user.get("id"), Integer.parseInt(userId)));
            }

            String keyword = params.get("keyword");
            if (keyword != null && !keyword.isBlank()) {
                String like = String.format("%%%s%%", keyword.trim().toLowerCase());
                predicates.add(b.or(
                        b.like(b.lower(root.get("transactionCode")), like),
                        b.like(b.lower(course.get("name")), like),
                        b.like(b.lower(user.get("fullName")), like),
                        b.like(b.lower(user.get("email")), like)));
            }

            Date fromDate = this.parseDate(params.get("fromDate"));
            if (fromDate != null) {
                predicates.add(b.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            }

            Date toDate = this.parseDate(params.get("toDate"));
            if (toDate != null) {
                predicates.add(b.lessThan(root.get("createdAt"), this.nextDate(toDate)));
            }
        }

        q.select(root).distinct(true);
        q.where(predicates.toArray(Predicate[]::new));
        q.orderBy(b.desc(root.get("createdAt")), b.desc(root.get("id")));

        Query<Payment> query = session.createQuery(q);
        if (params != null) {
            int pageSize = this.env.getProperty("payments.page_size", Integer.class, 10);
            int page = Integer.parseInt(params.getOrDefault("page", "1"));
            int start = (page - 1) * pageSize;

            query.setMaxResults(pageSize);
            query.setFirstResult(start);
        }

        return query.getResultList();
    }

    @Override
    public Payment getPaymentById(int id) {
        try {
            Session session = this.factory.getObject().getCurrentSession();
            Query<Payment> q = session.createQuery(
                    "SELECT p FROM Payment p "
                    + "JOIN FETCH p.enrollmentId e "
                    + "JOIN FETCH e.courseId c "
                    + "JOIN FETCH e.studentId s "
                    + "JOIN FETCH s.userId "
                    + "WHERE p.id = :id "
                    + "AND (c.isDeleted = false OR c.isDeleted IS NULL)",
                    Payment.class);
            q.setParameter("id", id);
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public Payment updatePayment(Payment payment) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.merge(payment);
    }

    @Override
    public long countPayments(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = session.getCriteriaBuilder();
        CriteriaQuery<Long> q = b.createQuery(Long.class);
        Root<Payment> root = q.from(Payment.class);

        q.select(b.countDistinct(root.get("id")));
        q.where(this.buildPredicates(params, b, root).toArray(Predicate[]::new));

        return this.safeLong(session.createQuery(q).getSingleResult());
    }

    @Override
    public long countPaymentsByStatus(PaymentStatusEnum status, Map<String, String> params) {
        Map<String, String> normalizedParams = params == null ? new HashMap<>() : new HashMap<>(params);
        normalizedParams.put("status", status.name());
        return this.countPayments(normalizedParams);
    }

    @Override
    public long getTotalRevenue(Map<String, String> params) {
        Map<String, String> normalizedParams = params == null ? new HashMap<>() : new HashMap<>(params);
        normalizedParams.put("status", PaymentStatusEnum.SUCCESS.name());

        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = session.getCriteriaBuilder();
        CriteriaQuery<Long> q = b.createQuery(Long.class);
        Root<Payment> root = q.from(Payment.class);

        q.select(b.sum(root.get("amount")));
        q.where(this.buildRevenuePredicates(normalizedParams, b, root).toArray(Predicate[]::new));

        return this.safeLong(session.createQuery(q).getSingleResult());
    }

    @Override
    public Map<PaymentMethodEnum, Long> countPaymentsByMethod(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> q = b.createQuery(Object[].class);
        Root<Payment> root = q.from(Payment.class);

        q.multiselect(root.get("paymentMethod"), b.count(root.get("id")));
        q.where(this.buildPredicates(params, b, root).toArray(Predicate[]::new));
        q.groupBy(root.get("paymentMethod"));

        List<Object[]> rows = session.createQuery(q).getResultList();
        Map<PaymentMethodEnum, Long> results = new HashMap<>();
        for (Object[] row : rows) {
            if (row[0] != null) {
                results.put((PaymentMethodEnum) row[0], this.safeLong((Long) row[1]));
            }
        }

        return results;
    }

    @Override
    public List<Object[]> getRevenueByMonth(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> q = b.createQuery(Object[].class);
        Root<Payment> root = q.from(Payment.class);

        List<Predicate> predicates = this.buildRevenuePredicates(params, b, root);
        predicates.add(b.equal(root.get("status"), PaymentStatusEnum.SUCCESS));

        q.multiselect(
                b.function("YEAR", Integer.class, root.get("paidAt")),
                b.function("MONTH", Integer.class, root.get("paidAt")),
                b.sum(root.get("amount")),
                b.count(root.get("id"))
        );
        q.where(predicates.toArray(Predicate[]::new));
        q.groupBy(
                b.function("YEAR", Integer.class, root.get("paidAt")),
                b.function("MONTH", Integer.class, root.get("paidAt"))
        );
        q.orderBy(
                b.asc(b.function("YEAR", Integer.class, root.get("paidAt"))),
                b.asc(b.function("MONTH", Integer.class, root.get("paidAt")))
        );

        return session.createQuery(q).getResultList();
    }

    @Override
    public Map<String, Long> countPaymentsByUserRole(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> q = b.createQuery(Object[].class);
        Root<Payment> root = q.from(Payment.class);

        Join<Object, Object> enrollment = root.join("enrollmentId", JoinType.INNER);
        Join<Object, Object> course = enrollment.join("courseId", JoinType.INNER);
        Join<Object, Object> student = enrollment.join("studentId", JoinType.INNER);
        Join<Object, Object> user = student.join("userId", JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(b.or(b.isFalse(course.get("isDeleted")), b.isNull(course.get("isDeleted"))));

        if (params != null) {
            Date fromDate = this.parseDate(params.get("fromDate"));
            if (fromDate != null) {
                predicates.add(b.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            }
            Date toDate = this.parseDate(params.get("toDate"));
            if (toDate != null) {
                predicates.add(b.lessThan(root.get("createdAt"), this.nextDate(toDate)));
            }
        }

        q.multiselect(user.get("role"), b.count(root.get("id")));
        q.where(predicates.toArray(Predicate[]::new));
        q.groupBy(user.get("role"));

        List<Object[]> rows = session.createQuery(q).getResultList();
        Map<String, Long> results = new HashMap<>();
        for (Object[] row : rows) {
            if (row[0] != null) {
                results.put(row[0].toString(), this.safeLong((Long) row[1]));
            }
        }
        return results;
    }

    private List<Predicate> buildPredicates(Map<String, String> params, CriteriaBuilder b, Root<Payment> root) {
        return this.buildPredicates(params, b, root, "createdAt");
    }

    private List<Predicate> buildRevenuePredicates(Map<String, String> params, CriteriaBuilder b,
            Root<Payment> root) {
        List<Predicate> predicates = this.buildPredicates(params, b, root, "paidAt");
        predicates.add(b.isNotNull(root.get("paidAt")));
        return predicates;
    }

    private List<Predicate> buildPredicates(Map<String, String> params, CriteriaBuilder b, Root<Payment> root,
            String dateField) {
        List<Predicate> predicates = new ArrayList<>();
        Join<Object, Object> enrollment = root.join("enrollmentId", JoinType.INNER);
        Join<Object, Object> course = enrollment.join("courseId", JoinType.INNER);
        Join<Object, Object> student = enrollment.join("studentId", JoinType.INNER);
        Join<Object, Object> user = student.join("userId", JoinType.INNER);

        predicates.add(b.or(b.isFalse(course.get("isDeleted")), b.isNull(course.get("isDeleted"))));

        if (params == null) {
            return predicates;
        }

        String status = params.get("status");
        if (status != null && !status.isEmpty()) {
            predicates.add(b.equal(root.get("status"), PaymentStatusEnum.valueOf(status)));
        }

        String userId = params.get("userId");
        if (userId != null && !userId.isEmpty()) {
            predicates.add(b.equal(user.get("id"), Integer.parseInt(userId)));
        }

        String keyword = params.get("keyword");
        if (keyword != null && !keyword.isBlank()) {
            String like = String.format("%%%s%%", keyword.trim().toLowerCase());
            predicates.add(b.or(
                    b.like(b.lower(root.get("transactionCode")), like),
                    b.like(b.lower(course.get("name")), like),
                    b.like(b.lower(user.get("fullName")), like),
                    b.like(b.lower(user.get("email")), like)));
        }

        Date fromDate = this.parseDate(params.get("fromDate"));
        if (fromDate != null) {
            predicates.add(b.greaterThanOrEqualTo(root.get(dateField), fromDate));
        }

        Date toDate = this.parseDate(params.get("toDate"));
        if (toDate != null) {
            predicates.add(b.lessThan(root.get(dateField), this.nextDate(toDate)));
        }

        return predicates;
    }

    private Date parseDate(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(value);
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Ngày phải có định dạng yyyy-MM-dd");
        }
    }

    private Date nextDate(Date date) {
        return new Date(date.getTime() + 24L * 60L * 60L * 1000L);
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    @Override
    public List<Payment> getPaymentsByUsername(String username) {
        Session session = this.factory.getObject().getCurrentSession();

        Query<Payment> q = session.createQuery(
                "FROM Payment p "
                + "WHERE p.enrollmentId.studentId.userId.username = :username "
                + "ORDER BY p.createdAt DESC",
                Payment.class);

        q.setParameter("username", username);

        return q.getResultList();
    }

    @Override
    public Payment createPayment(Payment payment) {
        Session session = this.factory.getObject().getCurrentSession();

        session.persist(payment);

        return payment;
    }

    @Override
    public Payment getPaymentByTransactionCode(String transactionCode) {
        try {
            Session session = this.factory.getObject().getCurrentSession();
            Query<Payment> q = session.createQuery(
                    "SELECT p FROM Payment p "
                    + "JOIN FETCH p.enrollmentId e "
                    + "JOIN FETCH e.courseId c "
                    + "JOIN FETCH e.studentId s "
                    + "JOIN FETCH s.userId "
                    + "WHERE p.transactionCode = :code "
                    + "AND (c.isDeleted = false OR c.isDeleted IS NULL)",
                    Payment.class);
            q.setParameter("code", transactionCode);
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }
}


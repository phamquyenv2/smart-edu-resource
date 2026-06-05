package com.paq.repository.impl;

import com.paq.pojo.Payment;
import com.paq.repository.PaymentStatRepository;
import com.paq.utils.constant.PaymentMethodEnum;
import com.paq.utils.constant.PaymentStatusEnum;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class PaymentStatRepositoryImpl implements PaymentStatRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public long countPayments(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = session.getCriteriaBuilder();
        CriteriaQuery<Long> q = b.createQuery(Long.class);
        Root<Payment> root = q.from(Payment.class);

        q.select(b.countDistinct(root.get("id")));
        q.where(this.buildPredicates(params, b, root, "createdAt").toArray(Predicate[]::new));

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

        List<Predicate> predicates = this.buildPredicates(normalizedParams, b, root, "paidAt");
        predicates.add(b.isNotNull(root.get("paidAt")));

        q.select(b.sum(root.get("amount")));
        q.where(predicates.toArray(Predicate[]::new));

        return this.safeLong(session.createQuery(q).getSingleResult());
    }

    @Override
    public Map<PaymentMethodEnum, Long> countPaymentsByMethod(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> q = b.createQuery(Object[].class);
        Root<Payment> root = q.from(Payment.class);

        q.multiselect(root.get("paymentMethod"), b.count(root.get("id")));
        q.where(this.buildPredicates(params, b, root, "createdAt").toArray(Predicate[]::new));
        q.groupBy(root.get("paymentMethod"));
        q.orderBy(b.desc(b.count(root.get("id"))));

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

        Map<String, String> normalizedParams = params == null ? new HashMap<>() : new HashMap<>(params);
        normalizedParams.put("status", PaymentStatusEnum.SUCCESS.name());

        List<Predicate> predicates = this.buildPredicates(normalizedParams, b, root, "paidAt");
        predicates.add(b.isNotNull(root.get("paidAt")));

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
        Join<Object, Object> student = enrollment.join("studentId", JoinType.INNER);
        Join<Object, Object> user = student.join("userId", JoinType.INNER);

        q.multiselect(user.get("role"), b.count(root.get("id")));
        q.where(this.buildPredicates(params, b, root, "createdAt").toArray(Predicate[]::new));
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

        String method = params.get("method");
        if (method != null && !method.isEmpty()) {
            predicates.add(b.equal(root.get("paymentMethod"), PaymentMethodEnum.valueOf(method)));
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
            throw new IllegalArgumentException("Ngay phai co dinh dang yyyy-MM-dd");
        }
    }

    private Date nextDate(Date date) {
        return new Date(date.getTime() + 24L * 60L * 60L * 1000L);
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }
}

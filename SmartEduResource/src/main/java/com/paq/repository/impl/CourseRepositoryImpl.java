/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.repository.impl;

import com.paq.pojo.Course;
import com.paq.repository.CourseRepository;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
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

/**
 *
 * @author Admin
 */
@Repository
@Transactional
@PropertySource("classpath:configs.properties")
public class CourseRepositoryImpl implements CourseRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Autowired
    private Environment env;

    @Override
    public List<Course> getCourses(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = session.getCriteriaBuilder();
        CriteriaQuery<Course> q = b.createQuery(Course.class);
        Root<Course> root = q.from(Course.class);

        q.select(root).distinct(true);

        List<Predicate> predicates = this.buildCommonPredicates(b, root, params);
        if (!predicates.isEmpty()) {
            q.where(predicates.toArray(Predicate[]::new));
        }
        q.orderBy(b.desc(root.get("id")));

        Query<Course> query = session.createQuery(q);

        int pageSize = this.env.getProperty("courses.page_size", Integer.class);
        int page = params != null ? Integer.parseInt(params.getOrDefault("page", "1")) : 1;
        query.setMaxResults(pageSize);
        query.setFirstResult((page - 1) * pageSize);

        return query.getResultList();
    }

    @Override
    public Course getCourseById(int id) {
        Session s = this.factory.getObject().getCurrentSession();
        return s.get(Course.class, id);
    }

    @Override
    public Course getCourseByName(String name) {
        Session s = this.factory.getObject().getCurrentSession();

        Query<Course> q = s.createQuery(
                "FROM Course c WHERE c.name = :name AND c.isDeleted = false",
                Course.class
        );
        q.setParameter("name", name);

        try {
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public Course addOrUpdateCourse(Course course) {
        Session s = this.factory.getObject().getCurrentSession();
        if (course.getId() != null) {
            return s.merge(course);
        }
        s.persist(course);
        return course;
    }

    @Override
    public void deleteCourse(int id) {
        Session s = this.factory.getObject().getCurrentSession();
        Course course = s.get(Course.class, id);
        if (course != null) {
            course.setIsDeleted(Boolean.TRUE);
            s.merge(course);
        }
    }

    @Override
    public Long countCourses(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = session.getCriteriaBuilder();
        CriteriaQuery<Long> q = b.createQuery(Long.class);
        Root<Course> root = q.from(Course.class);

        q.select(b.countDistinct(root));

        List<Predicate> predicates = this.buildCommonPredicates(b, root, params);
        if (!predicates.isEmpty()) {
            q.where(predicates.toArray(Predicate[]::new));
        }

        return session.createQuery(q).getSingleResult();
    }

    private List<Predicate> buildCommonPredicates(CriteriaBuilder b, Root<Course> root,
            Map<String, String> params) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(b.isFalse(root.get("isDeleted")));

        if (params == null) {
            return predicates;
        }

        String keyword = params.containsKey("keyword") ? params.get("keyword") : params.get("kw");
        if (keyword != null && !keyword.isBlank()) {
            predicates.add(b.like(b.lower(root.get("name")),
                    String.format("%%%s%%", keyword.trim().toLowerCase())));
        }

        String lecturerId = params.get("lecturerId");
        if (lecturerId != null && !lecturerId.isBlank()) {
            predicates.add(b.equal(root.get("lecturerId").get("id"), Integer.parseInt(lecturerId)));
        }

        String subjectId = params.get("subjectId");
        if (subjectId != null && !subjectId.isBlank()) {
            predicates.add(b.equal(root.get("subjectId").get("id"), Integer.parseInt(subjectId)));
        }

        String isPaid = params.get("isPaid");
        if (isPaid != null && !isPaid.isBlank()) {
            predicates.add(b.equal(root.get("isPaid"), Boolean.parseBoolean(isPaid)));
        }

        return predicates;
    }
}

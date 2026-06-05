package com.paq.repository.impl;

import com.paq.pojo.Topic;
import com.paq.repository.TopicRepository;
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

@Repository
@PropertySource("classpath:configs.properties")
@Transactional
public class TopicRepositoryImpl implements TopicRepository {

    @Autowired
    private Environment env;

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<Topic> getTopics(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = session.getCriteriaBuilder();
        CriteriaQuery<Topic> q = b.createQuery(Topic.class);
        Root<Topic> root = q.from(Topic.class);
        q.select(root);
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(b.or(
                b.isFalse(root.get("isDeleted")),
                b.isNull(root.get("isDeleted"))));

        if (params != null) {
            String kw = params.get("kw");
            if (kw != null && !kw.isEmpty()) {
                predicates.add(b.like(root.get("name"), String.format("%%%s%%", kw)));
            }
        }

        q.where(predicates.toArray(Predicate[]::new));

        q.orderBy(b.desc(root.get("id")));

        Query<Topic> query = session.createQuery(q);

        if (params != null && params.containsKey("page")) {
            int pageSize = this.env.getProperty("topics.page_size", Integer.class);
            int page = Integer.parseInt(params.get("page"));
            int start = (page - 1) * pageSize;

            query.setMaxResults(pageSize);
            query.setFirstResult(start);
        }

        return query.getResultList();
    }

    @Override
    public long countTopics(Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = session.getCriteriaBuilder();
        CriteriaQuery<Long> q = b.createQuery(Long.class);
        Root<Topic> root = q.from(Topic.class);
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(b.or(
                b.isFalse(root.get("isDeleted")),
                b.isNull(root.get("isDeleted"))));

        if (params != null) {
            String kw = params.get("kw");
            if (kw != null && !kw.isEmpty()) {
                predicates.add(b.like(root.get("name"), String.format("%%%s%%", kw)));
            }
        }

        q.select(b.count(root.get("id")));
        q.where(predicates.toArray(Predicate[]::new));
        Long result = session.createQuery(q).getSingleResult();
        return result == null ? 0L : result;
    }

    @Override
    public Topic getTopicById(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        Topic topic = session.get(Topic.class, id);
        if (topic == null || (topic.getIsDeleted() != null && topic.getIsDeleted() == true)) {
            return null;
        }

        return topic;
    }

    @Override
    public Topic getTopicByName(String name) {
        try {
            Session session = this.factory.getObject().getCurrentSession();
            Query<Topic> q = session.createNamedQuery("Topic.findByName", Topic.class);
            q.setParameter("name", name);
            Topic topic = q.getSingleResult();
            if (topic.getIsDeleted() != null && topic.getIsDeleted() == true) {
                return null;
            }

            return topic;
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public Topic addOrUpdateTopic(Topic topic) {
        Session session = this.factory.getObject().getCurrentSession();
        if (topic.getId() != null) {
            return session.merge(topic);
        }
        session.persist(topic);
        return topic;
    }

    @Override
    public void deleteTopic(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        Topic topic = this.getTopicById(id);
        topic.setIsDeleted(Boolean.TRUE);
        session.merge(topic);
    }
}

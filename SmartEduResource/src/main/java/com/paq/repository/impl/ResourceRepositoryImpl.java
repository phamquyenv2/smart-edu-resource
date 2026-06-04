/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.repository.impl;

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

import com.paq.pojo.Resource;
import com.paq.pojo.ResourceRelation;
import com.paq.repository.ResourceRepository;
import com.paq.utils.constant.LevelEnum;

import jakarta.persistence.NoResultException;

/**
 *
 * @author Admin
 */
@Repository
@Transactional
@PropertySource("classpath:configs.properties")
public class ResourceRepositoryImpl implements ResourceRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Autowired
    private Environment env;

    @Override
    public List<Resource> getResources(Map<String, String> params) {
        Session s = this.factory.getObject().getCurrentSession();

        String hql = "SELECT DISTINCT r FROM Resource r";

        if (params != null) {
            if (params.containsKey("subjectId")) {
                hql += " JOIN r.subjectSet subject";
            }
            if (params.containsKey("topicId")) {
                hql += " JOIN r.topicSet topic";
            }
            if (params.containsKey("typeId")) {
                hql += " JOIN r.resourceTypeSet resourceType";
            }
        }

        hql += " WHERE r.isDeleted = false";

        if (params != null) {
            if (params.containsKey("keyword")) {
                hql += " AND r.title LIKE :kw";
            }
            if (params.containsKey("level")) {
                hql += " AND r.level = :level";
            }
            if (params.containsKey("subjectId")) {
                hql += " AND subject.id = :subjectId";
            }
            if (params.containsKey("topicId")) {
                hql += " AND topic.id = :topicId";
            }
            if (params.containsKey("typeId")) {
                hql += " AND resourceType.id = :typeId";
            }
            if (params.containsKey("uploaderId")) {
                hql += " AND r.uploadBy.id = :uploaderId";
            }
        }

        Query<Resource> q = s.createQuery(hql, Resource.class);

        if (params != null) {
            if (params.containsKey("keyword")) {
                q.setParameter("kw", "%" + params.get("keyword") + "%");
            }
            if (params.containsKey("level")) {
                q.setParameter("level", LevelEnum.valueOf(params.get("level")));
            }
            if (params.containsKey("subjectId")) {
                q.setParameter("subjectId", Integer.parseInt(params.get("subjectId")));
            }
            if (params.containsKey("topicId")) {
                q.setParameter("topicId", Integer.parseInt(params.get("topicId")));
            }
            if (params.containsKey("typeId")) {
                q.setParameter("typeId", Integer.parseInt(params.get("typeId")));
            }
            if (params.containsKey("uploaderId")) {
                q.setParameter("uploaderId", Integer.parseInt(params.get("uploaderId")));
            }
        }

        int pageSize = this.env.getProperty("resources.page_size", Integer.class);
        int page = 1;
        if (params != null && params.containsKey("page")) {
            page = Integer.parseInt(params.get("page"));
        }
        q.setMaxResults(pageSize);
        q.setFirstResult((page - 1) * pageSize);

        return q.getResultList();
    }

    @Override
    public Long countResources(Map<String, String> params) {
        Session s = this.factory.getObject().getCurrentSession();

        String hql = "SELECT COUNT(DISTINCT r) FROM Resource r";

        if (params != null) {
            if (params.containsKey("subjectId")) {
                hql += " JOIN r.subjectSet subject";
            }
            if (params.containsKey("topicId")) {
                hql += " JOIN r.topicSet topic";
            }
            if (params.containsKey("typeId")) {
                hql += " JOIN r.resourceTypeSet resourceType";
            }
        }

        hql += " WHERE r.isDeleted = false";

        if (params != null) {
            if (params.containsKey("keyword")) {
                hql += " AND r.title LIKE :kw";
            }
            if (params.containsKey("level")) {
                hql += " AND r.level = :level";
            }
            if (params.containsKey("subjectId")) {
                hql += " AND subject.id = :subjectId";
            }
            if (params.containsKey("topicId")) {
                hql += " AND topic.id = :topicId";
            }
            if (params.containsKey("typeId")) {
                hql += " AND resourceType.id = :typeId";
            }
            if (params.containsKey("uploaderId")) {
                hql += " AND r.uploadBy.id = :uploaderId";
            }
        }

        Query<Long> q = s.createQuery(hql, Long.class);

        if (params != null) {
            if (params.containsKey("keyword")) {
                q.setParameter("kw", "%" + params.get("keyword") + "%");
            }
            if (params.containsKey("level")) {
                q.setParameter("level", LevelEnum.valueOf(params.get("level")));
            }
            if (params.containsKey("subjectId")) {
                q.setParameter("subjectId", Integer.parseInt(params.get("subjectId")));
            }
            if (params.containsKey("topicId")) {
                q.setParameter("topicId", Integer.parseInt(params.get("topicId")));
            }
            if (params.containsKey("typeId")) {
                q.setParameter("typeId", Integer.parseInt(params.get("typeId")));
            }
            if (params.containsKey("uploaderId")) {
                q.setParameter("uploaderId", Integer.parseInt(params.get("uploaderId")));
            }
        }

        return q.getSingleResult();
    }

    @Override
    public Resource getResourceById(int id) {
        Session s = this.factory.getObject().getCurrentSession();
        return s.get(Resource.class, id);
    }

    @Override
    public Resource getResourceByTitle(String title) {
        Session s = this.factory.getObject().getCurrentSession();

        Query<Resource> q = s.createQuery(
                "FROM Resource r WHERE r.title = :title AND r.isDeleted = false",
                Resource.class
        );
        q.setParameter("title", title);

        try {
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public Resource addOrUpdateResource(Resource resource) {
        Session s = this.factory.getObject().getCurrentSession();
        return s.merge(resource);
    }

    @Override
    public void deleteResource(int id) {
        Session s = this.factory.getObject().getCurrentSession();
        Resource resource = s.get(Resource.class, id);
        if (resource != null) {
            resource.setIsDeleted(Boolean.TRUE);
            s.merge(resource);
        }
    }

    @Override
    public List<ResourceRelation> getRelationsBySourceId(int sourceId) {
        Session s = this.factory.getObject().getCurrentSession();

        Query<ResourceRelation> q = s.createQuery(
                "FROM ResourceRelation r WHERE r.sourceId.id = :sourceId",
                ResourceRelation.class
        );
        q.setParameter("sourceId", sourceId);

        return q.getResultList();
    }

    @Override
    public void replaceRelations(Resource source, List<Resource> relatedResources) {
        Session s = this.factory.getObject().getCurrentSession();

        Query<?> deleteQ = s.createQuery(
                "DELETE FROM ResourceRelation r WHERE r.sourceId.id = :sourceId"
        );
        deleteQ.setParameter("sourceId", source.getId());
        deleteQ.executeUpdate();

        // Thêm relations mới
        for (Resource related : relatedResources) {
            ResourceRelation relation = new ResourceRelation();
            relation.setSourceId(source);
            relation.setRelatedId(related);
            s.persist(relation);
        }
    }

    @Override
    public List<Resource> getRelatedResources(int resourceId) {
        Session s = this.factory.getObject().getCurrentSession();

        Query<Resource> q = s.createQuery(
                "SELECT r.relatedId FROM ResourceRelation r WHERE r.sourceId.id = :id AND r.relatedId.isDeleted = false",
                Resource.class
        );

        q.setParameter("id", resourceId);

        q.setMaxResults(5);

        return q.getResultList();
    }

    @Override
    public List<Resource> getSuggestedResources(int resourceId, int limit) {
        Session s = this.factory.getObject().getCurrentSession();

        String sql = "SELECT candidate.* "
                + "FROM resource candidate "
                + "WHERE candidate.id <> :resourceId "
                + "AND candidate.is_deleted = false "
                + "AND NOT EXISTS ("
                + "SELECT 1 FROM resource_relation relation "
                + "WHERE relation.source_id = :resourceId "
                + "AND relation.related_id = candidate.id"
                + ") "
                + "AND ("
                + "EXISTS ("
                + "SELECT 1 FROM resource_topic candidate_topic "
                + "JOIN resource_topic source_topic ON source_topic.topic_id = candidate_topic.topic_id "
                + "WHERE candidate_topic.resource_id = candidate.id "
                + "AND source_topic.resource_id = :resourceId"
                + ") "
                + "OR EXISTS ("
                + "SELECT 1 FROM resource_tag_map candidate_tag "
                + "JOIN resource_tag_map source_tag ON source_tag.tag_id = candidate_tag.tag_id "
                + "WHERE candidate_tag.resource_id = candidate.id "
                + "AND source_tag.resource_id = :resourceId"
                + ") "
                + "OR EXISTS ("
                + "SELECT 1 FROM resource_subject candidate_subject "
                + "JOIN resource_subject source_subject ON source_subject.subject_id = candidate_subject.subject_id "
                + "WHERE candidate_subject.resource_id = candidate.id "
                + "AND source_subject.resource_id = :resourceId"
                + ")"
                + ") "
                + "ORDER BY ("
                + "3 * ("
                + "SELECT COUNT(*) FROM resource_topic candidate_topic "
                + "JOIN resource_topic source_topic ON source_topic.topic_id = candidate_topic.topic_id "
                + "WHERE candidate_topic.resource_id = candidate.id "
                + "AND source_topic.resource_id = :resourceId"
                + ") "
                + "+ 2 * ("
                + "SELECT COUNT(*) FROM resource_tag_map candidate_tag "
                + "JOIN resource_tag_map source_tag ON source_tag.tag_id = candidate_tag.tag_id "
                + "WHERE candidate_tag.resource_id = candidate.id "
                + "AND source_tag.resource_id = :resourceId"
                + ") "
                + "+ ("
                + "SELECT COUNT(*) FROM resource_subject candidate_subject "
                + "JOIN resource_subject source_subject ON source_subject.subject_id = candidate_subject.subject_id "
                + "WHERE candidate_subject.resource_id = candidate.id "
                + "AND source_subject.resource_id = :resourceId"
                + ")"
                + ") DESC, candidate.created_at DESC, candidate.id DESC";

        Query<Resource> q = s.createNativeQuery(sql, Resource.class);
        q.setParameter("resourceId", resourceId);
        q.setMaxResults(limit);

        return q.getResultList();
    }

}

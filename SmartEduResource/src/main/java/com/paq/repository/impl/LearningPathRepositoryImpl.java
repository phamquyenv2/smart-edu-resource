package com.paq.repository.impl;

import com.paq.pojo.LearningPath;
import com.paq.pojo.LearningPathItem;
import com.paq.repository.LearningPathRepository;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class LearningPathRepositoryImpl implements LearningPathRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<LearningPath> getLearningPathsByStudentId(int studentId) {
        Session s = this.factory.getObject().getCurrentSession();
        Query<LearningPath> q = s.createQuery(
                "FROM LearningPath lp WHERE lp.studentId.id = :studentId "
                + "AND (lp.isDeleted IS NULL OR lp.isDeleted = false) "
                + "ORDER BY lp.createdAt DESC",
                LearningPath.class
        );
        q.setParameter("studentId", studentId);
        return q.getResultList();
    }

    @Override
    public LearningPath getLearningPathById(int id) {
        Session s = this.factory.getObject().getCurrentSession();
        return s.get(LearningPath.class, id);
    }

    @Override
    public LearningPath addOrUpdateLearningPath(LearningPath learningPath) {
        Session s = this.factory.getObject().getCurrentSession();
        if (learningPath.getId() != null) {
            return s.merge(learningPath);
        }
        s.persist(learningPath);
        return learningPath;
    }

    @Override
    public void deleteLearningPath(int id) {
        Session s = this.factory.getObject().getCurrentSession();
        LearningPath lp = s.get(LearningPath.class, id);
        if (lp != null) {
            lp.setIsDeleted(true);
            s.merge(lp);
        }
    }

    @Override
    public List<LearningPathItem> getItemsByPathId(int pathId) {
        Session s = this.factory.getObject().getCurrentSession();
        Query<LearningPathItem> q = s.createQuery(
                "FROM LearningPathItem lpi WHERE lpi.pathId.id = :pathId "
                + "AND (lpi.isDeleted IS NULL OR lpi.isDeleted = false) "
                + "ORDER BY lpi.orderNumber ASC",
                LearningPathItem.class
        );
        q.setParameter("pathId", pathId);
        return q.getResultList();
    }

    @Override
    public LearningPathItem addOrUpdateItem(LearningPathItem item) {
        Session s = this.factory.getObject().getCurrentSession();
        if (item.getId() != null) {
            return s.merge(item);
        }
        s.persist(item);
        return item;
    }

    @Override
    public void deleteItem(int id) {
        Session s = this.factory.getObject().getCurrentSession();
        LearningPathItem item = s.get(LearningPathItem.class, id);
        if (item != null) {
            item.setIsDeleted(true);
            s.merge(item);
        }
    }

    @Override
    public LearningPathItem getItemById(int id) {
        Session s = this.factory.getObject().getCurrentSession();
        return s.get(LearningPathItem.class, id);
    }

    @Override
    public void deleteItemsByPathId(int pathId) {
        Session s = this.factory.getObject().getCurrentSession();
        Query q = s.createQuery(
                "UPDATE LearningPathItem lpi SET lpi.isDeleted = true "
                + "WHERE lpi.pathId.id = :pathId"
        );
        q.setParameter("pathId", pathId);
        q.executeUpdate();
    }
}

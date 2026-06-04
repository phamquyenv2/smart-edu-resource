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

import com.paq.pojo.ChatParticipant;
import com.paq.repository.ChatParticipantRepository;
import com.paq.utils.constant.RoleEnum;

import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Repository
@PropertySource("classpath:configs.properties")
@Transactional
public class ChatParticipantRepositoryImpl implements ChatParticipantRepository {

    @Autowired
    private Environment env;

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<ChatParticipant> getParticipantsByRoomId(int roomId, Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = session.getCriteriaBuilder();
        CriteriaQuery<ChatParticipant> q = b.createQuery(ChatParticipant.class);
        Root<ChatParticipant> root = q.from(ChatParticipant.class);
        root.fetch("roomId", JoinType.INNER);
        root.fetch("userId", JoinType.INNER);

        Predicate predicate = b.equal(root.get("roomId").get("id"), roomId);

        if (params != null && params.containsKey("kw") && !params.get("kw").isEmpty()) {
            String kw = params.get("kw").toLowerCase();
            Predicate namePredicate = b.like(b.lower(root.get("userId").get("fullName")), "%" + kw + "%");
            Predicate usernamePredicate = b.like(b.lower(root.get("userId").get("username")), "%" + kw + "%");
            predicate = b.and(predicate, b.or(namePredicate, usernamePredicate));
        }

        q.select(root).distinct(true)
                .where(predicate)
                .orderBy(b.asc(root.get("joinedAt")), b.asc(root.get("id")));

        Query<ChatParticipant> query = session.createQuery(q);
        if (params != null && params.containsKey("page")) {
            int pageSize = this.env.getProperty("chat_participants.page_size", Integer.class, 10);
            int page = Integer.parseInt(params.getOrDefault("page", "1"));
            query.setMaxResults(pageSize);
            query.setFirstResult((page - 1) * pageSize);
        }

        return query.getResultList();
    }

    @Override
    public ChatParticipant getParticipantById(int id) {
        try {
            Session session = this.factory.getObject().getCurrentSession();
            Query<ChatParticipant> q = session.createQuery(
                    "SELECT p FROM ChatParticipant p "
                    + "JOIN FETCH p.roomId r "
                    + "JOIN FETCH p.userId "
                    + "WHERE p.id = :id",
                    ChatParticipant.class);
            q.setParameter("id", id);
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public ChatParticipant getParticipantByRoomIdAndUserId(int roomId, int userId) {
        try {
            Session session = this.factory.getObject().getCurrentSession();
            Query<ChatParticipant> q = session.createQuery(
                    "SELECT p FROM ChatParticipant p "
                    + "WHERE p.roomId.id = :roomId AND p.userId.id = :userId",
                    ChatParticipant.class);
            q.setParameter("roomId", roomId);
            q.setParameter("userId", userId);
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public Long countParticipantsByRoomId(int roomId, Map<String, String> params) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = session.getCriteriaBuilder();
        CriteriaQuery<Long> q = b.createQuery(Long.class);
        Root<ChatParticipant> root = q.from(ChatParticipant.class);

        jakarta.persistence.criteria.Predicate predicate = b.equal(root.get("roomId").get("id"), roomId);

        if (params != null && params.containsKey("kw") && !params.get("kw").isEmpty()) {
            String kw = params.get("kw").toLowerCase();
            jakarta.persistence.criteria.Predicate namePredicate = b.like(b.lower(root.get("userId").get("fullName")), "%" + kw + "%");
            jakarta.persistence.criteria.Predicate usernamePredicate = b.like(b.lower(root.get("userId").get("username")), "%" + kw + "%");
            predicate = b.and(predicate, b.or(namePredicate, usernamePredicate));
        }

        q.select(b.count(root)).where(predicate);
        return session.createQuery(q).getSingleResult();
    }

    @Override
    public Long countStudentParticipantsByRoomId(int roomId) {
        Session session = this.factory.getObject().getCurrentSession();
        Query<Long> q = session.createQuery(
                "SELECT COUNT(p) FROM ChatParticipant p "
                + "WHERE p.roomId.id = :roomId AND p.userId.role = :role",
                Long.class);
        q.setParameter("roomId", roomId);
        q.setParameter("role", RoleEnum.STUDENT);
        return q.getSingleResult();
    }

    @Override
    public ChatParticipant addParticipant(ChatParticipant participant) {
        Session session = this.factory.getObject().getCurrentSession();
        session.persist(participant);
        return participant;
    }

    @Override
    public void deleteParticipant(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        ChatParticipant participant = session.get(ChatParticipant.class, id);
        session.remove(participant);
    }
}

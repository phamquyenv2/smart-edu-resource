/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.repository.impl;

import com.paq.pojo.ChatMessage;
import com.paq.pojo.ChatRoom;
import com.paq.repository.ChatMessageRepository;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
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
public class ChatMessageRepositoryImpl implements ChatMessageRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Autowired
    private Environment env;

    @Override
    public List<ChatMessage> getMessagesByRoomId(int roomId, Map<String, String> params) {
        Session s = this.factory.getObject().getCurrentSession();

        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<ChatMessage> q = b.createQuery(ChatMessage.class);
        Root<ChatMessage> root = q.from(ChatMessage.class);

        root.fetch("senderId", JoinType.INNER);
        root.fetch("roomId", JoinType.INNER);

        Join<ChatMessage, ChatRoom> room = root.join("roomId", JoinType.INNER);
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(b.equal(room.get("id"), roomId));
        predicates.add(b.or(
                b.isFalse(root.get("isDeleted")),
                b.isNull(root.get("isDeleted"))));

        q.select(root)
                .distinct(true)
                .where(predicates.toArray(Predicate[]::new))
                .orderBy(b.asc(root.get("sentAt")), b.asc(root.get("id")));

        Query<ChatMessage> query = s.createQuery(q);

        if (params != null) {
            int pageSize = this.env.getProperty("chat_messages.page_size", Integer.class, 20);
            int page = Integer.parseInt(params.getOrDefault("page", "1"));
            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);
        }

        return query.getResultList();
    }

    @Override
    public ChatMessage getMessageById(int id) {
        try {
            Session s = this.factory.getObject().getCurrentSession();

            Query<ChatMessage> q = s.createQuery(
                    "SELECT m FROM ChatMessage m "
                    + "JOIN FETCH m.senderId u "
                    + "JOIN FETCH m.roomId r "
                    + "WHERE m.id = :id "
                    + "AND (m.isDeleted = false OR m.isDeleted IS NULL)",
                    ChatMessage.class
            );

            q.setParameter("id", id);

            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public ChatMessage addOrUpdateMessage(ChatMessage message) {
        Session s = this.factory.getObject().getCurrentSession();

        if (message.getId() != null) {
            return s.merge(message);
        }

        s.persist(message);
        return message;
    }

    @Override
    public void deleteMessage(int id) {
        Session s = this.factory.getObject().getCurrentSession();

        ChatMessage message = this.getMessageById(id);
        if (message != null) {
            message.setIsDeleted(Boolean.TRUE);
            s.merge(message);
        }
    }

}

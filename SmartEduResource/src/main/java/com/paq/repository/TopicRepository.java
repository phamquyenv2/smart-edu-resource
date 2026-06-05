package com.paq.repository;

import java.util.List;
import java.util.Map;

import com.paq.pojo.Topic;

public interface TopicRepository {

    List<Topic> getTopics(Map<String, String> params);

    long countTopics(Map<String, String> params);

    Topic getTopicById(int id);

    Topic getTopicByName(String name);

    Topic addOrUpdateTopic(Topic topic);

    void deleteTopic(int id);
}

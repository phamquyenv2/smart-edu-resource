package com.paq.service;

import com.paq.pojo.request.ReqCategoryDTO;
import com.paq.pojo.response.ResCategoryDTO;
import java.util.List;
import java.util.Map;

public interface TopicService {

    List<ResCategoryDTO> getTopics(Map<String, String> params);

    long countTopics(Map<String, String> params);

    ResCategoryDTO getTopicById(int id);

    ResCategoryDTO createTopic(ReqCategoryDTO request);

    ResCategoryDTO updateTopic(int id, ReqCategoryDTO request);

    void deleteTopic(int id);
}

package com.paq.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paq.pojo.Topic;
import com.paq.pojo.request.ReqCategoryDTO;
import com.paq.pojo.response.ResCategoryDTO;
import com.paq.repository.TopicRepository;
import com.paq.service.TopicService;
import com.paq.utils.DTOMapper;
import com.paq.utils.error.IdInvalidException;

@Service
public class TopicServiceImpl implements TopicService {

    @Autowired
    private TopicRepository topicRepo;

    @Override
    public List<ResCategoryDTO> getTopics(Map<String, String> params) {
        return this.topicRepo.getTopics(params).stream()
                .map(DTOMapper::toResCategoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long countTopics(Map<String, String> params) {
        return this.topicRepo.countTopics(params);
    }

    @Override
    public ResCategoryDTO getTopicById(int id) {
        Topic topic = this.topicRepo.getTopicById(id);
        if (topic == null || Boolean.TRUE.equals(topic.getIsDeleted())) {
            throw new IdInvalidException("Topic không tồn tại");
        }

        return DTOMapper.toResCategoryDTO(topic);
    }

    @Override
    public ResCategoryDTO createTopic(ReqCategoryDTO request) {
        Topic topic = new Topic();
        topic.setName(request.getName());
        topic.setIsDeleted(Boolean.FALSE);

        return DTOMapper.toResCategoryDTO(this.topicRepo.addOrUpdateTopic(topic));
    }

    @Override
    public ResCategoryDTO updateTopic(int id, ReqCategoryDTO request) {
        Topic topic = this.topicRepo.getTopicById(id);
        if (topic == null || Boolean.TRUE.equals(topic.getIsDeleted())) {
            throw new IdInvalidException("Topic không tồn tại");
        }

        topic.setName(request.getName());

        return DTOMapper.toResCategoryDTO(this.topicRepo.addOrUpdateTopic(topic));
    }

    @Override
    public void deleteTopic(int id) {
        Topic topic = this.topicRepo.getTopicById(id);
        if (topic == null || Boolean.TRUE.equals(topic.getIsDeleted())) {
            throw new IdInvalidException("Topic không tồn tại");
        }

        this.topicRepo.deleteTopic(id);
    }
}

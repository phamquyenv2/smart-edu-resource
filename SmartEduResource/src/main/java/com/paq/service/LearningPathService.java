package com.paq.service;

import com.paq.pojo.request.ReqGenerateLearningPathDTO;
import com.paq.pojo.request.ReqLearningPathDTO;
import com.paq.pojo.response.ResLearningPathDTO;
import java.util.List;

public interface LearningPathService {

    List<ResLearningPathDTO> getMyLearningPaths(String username);

    ResLearningPathDTO getLearningPathDetail(String username, int pathId);

    ResLearningPathDTO generateLearningPath(String username, ReqGenerateLearningPathDTO dto);

    ResLearningPathDTO updateLearningPath(String username, int pathId, ReqLearningPathDTO dto);

    void deleteLearningPath(String username, int pathId);

    void deleteItem(String username, int itemId);
}

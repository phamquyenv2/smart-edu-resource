package com.paq.repository;

import com.paq.pojo.LearningPath;
import com.paq.pojo.LearningPathItem;
import java.util.List;

public interface LearningPathRepository {

    List<LearningPath> getLearningPathsByStudentId(int studentId);

    LearningPath getLearningPathById(int id);

    LearningPath addOrUpdateLearningPath(LearningPath learningPath);

    void deleteLearningPath(int id);

    List<LearningPathItem> getItemsByPathId(int pathId);

    LearningPathItem addOrUpdateItem(LearningPathItem item);

    void deleteItem(int id);

    LearningPathItem getItemById(int id);

    void deleteItemsByPathId(int pathId);
}

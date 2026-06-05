package com.paq.pojo.response;

import java.util.Date;
import java.util.List;

public class ResLearningPathDTO {

    private Integer id;
    private String title;
    private String description;
    private Boolean generatedByAi;
    private String goal;
    private Date createdAt;
    private Date updatedAt;
    private Integer studentId;
    private String studentName;
    private String recommendedByName;
    private List<ResLearningPathItemDTO> items;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getGeneratedByAi() {
        return generatedByAi;
    }

    public void setGeneratedByAi(Boolean generatedByAi) {
        this.generatedByAi = generatedByAi;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRecommendedByName() {
        return recommendedByName;
    }

    public void setRecommendedByName(String recommendedByName) {
        this.recommendedByName = recommendedByName;
    }

    public List<ResLearningPathItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ResLearningPathItemDTO> items) {
        this.items = items;
    }
}

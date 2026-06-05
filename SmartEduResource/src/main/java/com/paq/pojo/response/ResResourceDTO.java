/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.pojo.response;

/**
 *
 * @author Admin
 */
import java.util.Date;
import java.util.List;

public class ResResourceDTO {

    private Integer id;
    private String title;
    private String description;
    private String fileUrl;
    private String thumbnailUrl;
    private String format;
    private Integer fileSize;
    private String level;
    private Date createdAt;
    private Date updateAt;
    private Integer pageCount;
    private Boolean isDeleted;
    private ResUserDTO uploadBy;
    private List<ResSubjectDTO> subjects;
    private List<ResCategoryDTO> topics;
    private List<ResCategoryDTO> tags;
    private List<ResCategoryDTO> types;
    private List<ResResourceDTO> relatedResources;
    private List<ResCourseDTO> paidCourses;
    private Boolean hasFreePath;

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

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public ResUserDTO getUploadBy() {
        return uploadBy;
    }

    public void setUploadBy(ResUserDTO uploadBy) {
        this.uploadBy = uploadBy;
    }

    public List<ResSubjectDTO> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<ResSubjectDTO> subjects) {
        this.subjects = subjects;
    }

    public List<ResCategoryDTO> getTopics() {
        return topics;
    }

    public void setTopics(List<ResCategoryDTO> topics) {
        this.topics = topics;
    }

    public List<ResCategoryDTO> getTags() {
        return tags;
    }

    public void setTags(List<ResCategoryDTO> tags) {
        this.tags = tags;
    }

    public List<ResCategoryDTO> getTypes() {
        return types;
    }

    public void setTypes(List<ResCategoryDTO> types) {
        this.types = types;
    }

    public List<ResResourceDTO> getRelatedResources() {
        return relatedResources;
    }

    public void setRelatedResources(List<ResResourceDTO> relatedResources) {
        this.relatedResources = relatedResources;
    }

    public List<ResCourseDTO> getPaidCourses() {
        return paidCourses;
    }

    public void setPaidCourses(List<ResCourseDTO> paidCourses) {
        this.paidCourses = paidCourses;
    }

    public Boolean getHasFreePath() {
        return hasFreePath;
    }

    public void setHasFreePath(Boolean hasFreePath) {
        this.hasFreePath = hasFreePath;
    }
}

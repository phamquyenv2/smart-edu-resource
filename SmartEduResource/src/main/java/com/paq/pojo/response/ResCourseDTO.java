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

public class ResCourseDTO {

    private Integer id;
    private String name;
    private String description;
    private Date startDate;
    private Date endDate;
    private Boolean isPaid;
    private Long price;
    private Boolean isDeleted;
    private String targetLevel;
    private ResUserDTO createdBy;
    private Integer lecturerId;
    private ResUserDTO lecturerUser;
    private Integer subjectId;
    private ResSubjectDTO subject;
    private Integer enrollmentCount;
    private List<ResCourseChapterDTO> chapters;
    private Integer totalLessons;
    private Integer totalChapters;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Boolean getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(Boolean isPaid) {
        this.isPaid = isPaid;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(String targetLevel) {
        this.targetLevel = targetLevel;
    }

    public ResUserDTO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(ResUserDTO createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(Integer lecturerId) {
        this.lecturerId = lecturerId;
    }

    public ResUserDTO getLecturerUser() {
        return lecturerUser;
    }

    public void setLecturerUser(ResUserDTO lecturerUser) {
        this.lecturerUser = lecturerUser;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public ResSubjectDTO getSubject() {
        return subject;
    }

    public void setSubject(ResSubjectDTO subject) {
        this.subject = subject;
    }

    public Integer getEnrollmentCount() {
        return enrollmentCount;
    }

    public void setEnrollmentCount(Integer enrollmentCount) {
        this.enrollmentCount = enrollmentCount;
    }

    public List<ResCourseChapterDTO> getChapters() {
        return chapters;
    }

    public void setChapters(List<ResCourseChapterDTO> chapters) {
        this.chapters = chapters;
    }

    public Integer getTotalLessons() {
        return totalLessons;
    }

    public void setTotalLessons(Integer totalLessons) {
        this.totalLessons = totalLessons;
    }

    public Integer getTotalChapters() {
        return totalChapters;
    }

    public void setTotalChapters(Integer totalChapters) {
        this.totalChapters = totalChapters;
    }
}

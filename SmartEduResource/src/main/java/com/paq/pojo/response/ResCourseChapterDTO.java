package com.paq.pojo.response;

import java.util.List;

public class ResCourseChapterDTO {

    private Integer chapterNum;
    private String chapterTitle;
    private List<ResCourseLessonDTO> lessons;

    public ResCourseChapterDTO() {
    }

    public ResCourseChapterDTO(Integer chapterNum, String chapterTitle, List<ResCourseLessonDTO> lessons) {
        this.chapterNum = chapterNum;
        this.chapterTitle = chapterTitle;
        this.lessons = lessons;
    }

    public Integer getChapterNum() { return chapterNum; }
    public void setChapterNum(Integer chapterNum) { this.chapterNum = chapterNum; }

    public String getChapterTitle() { return chapterTitle; }
    public void setChapterTitle(String chapterTitle) { this.chapterTitle = chapterTitle; }

    public List<ResCourseLessonDTO> getLessons() { return lessons; }
    public void setLessons(List<ResCourseLessonDTO> lessons) { this.lessons = lessons; }
}

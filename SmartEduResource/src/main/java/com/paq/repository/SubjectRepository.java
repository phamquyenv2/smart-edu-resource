package com.paq.repository;

import java.util.List;
import java.util.Map;

import com.paq.pojo.Subject;

public interface SubjectRepository {

    List<Subject> getSubjects(Map<String, String> params);

    long countSubjects(Map<String, String> params);

    Subject getSubjectById(int id);

    Subject getSubjectByName(String name);

    Subject getSubjectByCode(String code);

    Subject addOrUpdateSubject(Subject subject);

    void deleteSubject(int id);
}

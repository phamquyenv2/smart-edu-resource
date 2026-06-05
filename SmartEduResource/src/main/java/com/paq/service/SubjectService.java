package com.paq.service;

import com.paq.pojo.request.ReqSubjectDTO;
import com.paq.pojo.response.ResSubjectDTO;
import java.util.List;
import java.util.Map;

public interface SubjectService {

    List<ResSubjectDTO> getSubjects(Map<String, String> params);

    long countSubjects(Map<String, String> params);

    ResSubjectDTO getSubjectById(int id);

    ResSubjectDTO createSubject(ReqSubjectDTO request);

    ResSubjectDTO updateSubject(int id, ReqSubjectDTO request);

    void deleteSubject(int id);
}

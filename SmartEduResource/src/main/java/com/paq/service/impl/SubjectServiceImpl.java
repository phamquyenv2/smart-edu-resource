package com.paq.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paq.pojo.Subject;
import com.paq.pojo.request.ReqSubjectDTO;
import com.paq.pojo.response.ResSubjectDTO;
import com.paq.repository.SubjectRepository;
import com.paq.service.SubjectService;
import com.paq.utils.DTOMapper;
import com.paq.utils.error.IdInvalidException;

@Service
public class SubjectServiceImpl implements SubjectService {

    @Autowired
    private SubjectRepository subjectRepo;

    @Override
    public List<ResSubjectDTO> getSubjects(Map<String, String> params) {
        return this.subjectRepo.getSubjects(params).stream()
                .map(DTOMapper::toResSubjectDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long countSubjects(Map<String, String> params) {
        return this.subjectRepo.countSubjects(params);
    }

    @Override
    public ResSubjectDTO getSubjectById(int id) {
        Subject subject = this.subjectRepo.getSubjectById(id);
        if (subject == null || Boolean.TRUE.equals(subject.getIsDeleted())) {
            throw new IdInvalidException("Subject không tồn tại");
        }

        return DTOMapper.toResSubjectDTO(subject);
    }

    @Override
    public ResSubjectDTO createSubject(ReqSubjectDTO request) {
        Subject subject = new Subject();
        subject.setCode(request.getCode());
        subject.setName(request.getName());
        subject.setDescription(request.getDescription());
        subject.setCreatedAt(new Date());
        subject.setIsDeleted(Boolean.FALSE);

        return DTOMapper.toResSubjectDTO(this.subjectRepo.addOrUpdateSubject(subject));
    }

    @Override
    public ResSubjectDTO updateSubject(int id, ReqSubjectDTO request) {
        Subject subject = this.subjectRepo.getSubjectById(id);
        if (subject == null || Boolean.TRUE.equals(subject.getIsDeleted())) {
            throw new IdInvalidException("Subject không tồn tại");
        }

        subject.setCode(request.getCode());
        subject.setName(request.getName());
        subject.setDescription(request.getDescription());

        return DTOMapper.toResSubjectDTO(this.subjectRepo.addOrUpdateSubject(subject));
    }

    @Override
    public void deleteSubject(int id) {
        Subject subject = this.subjectRepo.getSubjectById(id);
        if (subject == null || Boolean.TRUE.equals(subject.getIsDeleted())) {
            throw new IdInvalidException("Subject không tồn tại");
        }

        this.subjectRepo.deleteSubject(id);
    }
}

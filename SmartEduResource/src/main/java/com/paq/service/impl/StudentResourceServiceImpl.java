/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.service.impl;

import com.paq.pojo.Resource;
import com.paq.pojo.response.ResResourceDTO;
import com.paq.repository.ResourceRepository;
import com.paq.service.StudentResourceService;
import com.paq.utils.DTOMapper;
import com.paq.utils.error.IdInvalidException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Admin
 */
@Service
public class StudentResourceServiceImpl implements StudentResourceService {

    @Autowired
    private ResourceRepository resourceRepo;

    @Override
    public List<ResResourceDTO> getResources(Map<String, String> params) {
        return this.resourceRepo.getResources(params)
                .stream()
                .map(r -> DTOMapper.toResourceDTO(r))
                .collect(Collectors.toList());
    }

    @Override
    public ResResourceDTO getResourceById(int id) {
        Resource r = this.resourceRepo.getResourceById(id);

        if (r == null || Boolean.TRUE.equals(r.getIsDeleted())) {
            throw new IdInvalidException("Resource khong ton tai");
        }

        return DTOMapper.toResourceDTO(r);
    }

    @Override
    public List<ResResourceDTO> getRelatedResources(int resourceId) {
        return this.resourceRepo.getRelatedResources(resourceId)
                .stream()
                .map(r -> DTOMapper.toResourceDTO(r))
                .collect(Collectors.toList());
    }

}

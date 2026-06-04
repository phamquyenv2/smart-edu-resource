/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paq.pojo.Resource;
import com.paq.pojo.response.ResResourceDTO;
import com.paq.repository.ResourceRepository;
import com.paq.service.StudentResourceService;
import com.paq.utils.DTOMapper;
import com.paq.utils.error.IdInvalidException;

/**
 *
 * @author Admin
 */
@Service
@Transactional
public class StudentResourceServiceImpl implements StudentResourceService {

    private static final int SUGGESTED_RESOURCE_LIMIT = 6;

    @Autowired
    private ResourceRepository resourceRepo;

    @Override
    public List<ResResourceDTO> getResources(Map<String, String> params) {
        return this.resourceRepo.getResources(params)
                .stream()
                .map(DTOMapper::toPublicResResourceDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Long countResources(Map<String, String> params) {
        return this.resourceRepo.countResources(params);
    }

    @Override
    public ResResourceDTO getResourceById(int id) {
        Resource r = this.resourceRepo.getResourceById(id);

        if (r == null || Boolean.TRUE.equals(r.getIsDeleted())) {
            throw new IdInvalidException("Resource không tồn tại");
        }

        return DTOMapper.toPublicResResourceDTO(r);
    }

    @Override
    public List<ResResourceDTO> getRelatedResources(int resourceId) {
        Resource resource = this.resourceRepo.getResourceById(resourceId);
        if (resource == null || Boolean.TRUE.equals(resource.getIsDeleted())) {
            throw new IdInvalidException("Resource không tồn tại");
        }

        Map<Integer, Resource> resources = new LinkedHashMap<>();

        this.resourceRepo.getRelatedResources(resourceId)
                .forEach(related -> resources.put(related.getId(), related));

        this.resourceRepo.getSuggestedResources(resourceId, SUGGESTED_RESOURCE_LIMIT)
                .forEach(suggested -> resources.putIfAbsent(suggested.getId(), suggested));

        return new ArrayList<>(resources.values())
                .stream()
                .map(DTOMapper::toPublicResResourceDTO)
                .collect(Collectors.toList());
    }

}

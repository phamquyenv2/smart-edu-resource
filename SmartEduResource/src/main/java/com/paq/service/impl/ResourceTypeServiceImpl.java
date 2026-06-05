package com.paq.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paq.pojo.ResourceType;
import com.paq.pojo.request.ReqCategoryDTO;
import com.paq.pojo.response.ResCategoryDTO;
import com.paq.repository.ResourceTypeRepository;
import com.paq.service.ResourceTypeService;
import com.paq.utils.DTOMapper;
import com.paq.utils.error.IdInvalidException;

@Service
public class ResourceTypeServiceImpl implements ResourceTypeService {

    @Autowired
    private ResourceTypeRepository resourceTypeRepo;

    @Override
    public List<ResCategoryDTO> getResourceTypes(Map<String, String> params) {
        return this.resourceTypeRepo.getResourceTypes(params).stream()
                .map(DTOMapper::toResCategoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long countResourceTypes(Map<String, String> params) {
        return this.resourceTypeRepo.countResourceTypes(params);
    }

    @Override
    public ResCategoryDTO getResourceTypeById(int id) {
        ResourceType resourceType = this.resourceTypeRepo.getResourceTypeById(id);
        if (resourceType == null || Boolean.TRUE.equals(resourceType.getIsDeleted())) {
            throw new IdInvalidException("Resource type không tồn tại");
        }

        return DTOMapper.toResCategoryDTO(resourceType);
    }

    @Override
    public ResCategoryDTO createResourceType(ReqCategoryDTO request) {
        ResourceType resourceType = new ResourceType();
        resourceType.setName(request.getName());
        resourceType.setIsDeleted(Boolean.FALSE);

        return DTOMapper.toResCategoryDTO(this.resourceTypeRepo.addOrUpdateResourceType(resourceType));
    }

    @Override
    public ResCategoryDTO updateResourceType(int id, ReqCategoryDTO request) {
        ResourceType resourceType = this.resourceTypeRepo.getResourceTypeById(id);
        if (resourceType == null || Boolean.TRUE.equals(resourceType.getIsDeleted())) {
            throw new IdInvalidException("Resource type không tồn tại");
        }

        resourceType.setName(request.getName());

        return DTOMapper.toResCategoryDTO(this.resourceTypeRepo.addOrUpdateResourceType(resourceType));
    }

    @Override
    public void deleteResourceType(int id) {
        ResourceType resourceType = this.resourceTypeRepo.getResourceTypeById(id);
        if (resourceType == null || Boolean.TRUE.equals(resourceType.getIsDeleted())) {
            throw new IdInvalidException("Resource type không tồn tại");
        }

        this.resourceTypeRepo.deleteResourceType(id);
    }
}

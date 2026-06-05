package com.paq.service;

import com.paq.pojo.request.ReqCategoryDTO;
import com.paq.pojo.response.ResCategoryDTO;
import java.util.List;
import java.util.Map;

public interface ResourceTypeService {

    List<ResCategoryDTO> getResourceTypes(Map<String, String> params);

    long countResourceTypes(Map<String, String> params);

    ResCategoryDTO getResourceTypeById(int id);

    ResCategoryDTO createResourceType(ReqCategoryDTO request);

    ResCategoryDTO updateResourceType(int id, ReqCategoryDTO request);

    void deleteResourceType(int id);
}

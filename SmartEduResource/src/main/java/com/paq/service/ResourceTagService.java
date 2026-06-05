package com.paq.service;

import com.paq.pojo.request.ReqCategoryDTO;
import com.paq.pojo.response.ResCategoryDTO;
import java.util.List;
import java.util.Map;

public interface ResourceTagService {

    List<ResCategoryDTO> getResourceTags(Map<String, String> params);

    long countResourceTags(Map<String, String> params);

    ResCategoryDTO getResourceTagById(int id);

    ResCategoryDTO createResourceTag(ReqCategoryDTO request);

    ResCategoryDTO updateResourceTag(int id, ReqCategoryDTO request);

    void deleteResourceTag(int id);
}

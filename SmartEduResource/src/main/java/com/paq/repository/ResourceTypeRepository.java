package com.paq.repository;

import java.util.List;
import java.util.Map;

import com.paq.pojo.ResourceType;

public interface ResourceTypeRepository {

    List<ResourceType> getResourceTypes(Map<String, String> params);

    long countResourceTypes(Map<String, String> params);

    ResourceType getResourceTypeById(int id);

    ResourceType getResourceTypeByName(String name);

    ResourceType addOrUpdateResourceType(ResourceType resourceType);

    void deleteResourceType(int id);
}

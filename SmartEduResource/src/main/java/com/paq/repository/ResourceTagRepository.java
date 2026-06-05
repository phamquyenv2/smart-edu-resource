package com.paq.repository;

import java.util.List;
import java.util.Map;

import com.paq.pojo.ResourceTag;

public interface ResourceTagRepository {

    List<ResourceTag> getResourceTags(Map<String, String> params);

    long countResourceTags(Map<String, String> params);

    ResourceTag getResourceTagById(int id);

    ResourceTag getResourceTagByName(String name);

    ResourceTag addOrUpdateResourceTag(ResourceTag resourceTag);

    void deleteResourceTag(int id);
}

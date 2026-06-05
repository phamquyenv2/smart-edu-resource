/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.service;

import com.paq.pojo.response.ResResourceDTO;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Admin
 */
public interface StudentResourceService {
    List<ResResourceDTO> getResources(Map<String, String> params);
    Long countResources(Map<String, String> params);
    ResResourceDTO getResourceById(int id);
    List<ResResourceDTO>getRelatedResources(int resourceId);

}

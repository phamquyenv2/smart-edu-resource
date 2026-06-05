package com.paq.controllers.admin;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paq.pojo.request.ReqCategoryDTO;
import com.paq.pojo.request.ReqSubjectDTO;
import com.paq.pojo.response.ResCategoryDTO;
import com.paq.pojo.response.ResPageDTO;
import com.paq.pojo.response.ResResponse;
import com.paq.pojo.response.ResSubjectDTO;
import com.paq.service.ResourceTagService;
import com.paq.service.ResourceTypeService;
import com.paq.service.SubjectService;
import com.paq.service.TopicService;
import com.paq.utils.DTOMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/secure/admin")
public class ApiAdminCategoryController {

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private ResourceTagService resourceTagService;

    @Autowired
    private ResourceTypeService resourceTypeService;

    @Autowired
    private Environment env;

    @GetMapping("/subjects")
    public ResponseEntity<ResResponse<ResPageDTO<ResSubjectDTO>>> getSubjects(
            @RequestParam Map<String, String> params) {
        int page = params.containsKey("page") ? Integer.parseInt(params.get("page")) : 1;
        int pageSize = this.env.getProperty("subjects.page_size", Integer.class, 10);
        long totalItems = this.subjectService.countSubjects(new HashMap<>(params));

        ResResponse<ResPageDTO<ResSubjectDTO>> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Lấy danh sách môn học thành công");
        res.setData(DTOMapper.toResPageDTO(this.subjectService.getSubjects(params), totalItems, page, pageSize));
        return ResponseEntity.ok(res);
    }

    @PostMapping("/subjects")
    public ResponseEntity<ResResponse<ResSubjectDTO>> createSubject(@Valid @RequestBody ReqSubjectDTO request) {
        ResResponse<ResSubjectDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.CREATED.value());
        res.setMessage("Tạo môn học thành công");
        res.setData(this.subjectService.createSubject(request));
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @PutMapping("/subjects/{id}")
    public ResponseEntity<ResResponse<ResSubjectDTO>> updateSubject(@PathVariable int id,
            @Valid @RequestBody ReqSubjectDTO request) {
        ResResponse<ResSubjectDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Cập nhật môn học thành công");
        res.setData(this.subjectService.updateSubject(id, request));
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<ResResponse<Object>> deleteSubject(@PathVariable int id) {
        this.subjectService.deleteSubject(id);
        ResResponse<Object> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Xóa môn học thành công");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/topics")
    public ResponseEntity<ResResponse<ResPageDTO<ResCategoryDTO>>> getTopics(
            @RequestParam Map<String, String> params) {
        int page = params.containsKey("page") ? Integer.parseInt(params.get("page")) : 1;
        int pageSize = this.env.getProperty("topics.page_size", Integer.class, 10);
        long totalItems = this.topicService.countTopics(new HashMap<>(params));

        ResResponse<ResPageDTO<ResCategoryDTO>> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Lấy danh sách chủ đề thành công");
        res.setData(DTOMapper.toResPageDTO(this.topicService.getTopics(params), totalItems, page, pageSize));
        return ResponseEntity.ok(res);
    }

    @PostMapping("/topics")
    public ResponseEntity<ResResponse<ResCategoryDTO>> createTopic(@Valid @RequestBody ReqCategoryDTO request) {
        ResResponse<ResCategoryDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.CREATED.value());
        res.setMessage("Tạo chủ đề thành công");
        res.setData(this.topicService.createTopic(request));
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @PutMapping("/topics/{id}")
    public ResponseEntity<ResResponse<ResCategoryDTO>> updateTopic(@PathVariable int id,
            @Valid @RequestBody ReqCategoryDTO request) {
        ResResponse<ResCategoryDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Cập nhật chủ đề thành công");
        res.setData(this.topicService.updateTopic(id, request));
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/topics/{id}")
    public ResponseEntity<ResResponse<Object>> deleteTopic(@PathVariable int id) {
        this.topicService.deleteTopic(id);
        ResResponse<Object> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Xóa chủ đề thành công");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/resource-tags")
    public ResponseEntity<ResResponse<ResPageDTO<ResCategoryDTO>>> getResourceTags(
            @RequestParam Map<String, String> params) {
        int page = params.containsKey("page") ? Integer.parseInt(params.get("page")) : 1;
        int pageSize = this.env.getProperty("resource_tags.page_size", Integer.class, 10);
        long totalItems = this.resourceTagService.countResourceTags(new HashMap<>(params));

        ResResponse<ResPageDTO<ResCategoryDTO>> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Lấy danh sách thẻ tài nguyên thành công");
        res.setData(DTOMapper.toResPageDTO(this.resourceTagService.getResourceTags(params), totalItems, page, pageSize));
        return ResponseEntity.ok(res);
    }

    @PostMapping("/resource-tags")
    public ResponseEntity<ResResponse<ResCategoryDTO>> createResourceTag(
            @Valid @RequestBody ReqCategoryDTO request) {
        ResResponse<ResCategoryDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.CREATED.value());
        res.setMessage("Tạo thẻ tài nguyên thành công");
        res.setData(this.resourceTagService.createResourceTag(request));
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @PutMapping("/resource-tags/{id}")
    public ResponseEntity<ResResponse<ResCategoryDTO>> updateResourceTag(@PathVariable int id,
            @Valid @RequestBody ReqCategoryDTO request) {
        ResResponse<ResCategoryDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Cập nhật thẻ tài nguyên thành công");
        res.setData(this.resourceTagService.updateResourceTag(id, request));
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/resource-tags/{id}")
    public ResponseEntity<ResResponse<Object>> deleteResourceTag(@PathVariable int id) {
        this.resourceTagService.deleteResourceTag(id);
        ResResponse<Object> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Xóa thẻ tài nguyên thành công");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/resource-types")
    public ResponseEntity<ResResponse<ResPageDTO<ResCategoryDTO>>> getResourceTypes(
            @RequestParam Map<String, String> params) {
        int page = params.containsKey("page") ? Integer.parseInt(params.get("page")) : 1;
        int pageSize = this.env.getProperty("resource_types.page_size", Integer.class, 10);
        long totalItems = this.resourceTypeService.countResourceTypes(new HashMap<>(params));

        ResResponse<ResPageDTO<ResCategoryDTO>> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Lấy danh sách loại tài liệu thành công");
        res.setData(DTOMapper.toResPageDTO(this.resourceTypeService.getResourceTypes(params), totalItems, page, pageSize));
        return ResponseEntity.ok(res);
    }

    @PostMapping("/resource-types")
    public ResponseEntity<ResResponse<ResCategoryDTO>> createResourceType(
            @Valid @RequestBody ReqCategoryDTO request) {
        ResResponse<ResCategoryDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.CREATED.value());
        res.setMessage("Tạo loại tài nguyên thành công");
        res.setData(this.resourceTypeService.createResourceType(request));
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @PutMapping("/resource-types/{id}")
    public ResponseEntity<ResResponse<ResCategoryDTO>> updateResourceType(@PathVariable int id,
            @Valid @RequestBody ReqCategoryDTO request) {
        ResResponse<ResCategoryDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Cập nhật loại tài nguyên thành công");
        res.setData(this.resourceTypeService.updateResourceType(id, request));
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/resource-types/{id}")
    public ResponseEntity<ResResponse<Object>> deleteResourceType(@PathVariable int id) {
        this.resourceTypeService.deleteResourceType(id);
        ResResponse<Object> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Xóa loại tài nguyên thành công");
        return ResponseEntity.ok(res);
    }
}

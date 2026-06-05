package com.paq.controllers.client;

import com.paq.pojo.request.ReqGenerateLearningPathDTO;
import com.paq.pojo.request.ReqLearningPathDTO;
import com.paq.pojo.response.ResLearningPathDTO;
import com.paq.pojo.response.ResResponse;
import com.paq.service.LearningPathService;
import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/secure/student")
public class ApiStudentLearningPathController {

    @Autowired
    private LearningPathService learningPathService;

    @GetMapping("/learning-paths")
    public ResponseEntity<ResResponse<List<ResLearningPathDTO>>> getMyLearningPaths(
            Principal principal) {
        ResResponse<List<ResLearningPathDTO>> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Lấy danh sách lộ trình thành công");
        res.setData(this.learningPathService.getMyLearningPaths(principal.getName()));

        return ResponseEntity.ok(res);
    }

    @GetMapping("/learning-paths/{id}")
    public ResponseEntity<ResResponse<ResLearningPathDTO>> getLearningPathDetail(
            @PathVariable("id") int id,
            Principal principal) {
        ResResponse<ResLearningPathDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Lấy chi tiết lộ trình thành công");
        res.setData(this.learningPathService.getLearningPathDetail(principal.getName(), id));

        return ResponseEntity.ok(res);
    }

    @PostMapping("/learning-paths/generate")
    public ResponseEntity<ResResponse<ResLearningPathDTO>> generateLearningPath(
            @RequestBody(required = false) ReqGenerateLearningPathDTO dto,
            Principal principal) {
        ResResponse<ResLearningPathDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.CREATED.value());
        res.setMessage("Tạo lộ trình AI thành công");
        res.setData(this.learningPathService.generateLearningPath(principal.getName(), dto));

        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PutMapping("/learning-paths/{id}")
    public ResponseEntity<ResResponse<ResLearningPathDTO>> updateLearningPath(
            @PathVariable("id") int id,
            @RequestBody ReqLearningPathDTO dto,
            Principal principal) {
        ResResponse<ResLearningPathDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Cập nhật lộ trình thành công");
        res.setData(this.learningPathService.updateLearningPath(principal.getName(), id, dto));

        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/learning-paths/{id}")
    public ResponseEntity<ResResponse<Void>> deleteLearningPath(
            @PathVariable("id") int id,
            Principal principal) {
        this.learningPathService.deleteLearningPath(principal.getName(), id);

        ResResponse<Void> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Xóa lộ trình thành công");

        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/learning-paths/items/{id}")
    public ResponseEntity<ResResponse<Void>> deleteItem(
            @PathVariable("id") int id,
            Principal principal) {
        this.learningPathService.deleteItem(principal.getName(), id);

        ResResponse<Void> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Xóa mục trong lộ trình thành công");

        return ResponseEntity.ok(res);
    }
}

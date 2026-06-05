package com.paq.controllers.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paq.pojo.response.ResEnrollmentDTO;
import com.paq.pojo.response.ResResponse;
import com.paq.service.EnrollmentService;

@RestController
@RequestMapping("/api/secure/student")
public class ApiEnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @GetMapping("/my-enrollments")
    public ResponseEntity<ResResponse<List<ResEnrollmentDTO>>> getMyEnrollments() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ResResponse<List<ResEnrollmentDTO>> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Lấy danh sách khóa học của tôi thành công");
        res.setData(this.enrollmentService.getMyEnrollments(auth != null ? auth.getName() : null));

        return ResponseEntity.ok(res);
    }

}

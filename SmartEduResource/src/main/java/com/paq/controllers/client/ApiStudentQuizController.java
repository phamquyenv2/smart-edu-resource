/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.controllers.client;

import com.paq.pojo.request.ReqSubmitQuizDTO;
import com.paq.pojo.response.ResQuizDTO;
import com.paq.pojo.response.ResQuizResultDTO;
import com.paq.pojo.response.ResResponse;
import com.paq.service.StudentQuizService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Admin
 */
@RestController
@RequestMapping("/api")
public class ApiStudentQuizController {

    @Autowired
    private StudentQuizService quizService;

    @GetMapping("/secure/student/quizzes")
    public ResponseEntity<ResResponse<List<ResQuizDTO>>> getQuizzes(Principal principal) {
        ResResponse<List<ResQuizDTO>> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Get quizzes successfully");
        res.setData(this.quizService.getQuizzes(principal.getName()));

        return ResponseEntity.ok(res);
    }

    @GetMapping("/secure/student/quizzes/{id}")
    public ResponseEntity<ResResponse<ResQuizDTO>> getQuizDetail(
            @PathVariable(value = "id") int id,
            Principal principal) {

        ResResponse<ResQuizDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Get quiz detail successfully");
        res.setData(this.quizService.getQuizById(principal.getName(), id));

        return ResponseEntity.ok(res);
    }

    @PostMapping("/secure/student/quizzes/{id}/submit")
    public ResponseEntity<ResResponse<ResQuizResultDTO>> submitQuiz(
            @PathVariable(value = "id") int id,
            @Valid @RequestBody ReqSubmitQuizDTO request,
            Principal principal) {

        ResResponse<ResQuizResultDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Submit quiz successfully");
        res.setData(this.quizService.submitQuiz(principal.getName(), id, request));

        return ResponseEntity.ok(res);
    }

    @GetMapping("/secure/student/quizzes/results")
    public ResponseEntity<ResResponse<List<ResQuizResultDTO>>> getMyQuizResults(
            Principal principal) {

        ResResponse<List<ResQuizResultDTO>> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Get quiz results successfully");
        res.setData(this.quizService.getMyQuizResults(principal.getName()));

        return ResponseEntity.ok(res);
    }

}

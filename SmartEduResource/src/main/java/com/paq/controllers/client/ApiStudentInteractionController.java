/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.controllers.client;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paq.pojo.request.ReqInteractionDTO;
import com.paq.pojo.request.ReqInteractionReplyDTO;
import com.paq.pojo.response.ResInteractionDTO;
import com.paq.pojo.response.ResInteractionReplyDTO;
import com.paq.pojo.response.ResResponse;
import com.paq.service.StudentInteractionService;

/**
 *
 * @author Admin
 */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiStudentInteractionController {

    @Autowired
    private StudentInteractionService interactionService;

    @GetMapping("/student/resources/{resourceId}/interactions")
    public ResponseEntity<ResResponse<List<ResInteractionDTO>>> getInteractions(
            @PathVariable("resourceId") int resourceId) {

        ResResponse<List<ResInteractionDTO>> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Get interactions successfully");
        res.setData(this.interactionService.getInteractionsByResourceId(resourceId));

        return ResponseEntity.ok(res);
    }

    @PostMapping("/secure/student/resources/{resourceId}/interactions")
    public ResponseEntity<ResResponse<ResInteractionDTO>> createInteraction(
            @PathVariable("resourceId") int resourceId,
            @RequestBody ReqInteractionDTO request,
            Principal principal) {

        if (principal == null) {
            ResResponse<ResInteractionDTO> res = new ResResponse<>();
            res.setStatusCode(HttpStatus.UNAUTHORIZED.value());
            res.setMessage("Vui lòng đăng nhập");
            res.setData(null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }

        ResResponse<ResInteractionDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Create interaction successfully");
        res.setData(this.interactionService.createInteraction(
                principal.getName(),
                resourceId,
                request
        ));

        return ResponseEntity.ok(res);
    }

    @PutMapping("/secure/student/interactions/{interactionId}")
    public ResponseEntity<ResResponse<ResInteractionDTO>> updateInteraction(
            @PathVariable("interactionId") int interactionId,
            @RequestBody ReqInteractionDTO request,
            Principal principal) {

        ResResponse<ResInteractionDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Update interaction successfully");
        res.setData(this.interactionService.updateInteraction(
                principal.getName(),
                interactionId,
                request
        ));

        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/secure/student/interactions/{interactionId}")
    public ResponseEntity<ResResponse<ResInteractionDTO>> deleteInteraction(
            @PathVariable("interactionId") int interactionId,
            Principal principal) {

        ResResponse<ResInteractionDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Delete interaction successfully");
        res.setData(this.interactionService.deleteInteraction(
                principal.getName(),
                interactionId
        ));

        return ResponseEntity.ok(res);
    }

    @GetMapping("/student/interactions/{interactionId}/replies")
    public ResponseEntity<ResResponse<List<ResInteractionReplyDTO>>> getReplies(
            @PathVariable("interactionId") int interactionId) {

        ResResponse<List<ResInteractionReplyDTO>> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Get replies successfully");
        res.setData(this.interactionService.getRepliesByInteractionId(interactionId));

        return ResponseEntity.ok(res);
    }

    @PostMapping("/secure/student/interactions/{interactionId}/replies")
    public ResponseEntity<ResResponse<ResInteractionReplyDTO>> createReply(
            @PathVariable("interactionId") int interactionId,
            @RequestBody ReqInteractionReplyDTO request,
            Principal principal) {

        if (principal == null) {
            ResResponse<ResInteractionReplyDTO> res = new ResResponse<>();
            res.setStatusCode(HttpStatus.UNAUTHORIZED.value());
            res.setMessage("Vui lòng đăng nhập");
            res.setData(null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }

        ResResponse<ResInteractionReplyDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Create reply successfully");
        res.setData(this.interactionService.createReply(
                principal.getName(),
                interactionId,
                request
        ));

        return ResponseEntity.ok(res);
    }

    @PutMapping("/secure/student/replies/{replyId}")
    public ResponseEntity<ResResponse<ResInteractionReplyDTO>> updateReply(
            @PathVariable("replyId") int replyId,
            @RequestBody ReqInteractionReplyDTO request,
            Principal principal) {

        ResResponse<ResInteractionReplyDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Update reply successfully");
        res.setData(this.interactionService.updateReply(
                principal.getName(),
                replyId,
                request
        ));

        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/secure/student/replies/{replyId}")
    public ResponseEntity<ResResponse<ResInteractionReplyDTO>> deleteReply(
            @PathVariable("replyId") int replyId,
            Principal principal) {

        ResResponse<ResInteractionReplyDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Delete reply successfully");
        res.setData(this.interactionService.deleteReply(
                principal.getName(),
                replyId
        ));

        return ResponseEntity.ok(res);
    }
}

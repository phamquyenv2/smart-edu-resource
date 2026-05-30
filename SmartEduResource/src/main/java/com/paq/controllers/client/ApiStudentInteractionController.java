/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.controllers.client;

import com.paq.pojo.request.ReqInteractionDTO;
import com.paq.pojo.request.ReqInteractionReplyDTO;
import com.paq.pojo.response.ResInteractionDTO;
import com.paq.pojo.response.ResInteractionReplyDTO;
import com.paq.service.StudentInteractionService;
import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<List<ResInteractionDTO>> getInteractions(
            @PathVariable int resourceId) {
        return ResponseEntity.ok(
                this.interactionService.getInteractionsByResourceId(resourceId)
        );
    }

    @PostMapping("/secure/student/interactions/{id}")
    public ResponseEntity<ResInteractionDTO> createInteraction(
            @PathVariable int resourceId,
            @RequestBody ReqInteractionDTO request,
            Principal principal) {
        return ResponseEntity.ok(
                this.interactionService.createInteraction(
                        principal.getName(),
                        resourceId,
                        request
                )
        );
    }

    @PutMapping("/secure/student/interactions/{id}")
    public ResponseEntity<ResInteractionDTO> updateInteraction(
            @PathVariable int id,
            @RequestBody ReqInteractionDTO request,
            Principal principal) {
        return ResponseEntity.ok(
                this.interactionService.updateInteraction(
                        principal.getName(),
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/secure/student/interactions/{id}")
    public ResponseEntity<ResInteractionDTO> deleteInteraction(
            @PathVariable int id,
            Principal principal
    ) {
        return ResponseEntity.ok(
                this.interactionService.deleteInteraction(
                        principal.getName(),
                        id
                )
        );
    }

    @GetMapping("/student/interactions/{id}/replies")
    public ResponseEntity<List<ResInteractionReplyDTO>> getReplies(
            @PathVariable int id) {

        return ResponseEntity.ok(
                this.interactionService.getRepliesByInteractionId(id)
        );
    }

    @PostMapping("/secure/student/interactions/{id}/replies")
    public ResponseEntity<ResInteractionReplyDTO> createReply(
            @PathVariable int id,
            @RequestBody ReqInteractionReplyDTO request,
            Principal principal) {

        return ResponseEntity.ok(
                this.interactionService.createReply(
                        principal.getName(),
                        id,
                        request
                )
        );
    }

    @PutMapping("/secure/student/interaction-replies/{id}")
    public ResponseEntity<ResInteractionReplyDTO> updateReply(
            @PathVariable int id,
            @RequestBody ReqInteractionReplyDTO request,
            Principal principal) {

        return ResponseEntity.ok(
                this.interactionService.updateReply(
                        principal.getName(),
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/secure/student/interaction-replies/{id}")
    public ResponseEntity<ResInteractionReplyDTO> deleteReply(
            @PathVariable int id,
            Principal principal) {

        return ResponseEntity.ok(
                this.interactionService.deleteReply(
                        principal.getName(),
                        id
                )
        );
    }
}

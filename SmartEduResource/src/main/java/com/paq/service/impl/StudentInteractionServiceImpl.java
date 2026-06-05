/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.service.impl;

import com.paq.pojo.Interaction;
import com.paq.pojo.InteractionReply;
import com.paq.pojo.Resource;
import com.paq.pojo.User;
import com.paq.pojo.request.ReqInteractionDTO;
import com.paq.pojo.request.ReqInteractionReplyDTO;
import com.paq.pojo.response.ResInteractionDTO;
import com.paq.pojo.response.ResInteractionReplyDTO;
import com.paq.repository.InteractionRepository;
import com.paq.repository.ResourceRepository;
import com.paq.service.StudentInteractionService;
import com.paq.service.UserService;
import com.paq.utils.DTOMapper;
import com.paq.utils.constant.TypeInteractionEnum;
import com.paq.utils.error.IdInvalidException;
import com.paq.utils.error.PermissionException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Admin
 */
@Service
@Transactional
public class StudentInteractionServiceImpl implements StudentInteractionService {

    @Autowired
    private InteractionRepository interactionRepo;

    @Autowired
    private ResourceRepository resourceRepo;

    @Autowired
    private UserService userService;

    @Override
    public List<ResInteractionDTO> getInteractionsByResourceId(int resourceId) {
        return this.interactionRepo.getInteractionsByResourceId(resourceId)
                .stream()
                .map(i -> DTOMapper.toInteractionDTO(i))
                .collect(Collectors.toList());
    }

    @Override
    public ResInteractionDTO createInteraction(String username, int resourceId, ReqInteractionDTO request) {
        User user = this.userService.getUserByUsername(username);
        Resource resource = this.resourceRepo.getResourceById(resourceId);

        if (user == null) {
            throw new PermissionException("User khong hop le");
        }

        if (resource == null || Boolean.TRUE.equals(resource.getIsDeleted())) {
            throw new IdInvalidException("Resource khong ton tai");
        }

        if (request.getNote() == null || request.getNote().trim().isEmpty()) {
            throw new IllegalArgumentException("Noi dung khong duoc de trong");
        }

        if (request.getType() == null || request.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Loai interaction khong duoc de trong");
        }

        Interaction interaction = new Interaction();
        interaction.setNote(request.getNote());
        interaction.setSelectedText(request.getSelectedText());
        interaction.setPageNumber(request.getPageNumber());
        interaction.setTimeOffsetSeconds(request.getTimeOffsetSeconds());
        interaction.setPositionX(request.getPositionX());
        interaction.setType(TypeInteractionEnum.valueOf(request.getType()));
        interaction.setCreatedAt(new Date());
        interaction.setUpdatedAt(new Date());
        interaction.setIsDeleted(false);
        interaction.setResourceId(resource);
        interaction.setUserId(user);

        return DTOMapper.toInteractionDTO(this.interactionRepo.addInteraction(interaction));
    }

    @Override
    public ResInteractionDTO updateInteraction(String username, int interactionId, ReqInteractionDTO request) {
        User user = this.userService.getUserByUsername(username);
        Interaction interaction = this.interactionRepo.getInteractionById(interactionId);

        if (interaction == null) {
            throw new IdInvalidException("Interaction khong ton tai");
        }

        if (user == null || !interaction.getUserId().getId().equals(user.getId())) {
            throw new PermissionException("Ban chi duoc sua noi dung cua chinh minh");
        }

        if (request.getNote() == null || request.getNote().trim().isEmpty()) {
            throw new IllegalArgumentException("Noi dung khong duoc de trong");
        }

        interaction.setNote(request.getNote());
        interaction.setSelectedText(request.getSelectedText());
        interaction.setPageNumber(request.getPageNumber());
        interaction.setTimeOffsetSeconds(request.getTimeOffsetSeconds());
        interaction.setPositionX(request.getPositionX());
        interaction.setUpdatedAt(new Date());

        if (request.getType() != null && !request.getType().trim().isEmpty()) {
            interaction.setType(TypeInteractionEnum.valueOf(request.getType()));
        }

        return DTOMapper.toInteractionDTO(this.interactionRepo.updateInteraction(interaction));
    }

    @Override
    public ResInteractionDTO deleteInteraction(String username, int interactionId) {
        User user = this.userService.getUserByUsername(username);
        Interaction interaction = this.interactionRepo.getInteractionById(interactionId);

        if (interaction == null) {
            throw new IdInvalidException("Interaction khong ton tai");
        }

        if (user == null || !interaction.getUserId().getId().equals(user.getId())) {
            throw new PermissionException("Ban chi duoc xoa noi dung cua chinh minh");
        }

        interaction.setIsDeleted(true);
        interaction.setUpdatedAt(new Date());

        return DTOMapper.toInteractionDTO(this.interactionRepo.updateInteraction(interaction));
    }

    @Override
    public List<ResInteractionReplyDTO> getRepliesByInteractionId(int interactionId) {
        return this.interactionRepo.getRepliesByInteractionId(interactionId)
                .stream()
                .map(r -> DTOMapper.toInteractionReplyDTO(r))
                .collect(Collectors.toList());
    }

    @Override
    public ResInteractionReplyDTO createReply(String username, int interactionId, ReqInteractionReplyDTO request) {
        User user = this.userService.getUserByUsername(username);
        Interaction interaction = this.interactionRepo.getInteractionById(interactionId);

        if (user == null) {
            throw new PermissionException("User khong hop le");
        }

        if (interaction == null) {
            throw new IdInvalidException("Interaction khong ton tai");
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Noi dung khong duoc de trong");
        }

        InteractionReply reply = new InteractionReply();
        reply.setContent(request.getContent());
        reply.setIsDeleted(false);
        reply.setInteractionId(interaction);
        reply.setUserId(user);

        return DTOMapper.toInteractionReplyDTO(this.interactionRepo.addReply(reply));
    }

    @Override
    public ResInteractionReplyDTO updateReply(String username, int replyId, ReqInteractionReplyDTO request) {
        User user = this.userService.getUserByUsername(username);
        InteractionReply reply = this.interactionRepo.getReplyById(replyId);

        if (reply == null) {
            throw new IdInvalidException("Reply khong ton tai");
        }

        if (user == null || !reply.getUserId().getId().equals(user.getId())) {
            throw new PermissionException("Ban chi duoc sua noi dung cua chinh minh");
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Noi dung khong duoc de trong");
        }

        reply.setContent(request.getContent());

        return DTOMapper.toInteractionReplyDTO(this.interactionRepo.updateReply(reply));
    }

    @Override
    public ResInteractionReplyDTO deleteReply(String username, int replyId) {
        User user = this.userService.getUserByUsername(username);
        InteractionReply reply = this.interactionRepo.getReplyById(replyId);

        if (reply == null) {
            throw new IdInvalidException("Reply khong ton tai");
        }

        if (user == null || !reply.getUserId().getId().equals(user.getId())) {
            throw new PermissionException("Ban chi duoc xoa noi dung cua chinh minh");
        }

        reply.setIsDeleted(true);

        return DTOMapper.toInteractionReplyDTO(this.interactionRepo.updateReply(reply));
    }

}

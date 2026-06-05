package com.paq.controllers.client;

import com.paq.pojo.request.ReqPrivateChatRoomDTO;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paq.pojo.response.ResChatParticipantDTO;
import com.paq.pojo.response.ResChatRoomDTO;
import com.paq.pojo.response.ResPageDTO;
import com.paq.pojo.response.ResResponse;
import com.paq.service.ChatParticipantService;
import com.paq.service.ChatRoomService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/secure/student")
public class ApiChatController {

    @Autowired
    private ChatRoomService roomService;

    @Autowired
    private ChatParticipantService participantService;

    @GetMapping("/chat-rooms")
    public ResponseEntity<ResResponse<ResPageDTO<ResChatRoomDTO>>> getRooms(
            @RequestParam Map<String, String> params) {
        ResResponse<ResPageDTO<ResChatRoomDTO>> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Lấy danh sách phòng chat thành công");
        res.setData(this.roomService.getRooms(params));
        return ResponseEntity.ok(res);
    }

    @GetMapping("/chat-rooms/{id}")
    public ResponseEntity<ResResponse<ResChatRoomDTO>> getRoomById(@PathVariable("id") int id) {
        ResResponse<ResChatRoomDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Lấy thông tin phòng chat thành công");
        res.setData(this.roomService.getRoomById(id));
        return ResponseEntity.ok(res);
    }

    @GetMapping("/chat-rooms/{roomId}/participants")
    public ResponseEntity<ResResponse<ResPageDTO<ResChatParticipantDTO>>> getParticipants(
            @PathVariable("roomId") int roomId,
            @RequestParam Map<String, String> params) {
        ResResponse<ResPageDTO<ResChatParticipantDTO>> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Lấy danh sách người tham gia phòng chat thành công");
        res.setData(this.participantService.getParticipantsByRoomId(roomId, params));
        return ResponseEntity.ok(res);
    }

    @PostMapping("/chat-rooms/private")
    public ResponseEntity<ResResponse<ResChatRoomDTO>> createPrivateRoomWithLecturer(
            @Valid @RequestBody ReqPrivateChatRoomDTO request) {

        ResResponse<ResChatRoomDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Lấy hoặc tạo phòng chat riêng với giảng viên thành công");
        res.setData(this.roomService.createPrivateRoomWithLecturer(request));

        return ResponseEntity.ok(res);
    }

    @PostMapping("/chat-rooms/private/course/{courseId}")
    public ResponseEntity<ResResponse<ResChatRoomDTO>> getOrCreatePrivateRoomByCourse(
            @PathVariable("courseId") int courseId) {

        ResResponse<ResChatRoomDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Lấy hoặc tạo phòng chat riêng với giảng viên thành công");
        res.setData(this.roomService.getOrCreatePrivateRoomByCourse(courseId));

        return ResponseEntity.ok(res);
    }

    @PostMapping("/chat-rooms/class/course/{courseId}")
    public ResponseEntity<ResResponse<ResChatRoomDTO>> getOrCreateClassRoomByCourse(
            @PathVariable("courseId") int courseId) {

        ResResponse<ResChatRoomDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Lấy hoặc tạo phòng thảo luận lớp thành công");
        res.setData(this.roomService.getOrCreateClassRoomByCourse(courseId));

        return ResponseEntity.ok(res);
    }
}

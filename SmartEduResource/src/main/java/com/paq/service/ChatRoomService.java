package com.paq.service;

import com.paq.pojo.request.ReqChatRoomDTO;
import com.paq.pojo.request.ReqPrivateChatRoomDTO;
import com.paq.pojo.response.ResChatRoomDTO;
import com.paq.pojo.response.ResPageDTO;
import java.util.Map;

public interface ChatRoomService {

    ResPageDTO<ResChatRoomDTO> getRooms(Map<String, String> params);

    ResChatRoomDTO getRoomById(int id);

    ResChatRoomDTO createRoom(ReqChatRoomDTO request);

    ResChatRoomDTO createPrivateRoomWithLecturer(ReqPrivateChatRoomDTO request);

    ResChatRoomDTO updateRoom(int id, ReqChatRoomDTO request);

    void deleteRoom(int id);

    ResChatRoomDTO getOrCreatePrivateRoomByCourse(int courseId);
    
    ResChatRoomDTO getOrCreateClassRoomByCourse(int courseId);
}

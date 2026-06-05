package com.paq.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.paq.pojo.ChatRoom;
import com.paq.pojo.ChatParticipant;
import com.paq.pojo.Course;
import com.paq.pojo.User;
import com.paq.pojo.Lecturer;
import com.paq.pojo.request.ReqChatRoomDTO;
import com.paq.pojo.request.ReqPrivateChatRoomDTO;
import com.paq.pojo.response.ResChatRoomDTO;
import com.paq.pojo.response.ResPageDTO;
import com.paq.repository.ChatParticipantRepository;
import com.paq.repository.ChatRoomRepository;
import com.paq.repository.CourseRepository;
import com.paq.repository.EnrollmentRepository;
import com.paq.service.ChatRoomService;
import com.paq.service.PermissionService;
import com.paq.utils.DTOMapper;
import com.paq.utils.constant.ChatRoomTypeEnum;
import com.paq.utils.constant.RoleEnum;
import com.paq.utils.error.IdInvalidException;
import com.paq.utils.error.PermissionException;
import java.util.HashMap;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatRoomServiceImpl implements ChatRoomService {

    @Autowired
    private ChatRoomRepository roomRepo;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private ChatParticipantRepository participantRepo;

    @Autowired
    private EnrollmentRepository enrollmentRepo;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private Environment env;

    @Override
    public ResPageDTO<ResChatRoomDTO> getRooms(Map<String, String> params) {
        User user = this.permissionService.getCurrentUser();
        List<ChatRoom> rooms;
        Long totalItems;
        if (this.permissionService.canManageChatRooms(user)) {
            rooms = this.roomRepo.getRooms(params);
            totalItems = this.roomRepo.countRooms(params);
        } else {
            rooms = this.roomRepo.getRoomsAvailableToUser(params, user.getId());
            totalItems = this.roomRepo.countRoomsAvailableToUser(params, user.getId());
        }

        List<ResChatRoomDTO> items = rooms.stream()
                .map(this::toRoomDTO)
                .collect(Collectors.toList());
        int page = params != null && params.containsKey("page") ? Integer.parseInt(params.get("page")) : 1;
        int pageSize = this.env.getProperty("chat_rooms.page_size", Integer.class, 10);
        return DTOMapper.toResPageDTO(items, totalItems, page, pageSize);
    }

    @Override
    public ResChatRoomDTO getRoomById(int id) {
        this.permissionService.requireChatRoomAccess(id);

        ChatRoom room = this.roomRepo.getRoomById(id);
        if (room == null) {
            throw new IdInvalidException("Chat room không tồn tại");
        }

        return this.toRoomDTO(room);
    }

    @Override
    public ResChatRoomDTO createRoom(ReqChatRoomDTO request) {
        User user = this.permissionService.getCurrentUser();
        Course course = this.resolveCourse(request.getCourseId());
        if (course != null) {
            this.permissionService.requireCourseLecturerOrAdmin(course.getId());
        } else {
            this.permissionService.requireLecturerOrAdmin();
        }

        ChatRoom room = new ChatRoom();
        room.setType(request.getType());
        room.setName(request.getName());
        room.setCourseId(course);
        room.setCreatedBy(user);
        room.setCreatedAt(new Date());

        ChatRoom savedRoom = this.roomRepo.addOrUpdateRoom(room);
        this.addCourseLecturerParticipant(savedRoom);

        return this.toRoomDTO(savedRoom);
    }

    @Override
    public ResChatRoomDTO createPrivateRoomWithLecturer(ReqPrivateChatRoomDTO request) {
        this.permissionService.requireStudent();
        User studentUser = this.permissionService.getCurrentUser();
        Course course = this.resolveCourse(request.getCourseId());

        if (!this.enrollmentRepo.existsByCourseIdAndUserId(course.getId(), studentUser.getId())) {
            throw new PermissionException("Ban chua ghi danh khoa hoc nay");
        }

        User lecturerUser = this.getCourseLecturerUser(course);
        if (lecturerUser == null || !lecturerUser.getId().equals(request.getLecturerUserId())) {
            throw new IdInvalidException("Lecturer khong phu trach khoa hoc nay");
        }

        ChatRoom existedRoom = this.roomRepo.getPrivateRoomByCourseAndUsers(
                course.getId(), studentUser.getId(), lecturerUser.getId());
        if (existedRoom != null) {
            return this.toRoomDTO(existedRoom);
        }

        ChatRoom room = new ChatRoom();
        room.setType(ChatRoomTypeEnum.PRIVATE);
        room.setCourseId(course);
        room.setCreatedBy(studentUser);
        room.setCreatedAt(new Date());

        ChatRoom savedRoom = this.roomRepo.addOrUpdateRoom(room);
        this.addParticipantIfAbsent(savedRoom, studentUser);
        this.addParticipantIfAbsent(savedRoom, lecturerUser);

        return this.toRoomDTO(savedRoom);
    }

    @Override
    public ResChatRoomDTO updateRoom(int id, ReqChatRoomDTO request) {
        this.permissionService.requireChatRoomManager(id);

        ChatRoom room = this.roomRepo.getRoomById(id);
        if (room == null) {
            throw new IdInvalidException("Chat room không tồn tại");
        }

        Course course = this.resolveCourse(request.getCourseId());
        if (course != null) {
            this.permissionService.requireCourseLecturerOrAdmin(course.getId());
        }

        room.setType(request.getType());
        room.setName(request.getName());
        room.setCourseId(course);

        return this.toRoomDTO(this.roomRepo.addOrUpdateRoom(room));
    }

    @Override
    public void deleteRoom(int id) {
        this.permissionService.requireChatRoomManager(id);

        ChatRoom room = this.roomRepo.getRoomById(id);
        if (room == null) {
            throw new IdInvalidException("Chat room không tồn tại");
        }

        this.roomRepo.deleteRoom(id);
    }

    private Course resolveCourse(Integer courseId) {
        if (courseId == null) {
            return null;
        }

        Course course = this.courseRepo.getCourseById(courseId);
        if (course == null) {
            throw new IdInvalidException("Course không tồn tại");
        }

        return course;
    }

    private ResChatRoomDTO toRoomDTO(ChatRoom room) {
        ResChatRoomDTO dto = DTOMapper.toResChatRoomDTO(room);
        if (dto != null && room.getId() != null) {
            dto.setParticipantCount(this.participantRepo.countStudentParticipantsByRoomId(room.getId()));
        }

        return dto;
    }

    private void addCourseLecturerParticipant(ChatRoom room) {
        if (room == null || room.getCourseId() == null) {
            return;
        }

        User lecturerUser = this.getCourseLecturerUser(room.getCourseId());
        if (lecturerUser != null) {
            this.addParticipantIfAbsent(room, lecturerUser);
        }
    }

    private User getCourseLecturerUser(Course course) {
        Lecturer lecturer = course != null ? course.getLecturerId() : null;
        return lecturer != null ? lecturer.getUserId() : null;
    }

    private void addParticipantIfAbsent(ChatRoom room, User user) {
        if (room == null || room.getId() == null || user == null || user.getId() == null) {
            return;
        }

        if (this.participantRepo.getParticipantByRoomIdAndUserId(room.getId(), user.getId()) != null) {
            return;
        }

        ChatParticipant participant = new ChatParticipant();
        participant.setRoomId(room);
        participant.setUserId(user);
        participant.setJoinedAt(new Date());
        participant.setIsMuted(Boolean.FALSE);
        this.participantRepo.addParticipant(participant);
    }

    @Override
    @Transactional
    public ResChatRoomDTO getOrCreatePrivateRoomByCourse(int courseId) {
        this.permissionService.requireStudent();

        User studentUser = this.permissionService.getCurrentUser();
        Course course = this.resolveCourse(courseId);

        if (!this.enrollmentRepo.existsByCourseIdAndUserId(course.getId(), studentUser.getId())) {
            throw new PermissionException("Bạn chưa ghi danh khóa học này");
        }

        User lecturerUser = this.getCourseLecturerUser(course);
        if (lecturerUser == null) {
            throw new IdInvalidException("Khóa học chưa có giảng viên phụ trách");
        }

        ChatRoom existedRoom = this.roomRepo.getPrivateRoomByCourseAndUsers(
                course.getId(),
                studentUser.getId(),
                lecturerUser.getId()
        );

        if (existedRoom != null) {
            return this.toRoomDTO(existedRoom);
        }

        ChatRoom room = new ChatRoom();
        room.setType(ChatRoomTypeEnum.PRIVATE);
        room.setCourseId(course);
        room.setCreatedBy(studentUser);
        room.setCreatedAt(new Date());

        ChatRoom savedRoom = this.roomRepo.addOrUpdateRoom(room);

        this.addParticipantIfAbsent(savedRoom, studentUser);
        this.addParticipantIfAbsent(savedRoom, lecturerUser);

        return this.toRoomDTO(savedRoom);
    }

    @Override
    @Transactional
    public ResChatRoomDTO getOrCreateClassRoomByCourse(int courseId) {
        User currentUser = this.permissionService.getCurrentUser();
        Course course = this.resolveCourse(courseId);

        if (currentUser.getRole() == RoleEnum.STUDENT) {
            if (!this.enrollmentRepo.existsByCourseIdAndUserId(course.getId(), currentUser.getId())) {
                throw new PermissionException("Bạn phải thuộc khóa học này thì mới có thể thảo luận.");
            }
        } else {
            this.permissionService.requireCourseLecturerOrAdmin(course.getId());
        }

        Map<String, String> params = new HashMap<>();
        params.put("courseId", String.valueOf(courseId));
        params.put("type", ChatRoomTypeEnum.CLASS.name());

        List<ChatRoom> rooms = this.roomRepo.getRooms(params);

        if (rooms != null && !rooms.isEmpty()) {
            ChatRoom existedRoom = rooms.get(0);
            this.addParticipantIfAbsent(existedRoom, currentUser);
            return this.toRoomDTO(existedRoom);
        }

        ChatRoom room = new ChatRoom();
        room.setType(ChatRoomTypeEnum.CLASS);
        room.setName("Lớp " + course.getName());
        room.setCourseId(course);
        room.setCreatedBy(currentUser);
        room.setCreatedAt(new Date());

        ChatRoom savedRoom = this.roomRepo.addOrUpdateRoom(room);

        this.addParticipantIfAbsent(savedRoom, currentUser);

        User lecturerUser = this.getCourseLecturerUser(course);
        if (lecturerUser != null) {
            this.addParticipantIfAbsent(savedRoom, lecturerUser);
        }

        return this.toRoomDTO(savedRoom);
    }

}

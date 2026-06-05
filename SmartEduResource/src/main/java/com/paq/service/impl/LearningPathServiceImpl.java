package com.paq.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paq.pojo.Course;
import com.paq.pojo.LearningPath;
import com.paq.pojo.LearningPathItem;
import com.paq.pojo.Resource;
import com.paq.pojo.Student;
import com.paq.pojo.User;
import com.paq.pojo.request.ReqGenerateLearningPathDTO;
import com.paq.pojo.request.ReqLearningPathDTO;
import com.paq.pojo.response.ResLearningPathDTO;
import com.paq.pojo.response.ResLearningPathItemDTO;
import com.paq.repository.CourseRepository;
import com.paq.repository.LearningPathRepository;
import com.paq.repository.ResourceRepository;
import com.paq.service.LearningPathService;
import com.paq.service.UserService;
import com.paq.utils.constant.PathItemTypeEnum;
import com.paq.utils.error.IdInvalidException;
import com.paq.utils.error.PermissionException;
import com.paq.utils.DTOMapper;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@PropertySource(value = "classpath:xiaomimimo.properties", ignoreResourceNotFound = true)
public class LearningPathServiceImpl implements LearningPathService {

    @Autowired
    private LearningPathRepository learningPathRepo;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private ResourceRepository resourceRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private Environment env;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Student getStudent(String username) {
        User user = this.userService.getUserByUsername(username);
        if (user == null || user.getStudent() == null) {
            throw new PermissionException("Tài khoản hiện tại không phải sinh viên!");
        }
        return user.getStudent();
    }

    @Override
    public List<ResLearningPathDTO> getMyLearningPaths(String username) {
        Student student = getStudent(username);
        List<LearningPath> paths = this.learningPathRepo.getLearningPathsByStudentId(student.getId());

        return paths.stream().map(lp -> {
            ResLearningPathDTO dto = toDTO(lp);
            List<LearningPathItem> items = this.learningPathRepo.getItemsByPathId(lp.getId());
            dto.setItems(items.stream().map(this::toItemDTO).collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public ResLearningPathDTO getLearningPathDetail(String username, int pathId) {
        Student student = getStudent(username);
        LearningPath lp = this.learningPathRepo.getLearningPathById(pathId);

        if (lp == null || Boolean.TRUE.equals(lp.getIsDeleted())) {
            throw new IdInvalidException("Lộ trình không tồn tại!");
        }

        if (!lp.getStudentId().getId().equals(student.getId())) {
            throw new PermissionException("Bạn không có quyền xem lộ trình này!");
        }

        ResLearningPathDTO dto = toDTO(lp);
        List<LearningPathItem> items = this.learningPathRepo.getItemsByPathId(lp.getId());
        dto.setItems(items.stream().map(this::toItemDTO).collect(Collectors.toList()));
        return dto;
    }

    @Override
    public ResLearningPathDTO generateLearningPath(String username, ReqGenerateLearningPathDTO reqDto) {
        Student student = getStudent(username);
        User user = student.getUserId();

        String studentContext = buildStudentContext(student, user, reqDto);

        List<Course> courses = this.courseRepo.getCourses(new HashMap<>());
        List<Resource> resources = this.resourceRepo.getResources(new HashMap<>());

        String availableContent = buildAvailableContent(courses, resources);

        String aiResponse = callXiaomiMiMoApi(studentContext, availableContent, reqDto);

        LearningPath lp = new LearningPath();
        lp.setStudentId(student);
        lp.setGeneratedByAi(true);
        lp.setCreatedAt(new Date());
        lp.setUpdatedAt(new Date());
        lp.setIsDeleted(false);

        String goal = reqDto != null && reqDto.getGoal() != null ? reqDto.getGoal() : student.getLearningGoal();
        lp.setGoal(goal != null ? goal : "Lộ trình học tập cá nhân");

        parseAndSaveLearningPath(lp, aiResponse, courses, resources);

        ResLearningPathDTO dto = toDTO(lp);
        List<LearningPathItem> items = this.learningPathRepo.getItemsByPathId(lp.getId());
        dto.setItems(items.stream().map(this::toItemDTO).collect(Collectors.toList()));

        return dto;
    }

    @Override
    public ResLearningPathDTO updateLearningPath(String username, int pathId, ReqLearningPathDTO reqDto) {
        Student student = getStudent(username);
        LearningPath lp = this.learningPathRepo.getLearningPathById(pathId);

        if (lp == null || Boolean.TRUE.equals(lp.getIsDeleted())) {
            throw new IdInvalidException("Lộ trình không tồn tại!");
        }

        if (!lp.getStudentId().getId().equals(student.getId())) {
            throw new PermissionException("Bạn không có quyền sửa lộ trình này!");
        }

        if (reqDto.getTitle() != null) {
            lp.setTitle(reqDto.getTitle());
        }
        if (reqDto.getDescription() != null) {
            lp.setDescription(reqDto.getDescription());
        }
        if (reqDto.getGoal() != null) {
            lp.setGoal(reqDto.getGoal());
        }
        lp.setUpdatedAt(new Date());

        lp = this.learningPathRepo.addOrUpdateLearningPath(lp);

        ResLearningPathDTO dto = toDTO(lp);
        List<LearningPathItem> items = this.learningPathRepo.getItemsByPathId(lp.getId());
        dto.setItems(items.stream().map(this::toItemDTO).collect(Collectors.toList()));

        return dto;
    }

    @Override
    public void deleteLearningPath(String username, int pathId) {
        Student student = getStudent(username);
        LearningPath lp = this.learningPathRepo.getLearningPathById(pathId);

        if (lp == null || Boolean.TRUE.equals(lp.getIsDeleted())) {
            throw new IdInvalidException("Lộ trình không tồn tại!");
        }

        if (!lp.getStudentId().getId().equals(student.getId())) {
            throw new PermissionException("Bạn không có quyền xóa lộ trình này!");
        }

        this.learningPathRepo.deleteItemsByPathId(pathId);
        this.learningPathRepo.deleteLearningPath(pathId);
    }

    @Override
    public void deleteItem(String username, int itemId) {
        Student student = getStudent(username);
        LearningPathItem item = this.learningPathRepo.getItemById(itemId);

        if (item == null || Boolean.TRUE.equals(item.getIsDeleted())) {
            throw new IdInvalidException("Mục trong lộ trình không tồn tại!");
        }

        if (!item.getPathId().getStudentId().getId().equals(student.getId())) {
            throw new PermissionException("Bạn không có quyền xóa mục này!");
        }

        this.learningPathRepo.deleteItem(itemId);
    }

    private String buildStudentContext(Student student, User user, ReqGenerateLearningPathDTO reqDto) {
        StringBuilder sb = new StringBuilder();
        sb.append("Thông tin sinh viên:\n");
        sb.append("- Họ tên: ").append(user.getFullName()).append("\n");

        if (student.getEducationLevel() != null) {
            sb.append("- Trình độ học vấn: ").append(student.getEducationLevel().name()).append("\n");
        }
        if (student.getExperienceLevel() != null) {
            sb.append("- Mức kinh nghiệm: ").append(student.getExperienceLevel().name()).append("\n");
        }
        if (student.getLearningGoal() != null) {
            sb.append("- Mục tiêu học tập đã đặt: ").append(student.getLearningGoal()).append("\n");
        }
        if (reqDto != null && reqDto.getGoal() != null && !reqDto.getGoal().isBlank()) {
            sb.append("- Mục tiêu cụ thể cho lộ trình này: ").append(reqDto.getGoal()).append("\n");
        }
        if (reqDto != null && reqDto.getAdditionalInfo() != null && !reqDto.getAdditionalInfo().isBlank()) {
            sb.append("- Thông tin bổ sung: ").append(reqDto.getAdditionalInfo()).append("\n");
        }

        return sb.toString();
    }

    private String buildAvailableContent(List<Course> courses, List<Resource> resources) {
        StringBuilder sb = new StringBuilder();

        sb.append("\nDanh sách khóa học hiện có trên hệ thống:\n");
        for (Course c : courses) {
            if (c.getIsDeleted() != null && c.getIsDeleted()) continue;
            sb.append(String.format("- [COURSE_ID:%d] %s", c.getId(), c.getName()));
            if (c.getDescription() != null) {
                String desc = c.getDescription().length() > 100
                        ? c.getDescription().substring(0, 100) + "..."
                        : c.getDescription();
                sb.append(" | Mô tả: ").append(desc);
            }
            if (c.getTargetLevel() != null) {
                sb.append(" | Trình độ: ").append(c.getTargetLevel().name());
            }
            sb.append("\n");
        }

        sb.append("\nDanh sách tài liệu hiện có trên hệ thống:\n");
        for (Resource r : resources) {
            if (r.getIsDeleted() != null && r.getIsDeleted()) continue;
            sb.append(String.format("- [RESOURCE_ID:%d] %s", r.getId(), r.getTitle()));
            if (r.getDescription() != null) {
                String desc = r.getDescription().length() > 100
                        ? r.getDescription().substring(0, 100) + "..."
                        : r.getDescription();
                sb.append(" | Mô tả: ").append(desc);
            }
            if (r.getLevel() != null) {
                sb.append(" | Trình độ: ").append(r.getLevel().name());
            }
            if (r.getFormat() != null) {
                sb.append(" | Định dạng: ").append(r.getFormat().name());
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private String callXiaomiMiMoApi(String studentContext, String availableContent,
            ReqGenerateLearningPathDTO reqDto) {
        String apiKey = env.getProperty("xiaomimimo.api.key");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = env.getProperty("gemini.api.key");
        }

        System.out.println("[LearningPath] API Key loaded: " + (apiKey != null && !apiKey.isBlank() ? "YES (" + apiKey.substring(0, Math.min(8, apiKey.length())) + "...)" : "NO"));

        if (apiKey == null || apiKey.isBlank() || apiKey.contains("YOUR_")) {
            System.err.println("[LearningPath] No valid API key found, using fallback.");
            return buildFallbackResponse(reqDto);
        }

        String prompt = buildPrompt(studentContext, availableContent);

        try {
            String mimoUrl = "https://token-plan-sgp.xiaomimimo.com/v1/chat/completions";

            URL url = new URL(mimoUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("api-key", apiKey.trim());
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);

            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", "mimo-v2.5-pro",
                    "messages", List.of(
                            Map.of("role", "system", "content", "Bạn là một chuyên gia tư vấn giáo dục AI của hệ thống SmartEdu."),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.7
            ));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            String responseBody;

            if (responseCode == 200) {
                responseBody = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            } else {
                String errorBody = "";
                if (conn.getErrorStream() != null) {
                    errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                }
                System.err.println("[LearningPath] MiMo API error (HTTP " + responseCode + "): " + errorBody);
                return buildFallbackResponse(reqDto);
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null && message.has("content")) {
                    System.out.println("[LearningPath] Success with MiMo API! Content: " + message.get("content").asText());
                    return message.get("content").asText();
                }
            }

            return buildFallbackResponse(reqDto);

        } catch (Exception e) {
            System.err.println("MiMo API call failed: " + e.getMessage());
            e.printStackTrace();
            return buildFallbackResponse(reqDto);
        }
    }

    private String buildPrompt(String studentContext, String availableContent) {
        return """
                Bạn là một chuyên gia tư vấn giáo dục AI. Dựa trên thông tin sinh viên và danh sách khóa học/tài liệu có sẵn, 
                hãy tạo một lộ trình học tập cá nhân hóa, chi tiết.

                """ + studentContext + """
                
                """ + availableContent + """
                
                Yêu cầu:
                1. Phân tích trình độ và mục tiêu của sinh viên
                2. Đề xuất lộ trình học tập phù hợp bao gồm thứ tự học các khóa học và tài liệu
                3. Mỗi mục trong lộ trình phải tham chiếu đến ID của khóa học hoặc tài liệu thực tế trên hệ thống
                4. Sắp xếp từ cơ bản đến nâng cao

                QUAN TRỌNG: Trả lời theo ĐÚNG định dạng JSON sau (không thêm markdown code block):
                {
                  "title": "Tên lộ trình học tập",
                  "description": "Mô tả tổng quan về lộ trình, lý do phù hợp với sinh viên",
                  "items": [
                    {
                      "type": "COURSE hoặc RESOURCE",
                      "referenceId": <id_số_nguyên>,
                      "reason": "Lý do đề xuất mục này",
                      "isRequired": true/false
                    }
                  ]
                }

                Chỉ sử dụng các ID có trong danh sách trên. Đề xuất tối đa 10 mục.
                Trả lời CHỈCHỈ JSON, không thêm text giải thích hay markdown.
                """;
    }

    private String buildFallbackResponse(ReqGenerateLearningPathDTO reqDto) {
        String goal = reqDto != null && reqDto.getGoal() != null ? reqDto.getGoal() : "Phát triển kỹ năng";

        return String.format("""
                {
                  "title": "Lộ trình: %s",
                  "description": "Lộ trình học tập được đề xuất dựa trên mục tiêu của bạn. Hãy cập nhật MIMO API key để nhận lộ trình chi tiết từ AI.",
                  "items": []
                }
                """, goal);
    }

    private void parseAndSaveLearningPath(LearningPath lp, String aiResponse,
            List<Course> courses, List<Resource> resources) {
        try {
            String json = aiResponse.trim();
            if (json.startsWith("```json")) {
                json = json.substring(7);
            } else if (json.startsWith("```")) {
                json = json.substring(3);
            }
            if (json.endsWith("```")) {
                json = json.substring(0, json.length() - 3);
            }
            json = json.trim();

            JsonNode root = objectMapper.readTree(json);

            lp.setTitle(root.has("title") ? root.get("title").asText() : "Lộ trình học tập AI");
            lp.setDescription(root.has("description") ? root.get("description").asText() : "");

            lp = this.learningPathRepo.addOrUpdateLearningPath(lp);

            Map<Integer, String> courseMap = new HashMap<>();
            for (Course c : courses) {
                courseMap.put(c.getId(), c.getName());
            }
            Map<Integer, String> resourceMap = new HashMap<>();
            for (Resource r : resources) {
                resourceMap.put(r.getId(), r.getTitle());
            }

            if (root.has("items") && root.get("items").isArray()) {
                int order = 1;
                for (JsonNode itemNode : root.get("items")) {
                    String type = itemNode.has("type") ? itemNode.get("type").asText().toUpperCase() : "RESOURCE";
                    int refId = itemNode.has("referenceId") ? itemNode.get("referenceId").asInt() : 0;
                    boolean isRequired = !itemNode.has("isRequired") || itemNode.get("isRequired").asBoolean();

                    if ("COURSE".equals(type) && !courseMap.containsKey(refId)) continue;
                    if ("RESOURCE".equals(type) && !resourceMap.containsKey(refId)) continue;

                    PathItemTypeEnum itemType;
                    try {
                        itemType = PathItemTypeEnum.valueOf(type);
                    } catch (IllegalArgumentException e) {
                        itemType = PathItemTypeEnum.RESOURCE;
                    }

                    LearningPathItem item = new LearningPathItem();
                    item.setPathId(lp);
                    item.setItemType(itemType);
                    item.setReferenceId(refId);
                    item.setOrderNumber(order++);
                    item.setIsRequired(isRequired);
                    item.setIsDeleted(false);

                    this.learningPathRepo.addOrUpdateItem(item);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse AI response: " + e.getMessage());
            e.printStackTrace();
            lp.setTitle("Lộ trình học tập AI");
            lp.setDescription("Lộ trình được tạo bởi AI. Có lỗi khi phân tích kết quả, vui lòng thử lại.");
            this.learningPathRepo.addOrUpdateLearningPath(lp);
        }
    }

    private ResLearningPathDTO toDTO(LearningPath lp) {
        return DTOMapper.toResLearningPathDTO(lp);
    }

    private ResLearningPathItemDTO toItemDTO(LearningPathItem item) {
        return DTOMapper.toResLearningPathItemDTO(item, resolveReferenceName(item));
    }

    private String resolveReferenceName(LearningPathItem item) {
        if (item.getItemType() == null) return "Mục học tập";

        try {
            switch (item.getItemType()) {
                case COURSE:
                    Course c = this.courseRepo.getCourseById(item.getReferenceId());
                    return c != null ? c.getName() : "Khóa học #" + item.getReferenceId();
                case RESOURCE:
                    Resource r = this.resourceRepo.getResourceById(item.getReferenceId());
                    return r != null ? r.getTitle() : "Tài liệu #" + item.getReferenceId();
                case QUIZ:
                    return "Bài kiểm tra #" + item.getReferenceId();
                default:
                    return "Mục học tập #" + item.getReferenceId();
            }
        } catch (Exception e) {
            return "Mục #" + item.getReferenceId();
        }
    }
}

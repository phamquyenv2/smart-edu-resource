package com.paq.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;

import com.paq.pojo.AnswerOption;
import com.paq.pojo.ChatParticipant;
import com.paq.pojo.ChatRoom;
import com.paq.pojo.Course;
import com.paq.pojo.CourseLesson;
import com.paq.pojo.Enrollment;
import com.paq.pojo.ForumCategory;
import com.paq.pojo.ForumPost;
import com.paq.pojo.ForumThread;
import com.paq.pojo.Interaction;
import com.paq.pojo.InteractionReply;
import com.paq.pojo.LearningLog;
import com.paq.pojo.LearningPath;
import com.paq.pojo.LearningPathItem;
import com.paq.pojo.Lecturer;
import com.paq.pojo.Payment;
import com.paq.pojo.Question;
import com.paq.pojo.Quiz;
import com.paq.pojo.QuizAttempt;
import com.paq.pojo.Resource;
import com.paq.pojo.ResourceRelation;
import com.paq.pojo.ResourceTag;
import com.paq.pojo.ResourceType;
import com.paq.pojo.Student;
import com.paq.pojo.StudentAnswer;
import com.paq.pojo.Subject;
import com.paq.pojo.Topic;
import com.paq.pojo.User;
import com.paq.pojo.response.ResAnswerOptionDTO;
import com.paq.pojo.response.ResCategoryDTO;
import com.paq.pojo.response.ResChatParticipantDTO;
import com.paq.pojo.response.ResChatRoomDTO;
import com.paq.pojo.response.ResCourseChapterDTO;
import com.paq.pojo.response.ResCourseDTO;
import com.paq.pojo.response.ResCourseLearnDTO;
import com.paq.pojo.response.ResCourseLessonDTO;
import com.paq.pojo.response.ResEnrollmentDTO;
import com.paq.pojo.response.ResForumCategoryDTO;
import com.paq.pojo.response.ResForumPostDTO;
import com.paq.pojo.response.ResForumThreadDTO;
import com.paq.pojo.response.ResInteractionDTO;
import com.paq.pojo.response.ResInteractionReplyDTO;
import com.paq.pojo.response.ResLearningLogDTO;
import com.paq.pojo.response.ResLearningPathDTO;
import com.paq.pojo.response.ResLearningPathItemDTO;
import com.paq.pojo.response.ResLearningProgressDTO;
import com.paq.pojo.response.ResLecturerDTO;
import com.paq.pojo.response.ResPageDTO;
import com.paq.pojo.response.ResPaymentDTO;
import com.paq.pojo.response.ResQuestionDTO;
import com.paq.pojo.response.ResQuizAttemptDTO;
import com.paq.pojo.response.ResQuizDTO;
import com.paq.pojo.response.ResResourceDTO;
import com.paq.pojo.response.ResStudentAnswerResultDTO;
import com.paq.pojo.response.ResStudentDTO;
import com.paq.pojo.response.ResSubjectDTO;
import com.paq.pojo.response.ResUserDTO;

public class DTOMapper {

    public static <T> ResPageDTO<T> toResPageDTO(List<T> items, long totalItems, int page, int pageSize) {
        ResPageDTO<T> pageDTO = new ResPageDTO<>();
        pageDTO.setItems(items);
        pageDTO.setTotalItems(totalItems);
        pageDTO.setPage(page);
        pageDTO.setPageSize(pageSize);
        pageDTO.setTotalPages((int) Math.ceil((double) totalItems / pageSize));
        return pageDTO;
    }

    public static ResUserDTO toResUserDTO(User user) {
        if (user == null) {
            return null;
        }

        ResUserDTO dto = new ResUserDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setAvatar(user.getAvatar());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setIsActive(user.getIsActive());
        if (user.getStudent() != null) {
            dto.setStudentId(user.getStudent().getId());
        }
        if (user.getLecturer() != null) {
            dto.setLecturerId(user.getLecturer().getId());
            dto.setLecturerApproved(user.getLecturer().getIsApprove());
        }

        return dto;
    }

    public static ResLearningPathDTO toResLearningPathDTO(LearningPath lp) {
        if (lp == null) return null;
        
        ResLearningPathDTO dto = new ResLearningPathDTO();
        dto.setId(lp.getId());
        dto.setTitle(lp.getTitle());
        dto.setDescription(lp.getDescription());
        dto.setGeneratedByAi(lp.getGeneratedByAi());
        dto.setGoal(lp.getGoal());
        dto.setCreatedAt(lp.getCreatedAt());
        dto.setUpdatedAt(lp.getUpdatedAt());

        if (lp.getStudentId() != null) {
            dto.setStudentId(lp.getStudentId().getId());
            if (lp.getStudentId().getUserId() != null) {
                dto.setStudentName(lp.getStudentId().getUserId().getFullName());
            }
        }

        if (lp.getRecommendedBy() != null) {
            dto.setRecommendedByName(lp.getRecommendedBy().getFullName());
        }

        return dto;
    }

    public static ResLearningPathItemDTO toResLearningPathItemDTO(LearningPathItem item, String refName) {
        if (item == null) return null;
        
        ResLearningPathItemDTO dto = new ResLearningPathItemDTO();
        dto.setId(item.getId());
        dto.setItemType(item.getItemType() != null ? item.getItemType().name() : null);
        dto.setReferenceId(item.getReferenceId());
        dto.setOrderNumber(item.getOrderNumber());
        dto.setIsRequired(item.getIsRequired());

        dto.setReferenceName(refName);

        return dto;
    }

    public static ResStudentDTO toResStudentDTO(Student student) {
        if (student == null) {
            return null;
        }

        ResStudentDTO dto = new ResStudentDTO();
        dto.setId(student.getId());
        dto.setStudentCode(student.getStudentCode());
        dto.setDob(student.getDob());
        dto.setGender(student.getGender());
        dto.setExperienceLevel(student.getExperienceLevel() != null ? student.getExperienceLevel().name() : null);
        dto.setEducationLevel(student.getEducationLevel() != null ? student.getEducationLevel().name() : null);
        dto.setLearningGoal(student.getLearningGoal());
        dto.setUser(toResUserDTO(student.getUserId()));

        return dto;
    }

    public static ResLecturerDTO toResLecturerDTO(Lecturer lecturer) {
        if (lecturer == null) {
            return null;
        }

        ResLecturerDTO dto = new ResLecturerDTO();
        dto.setId(lecturer.getId());
        dto.setDegree(lecturer.getDegree() != null ? lecturer.getDegree().name() : null);
        dto.setCertificateUrl(lecturer.getCertificateUrl());
        dto.setSpecialization(lecturer.getSpecialization());
        dto.setBio(lecturer.getBio());
        dto.setIsApprove(lecturer.getIsApprove());
        dto.setApproveAt(lecturer.getApproveAt());
        dto.setUser(toResUserDTO(lecturer.getUserId()));

        return dto;
    }

    public static ResSubjectDTO toResSubjectDTO(Subject subject) {
        if (subject == null) {
            return null;
        }

        ResSubjectDTO dto = new ResSubjectDTO();
        dto.setId(subject.getId());
        dto.setCode(subject.getCode());
        dto.setName(subject.getName());
        dto.setDescription(subject.getDescription());
        dto.setCreatedAt(subject.getCreatedAt());

        return dto;
    }

    public static ResCategoryDTO toResCategoryDTO(Topic topic) {
        if (topic == null) {
            return null;
        }

        ResCategoryDTO dto = new ResCategoryDTO();
        dto.setId(topic.getId());
        dto.setName(topic.getName());

        return dto;
    }

    public static ResCategoryDTO toResCategoryDTO(ResourceType resourceType) {
        if (resourceType == null) {
            return null;
        }

        ResCategoryDTO dto = new ResCategoryDTO();
        dto.setId(resourceType.getId());
        dto.setName(resourceType.getName());

        return dto;
    }

    public static ResCategoryDTO toResCategoryDTO(ResourceTag resourceTag) {
        if (resourceTag == null) {
            return null;
        }

        ResCategoryDTO dto = new ResCategoryDTO();
        dto.setId(resourceTag.getId());
        dto.setName(resourceTag.getName());

        return dto;
    }

    public static ResForumCategoryDTO toResForumCategoryDTO(ForumCategory category) {
        if (category == null) {
            return null;
        }

        ResForumCategoryDTO dto = new ResForumCategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());

        return dto;
    }

    public static ResForumThreadDTO toResForumThreadDTO(ForumThread thread) {
        if (thread == null) {
            return null;
        }

        ResForumThreadDTO dto = new ResForumThreadDTO();
        dto.setId(thread.getId());
        dto.setTitle(thread.getTitle());
        dto.setContent(thread.getContent());
        dto.setIsLock(thread.getIsLock());
        dto.setCreatedAt(thread.getCreatedAt());
        dto.setUpdateAt(thread.getUpdateAt());
        dto.setCategory(toResForumCategoryDTO(thread.getCategoryId()));
        dto.setCreatedBy(toResUserDTO(thread.getCreatedBy()));

        return dto;
    }

    public static ResForumPostDTO toResForumPostDTO(ForumPost post) {
        if (post == null) {
            return null;
        }

        ResForumPostDTO dto = new ResForumPostDTO();
        dto.setId(post.getId());
        dto.setContent(post.getContent());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setThreadId(post.getThreadId() != null ? post.getThreadId().getId() : null);
        dto.setUser(toResUserDTO(post.getUserId()));

        return dto;
    }

    public static ResChatRoomDTO toResChatRoomDTO(ChatRoom room) {
        if (room == null) {
            return null;
        }

        ResChatRoomDTO dto = new ResChatRoomDTO();
        dto.setId(room.getId());
        dto.setType(room.getType() != null ? room.getType().name() : null);
        dto.setName(room.getName());
        dto.setCreatedAt(room.getCreatedAt());
        dto.setCreatedBy(toResUserDTO(room.getCreatedBy()));
        if (room.getCourseId() != null) {
            dto.setCourseId(room.getCourseId().getId());
            dto.setCourseName(room.getCourseId().getName());
        }

        return dto;
    }

    public static ResChatParticipantDTO toResChatParticipantDTO(ChatParticipant participant) {
        if (participant == null) {
            return null;
        }

        ResChatParticipantDTO dto = new ResChatParticipantDTO();
        dto.setId(participant.getId());
        dto.setJoinedAt(participant.getJoinedAt());
        dto.setIsMuted(participant.getIsMuted());
        dto.setRoomId(participant.getRoomId() != null ? participant.getRoomId().getId() : null);
        dto.setUser(toResUserDTO(participant.getUserId()));

        return dto;
    }

    public static ResCourseDTO toResCourseDTO(Course course) {
        if (course == null) {
            return null;
        }

        ResCourseDTO dto = new ResCourseDTO();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setDescription(course.getDescription());
        dto.setStartDate(course.getStartDate());
        dto.setEndDate(course.getEndDate());
        dto.setIsPaid(course.getIsPaid());
        dto.setPrice(course.getPrice());
        dto.setIsDeleted(course.getIsDeleted());
        dto.setTargetLevel(course.getTargetLevel() != null ? course.getTargetLevel().name() : null);
        dto.setCreatedBy(toResUserDTO(course.getCreatedBy()));
        if (course.getLecturerId() != null) {
            dto.setLecturerId(course.getLecturerId().getId());
            dto.setLecturerUser(toResUserDTO(course.getLecturerId().getUserId()));
        }

        if (course.getSubjectId() != null) {
            dto.setSubjectId(course.getSubjectId().getId());
            dto.setSubject(toResSubjectDTO(course.getSubjectId()));
        }

        /*if (course.getEnrollmentSet() != null) {
            dto.setEnrollmentCount(course.getEnrollmentSet().size());
        }*/
        dto.setEnrollmentCount(0);

        return dto;
    }

    public static ResResourceDTO toResResourceDTO(Resource resource) {
        if (resource == null) {
            return null;
        }

        ResResourceDTO dto = toResResourceBasicDTO(resource);
        dto.setDescription(resource.getDescription());
        dto.setThumbnailUrl(resource.getThumbnailUrl());
        dto.setFileSize(resource.getFileSize());
        dto.setCreatedAt(resource.getCreatedAt());
        dto.setUpdateAt(resource.getUpdateAt());
        dto.setPageCount(resource.getPageCount());
        dto.setIsDeleted(resource.getIsDeleted());
        dto.setUploadBy(toResUserDTO(resource.getUploadBy()));
        if (resource.getSubjectSet() != null) {
            dto.setSubjects(resource.getSubjectSet().stream()
                    .map(DTOMapper::toResSubjectDTO)
                    .collect(Collectors.toList()));
        }

        if (resource.getTopicSet() != null) {
            dto.setTopics(resource.getTopicSet().stream()
                    .map(DTOMapper::toResCategoryDTO)
                    .collect(Collectors.toList()));
        }

        if (resource.getResourceTagSet() != null) {
            dto.setTags(resource.getResourceTagSet().stream()
                    .map(DTOMapper::toResCategoryDTO)
                    .collect(Collectors.toList()));
        }

        if (resource.getResourceTypeSet() != null) {
            dto.setTypes(resource.getResourceTypeSet().stream()
                    .map(DTOMapper::toResCategoryDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public static ResResourceDTO toResResourceDTO(Resource resource, List<ResourceRelation> relations) {
        ResResourceDTO dto = toResResourceDTO(resource);
        if (dto != null && relations != null) {
            dto.setRelatedResources(relations.stream()
                    .map(ResourceRelation::getRelatedId)
                    .map(DTOMapper::toResResourceBasicDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public static ResResourceDTO toPublicResResourceDTO(Resource resource) {
        return redactResourceFileUrl(toResResourceDTO(resource));
    }

    public static ResResourceDTO toPublicResResourceDTO(Resource resource, List<ResourceRelation> relations) {
        return redactResourceFileUrl(toResResourceDTO(resource, relations));
    }

    private static ResResourceDTO redactResourceFileUrl(ResResourceDTO dto) {
        if (dto == null) {
            return null;
        }

        dto.setFileUrl(null);
        if (dto.getRelatedResources() != null) {
            dto.getRelatedResources().forEach(related -> related.setFileUrl(null));
        }

        return dto;
    }

    private static ResResourceDTO toResResourceBasicDTO(Resource resource) {
        if (resource == null) {
            return null;
        }

        ResResourceDTO dto = new ResResourceDTO();
        dto.setId(resource.getId());
        dto.setTitle(resource.getTitle());
        dto.setFileUrl(resource.getFileUrl());
        dto.setFormat(resource.getFormat() != null ? resource.getFormat().name() : null);
        dto.setLevel(resource.getLevel() != null ? resource.getLevel().name() : null);

        return dto;
    }

    public static ResEnrollmentDTO toResEnrollmentDTO(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }

        ResEnrollmentDTO dto = new ResEnrollmentDTO();
        dto.setId(enrollment.getId());
        dto.setEnrollDate(enrollment.getEnrollDate());
        dto.setOverallProgress(enrollment.getOverallProgress());
        dto.setStatus(enrollment.getStatus() != null ? enrollment.getStatus().name() : null);
        dto.setTotalStudyTime(enrollment.getTotalStudyTime());

        if (enrollment.getCourseId() != null) {
            dto.setCourseId(enrollment.getCourseId().getId());
            dto.setCourseName(enrollment.getCourseId().getName());
        }

        Student student = enrollment.getStudentId();
        if (student != null) {
            dto.setStudentId(student.getId());
            dto.setStudentCode(student.getStudentCode());
            dto.setUser(toResUserDTO(student.getUserId()));
        }

        return dto;
    }

    public static ResPaymentDTO toResPaymentDTO(Payment payment) {
        if (payment == null) {
            return null;
        }

        ResPaymentDTO dto = new ResPaymentDTO();
        dto.setId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null);
        dto.setStatus(payment.getStatus() != null ? payment.getStatus().name() : null);
        dto.setTransactionCode(payment.getTransactionCode());
        dto.setPaidAt(payment.getPaidAt());
        dto.setCreatedAt(payment.getCreatedAt());

        Enrollment enrollment = payment.getEnrollmentId();
        if (enrollment != null) {
            dto.setEnrollmentId(enrollment.getId());

            Course course = enrollment.getCourseId();
            if (course != null) {
                dto.setCourseId(course.getId());
                dto.setCourseName(course.getName());
            }

            Student student = enrollment.getStudentId();
            if (student != null) {
                dto.setStudentId(student.getId());
                dto.setStudentCode(student.getStudentCode());
                dto.setUser(toResUserDTO(student.getUserId()));
            }
        }

        return dto;
    }

    public static ResQuizDTO toResQuizDTO(Quiz quiz, boolean includeCorrectAnswers) {
        return toResQuizDTO(quiz, includeCorrectAnswers, true);
    }

    public static ResQuizDTO toResQuizDTO(Quiz quiz, boolean includeCorrectAnswers, boolean includeQuestions) {
        if (quiz == null) {
            return null;
        }

        ResQuizDTO dto = new ResQuizDTO();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setDurationMinutes(quiz.getDurationMinutes());
        dto.setTotalScore(quiz.getTotalScore());
        dto.setCreatedAt(quiz.getCreatedAt());

        if (quiz.getCourseId() != null) {
            dto.setCourseId(quiz.getCourseId().getId());
        }

        if (quiz.getCreatedBy() != null) {
            dto.setCreatedBy(toResUserDTO(quiz.getCreatedBy()));
        }

        if (quiz.getQuestionSet() != null) {
            dto.setQuestionCount((int) quiz.getQuestionSet().stream()
                    .filter(q -> !Boolean.TRUE.equals(q.getIsDeleted()))
                    .count());
        }

        if (includeQuestions && quiz.getQuestionSet() != null) {
            dto.setQuestions(quiz.getQuestionSet().stream()
                    .filter(q -> !Boolean.TRUE.equals(q.getIsDeleted()))
                    .map(q -> toResQuestionDTO(q, includeCorrectAnswers))
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public static ResQuestionDTO toResQuestionDTO(Question question, boolean includeCorrectAnswers) {
        if (question == null) {
            return null;
        }

        ResQuestionDTO dto = new ResQuestionDTO();
        dto.setId(question.getId());
        dto.setContent(question.getContent());
        dto.setScore(question.getScore());
        if (includeCorrectAnswers) {
            dto.setExplanation(question.getExplanation());
        }
        dto.setType(question.getType() != null ? question.getType().name() : null);
        dto.setQuizId(question.getQuizId() != null ? question.getQuizId().getId() : null);

        if (question.getAnswerOptionSet() != null) {
            List<ResAnswerOptionDTO> options = question.getAnswerOptionSet().stream()
                    .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted()))
                    .map(a -> toResAnswerOptionDTO(a, includeCorrectAnswers))
                    .collect(Collectors.toList());
            dto.setAnswers(options);
            dto.setOptions(options);
        }

        return dto;
    }

    public static ResAnswerOptionDTO toResAnswerOptionDTO(AnswerOption answer, boolean includeCorrectAnswer) {
        if (answer == null) {
            return null;
        }

        ResAnswerOptionDTO dto = new ResAnswerOptionDTO();
        dto.setId(answer.getId());
        dto.setContent(answer.getContent());
        if (includeCorrectAnswer) {
            dto.setIsCorrect(answer.getIsCorrect());
        }

        return dto;
    }

    public static ResQuizAttemptDTO toResQuizAttemptDTO(QuizAttempt attempt, boolean includeAnswers) {
        if (attempt == null) {
            return null;
        }

        ResQuizAttemptDTO dto = new ResQuizAttemptDTO();
        dto.setId(attempt.getId());
        dto.setStartedAt(attempt.getStartedAt());
        dto.setSubmittedAt(attempt.getSubmittedAt());
        dto.setScore(attempt.getScore());
        dto.setStatus(attempt.getStatus() != null ? attempt.getStatus().name() : null);

        Quiz quiz = attempt.getQuizId();
        if (quiz != null) {
            dto.setQuizId(quiz.getId());
            dto.setQuizTitle(quiz.getTitle());
            dto.setTotalScore(quiz.getTotalScore());
            if (quiz.getCourseId() != null) {
                dto.setCourseId(quiz.getCourseId().getId());
                dto.setCourseName(quiz.getCourseId().getName());
            }
        }

        Student student = attempt.getStudentId();
        if (student != null) {
            dto.setStudentId(student.getId());
            dto.setStudentCode(student.getStudentCode());
            dto.setStudentUser(toResUserDTO(student.getUserId()));
        }

        if (includeAnswers && attempt.getStudentAnswerSet() != null) {
            dto.setAnswers(attempt.getStudentAnswerSet().stream()
                    .map(DTOMapper::toResStudentAnswerResultDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public static ResStudentAnswerResultDTO toResStudentAnswerResultDTO(StudentAnswer answer) {
        if (answer == null) {
            return null;
        }

        ResStudentAnswerResultDTO dto = new ResStudentAnswerResultDTO();
        dto.setId(answer.getId());
        dto.setAnswerText(answer.getAnswerText());
        dto.setIsCorrect(answer.getIsCorrect());
        dto.setScore(answer.getScore());

        Question question = answer.getQuestionId();
        if (question != null) {
            dto.setQuestionId(question.getId());
            dto.setQuestionContent(question.getContent());
            dto.setQuestionScore(question.getScore());
            dto.setQuestionType(question.getType() != null ? question.getType().name() : null);
            dto.setExplanation(question.getExplanation());
            if (question.getAnswerOptionSet() != null) {
                dto.setCorrectOptions(question.getAnswerOptionSet().stream()
                        .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                        .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted()))
                        .map(a -> toResAnswerOptionDTO(a, true))
                        .collect(Collectors.toList()));
            }
        }

        AnswerOption selectedOption = answer.getOptionId();
        if (selectedOption != null) {
            dto.setSelectedOptionId(selectedOption.getId());
            dto.setSelectedOptionContent(selectedOption.getContent());
        }

        return dto;
    }

    public static ResLearningProgressDTO toResLearningProgressDTO(Enrollment enrollment) {
        return toResLearningProgressDTO(enrollment, null);
    }

    public static ResLearningProgressDTO toResLearningProgressDTO(Enrollment enrollment,
            List<ResQuizAttemptDTO> quizAttempts) {
        if (enrollment == null) {
            return null;
        }

        ResLearningProgressDTO dto = new ResLearningProgressDTO();
        dto.setEnrollmentId(enrollment.getId());
        dto.setEnrollDate(enrollment.getEnrollDate());
        dto.setOverallProgress(enrollment.getOverallProgress());
        dto.setStatus(enrollment.getStatus() != null ? enrollment.getStatus().name() : null);
        dto.setTotalStudyTime(enrollment.getTotalStudyTime());

        if (enrollment.getCourseId() != null) {
            dto.setCourseId(enrollment.getCourseId().getId());
            dto.setCourseName(enrollment.getCourseId().getName());
        }

        Student student = enrollment.getStudentId();
        if (student != null) {
            dto.setStudentId(student.getId());
            dto.setStudentCode(student.getStudentCode());
            dto.setStudentUser(toResUserDTO(student.getUserId()));
        }

        dto.setLecturerFeedback(enrollment.getLecturerFeedback());
        dto.setQuizAttempts(quizAttempts);

        return dto;
    }

    // ── CourseLesson ────────────────────────────────────────────────────────────
    public static ResCourseLessonDTO toResCourseLessonDTO(CourseLesson cl) {
        return toResCourseLessonDTO(cl, true);
    }

    public static ResCourseLessonDTO toResCourseLessonDTO(CourseLesson cl, boolean includeProtectedContent) {
        if (cl == null) {
            return null;
        }

        ResCourseLessonDTO dto = new ResCourseLessonDTO();
        dto.setId(cl.getId());
        dto.setTitle(cl.getTitle());
        dto.setChapterNum(cl.getChapterNum());
        dto.setLessonNum(cl.getLessonNum());
        dto.setIsFree(cl.getIsFree());

        Resource res = cl.getResourceId();
        if (res != null) {
            dto.setResourceTitle(res.getTitle());
            dto.setThumbnailUrl(res.getThumbnailUrl());
            dto.setPageCount(res.getPageCount());
            if (includeProtectedContent) {
                dto.setResourceId(res.getId());
                dto.setFileUrl(res.getFileUrl());
            }
            if (res.getFormat() != null) {
                String fmt = res.getFormat().name();
                dto.setFormat(fmt);
                dto.setItemType("MP4".equalsIgnoreCase(fmt) ? "VIDEO" : "DOCUMENT");
            }
        }

        Quiz quiz = cl.getQuizId();
        if (quiz != null) {
            dto.setQuizTitle(quiz.getTitle());
            dto.setDurationMinutes(quiz.getDurationMinutes());
            if (includeProtectedContent) {
                dto.setQuizId(quiz.getId());
            }
            if (quiz.getQuestionSet() != null && Hibernate.isInitialized(quiz.getQuestionSet())) {
                dto.setQuestionCount(quiz.getQuestionSet().size());
            }
            if (dto.getItemType() == null) {
                dto.setItemType("QUIZ");
            }
        }

        if (dto.getItemType() == null) {
            dto.setItemType("DOCUMENT");
        }

        return dto;
    }

    private static final SimpleDateFormat DATETIME_FORMAT
            = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static ResInteractionDTO toInteractionDTO(Interaction interaction) {
        if (interaction == null) {
            return null;
        }

        ResInteractionDTO dto = new ResInteractionDTO();

        dto.setId(interaction.getId());
        dto.setNote(interaction.getNote());
        dto.setSelectedText(interaction.getSelectedText());
        dto.setType(interaction.getType() != null ? interaction.getType().name() : null);
        dto.setPageNumber(interaction.getPageNumber());
        dto.setTimeOffsetSeconds(interaction.getTimeOffsetSeconds());
        dto.setPositionX(interaction.getPositionX());

        if (interaction.getCreatedAt() != null) {
            dto.setCreatedAt(DATETIME_FORMAT.format(interaction.getCreatedAt()));
        }

        if (interaction.getUpdatedAt() != null) {
            dto.setUpdatedAt(DATETIME_FORMAT.format(interaction.getUpdatedAt()));
        }

        if (interaction.getResourceId() != null) {
            dto.setResourceId(interaction.getResourceId().getId());
            dto.setResourceTitle(interaction.getResourceId().getTitle());
        }

        if (interaction.getUserId() != null) {
            dto.setUserId(interaction.getUserId().getId());
            dto.setUsername(interaction.getUserId().getUsername());
            dto.setFullName(interaction.getUserId().getFullName());
        }

        return dto;
    }

    public static ResCourseLearnDTO toResCourseLearnDTO(Course course, List<CourseLesson> lessons,
            boolean hasAccess, String enrollmentStatus) {
        if (course == null) {
            return null;
        }

        ResCourseLearnDTO dto = new ResCourseLearnDTO();
        dto.setCourseId(course.getId());
        dto.setCourseName(course.getName());
        dto.setDescription(course.getDescription());
        dto.setTargetLevel(course.getTargetLevel() != null ? course.getTargetLevel().name() : null);
        dto.setIsPaid(course.getIsPaid());
        dto.setHasAccess(hasAccess);
        dto.setEnrollmentStatus(enrollmentStatus);

        if (course.getLecturerId() != null) {
            User lecturerUser = course.getLecturerId().getUserId();
            if (lecturerUser != null) {
                dto.setLecturerName(lecturerUser.getFullName());
            }
            dto.setLecturerTitle(course.getLecturerId().getSpecialization());
        }

        List<ResCourseChapterDTO> chapters = toResCourseChapterList(lessons, hasAccess);
        dto.setChapters(chapters);
        dto.setTotalChapters(chapters.size());
        dto.setTotalLessons(lessons != null ? lessons.size() : 0);

        return dto;
    }

    public static List<ResCourseChapterDTO> toResCourseChapterList(List<CourseLesson> lessons, boolean hasAccess) {
        Map<Integer, List<ResCourseLessonDTO>> map = new LinkedHashMap<>();
        if (lessons != null) {
            for (CourseLesson cl : lessons) {
                boolean canAccessLesson = hasAccess || Boolean.TRUE.equals(cl.getIsFree());
                map.computeIfAbsent(cl.getChapterNum(), k -> new ArrayList<>())
                        .add(toResCourseLessonDTO(cl, canAccessLesson));
            }
        }

        List<ResCourseChapterDTO> chapters = new ArrayList<>();
        for (Map.Entry<Integer, List<ResCourseLessonDTO>> entry : map.entrySet()) {
            chapters.add(new ResCourseChapterDTO(
                    entry.getKey(),
                    "Chương " + entry.getKey(),
                    entry.getValue()
            ));
        }

        return chapters;
    }

    public static ResInteractionReplyDTO toInteractionReplyDTO(InteractionReply reply) {
        if (reply == null) {
            return null;
        }

        ResInteractionReplyDTO dto = new ResInteractionReplyDTO();

        dto.setId(reply.getId());
        dto.setContent(reply.getContent());

        if (reply.getInteractionId() != null) {
            dto.setInteractionId(reply.getInteractionId().getId());
        }

        if (reply.getUserId() != null) {
            dto.setUserId(reply.getUserId().getId());
            dto.setUsername(reply.getUserId().getUsername());
            dto.setFullName(reply.getUserId().getFullName());
        }

        return dto;
    }

    public static ResCourseDTO toCourseDTO(Course course) {
        return toResCourseDTO(course);
    }

    public static ResResourceDTO toResourceDTO(Resource resource) {
        return toResResourceDTO(resource);
    }

    public static ResEnrollmentDTO toEnrollmentDTO(Enrollment enrollment) {
        return toResEnrollmentDTO(enrollment);
    }

    public static ResLearningLogDTO toLearningLogDTO(LearningLog log) {
        if (log == null) {
            return null;
        }

        ResLearningLogDTO dto = new ResLearningLogDTO();

        dto.setId(log.getId());
        dto.setCompletionStatus(log.getCompletionStatus());

        if (log.getStartTime() != null) {
            dto.setStartTime(DATETIME_FORMAT.format(log.getStartTime()));
        }

        if (log.getEndTime() != null) {
            dto.setEndTime(DATETIME_FORMAT.format(log.getEndTime()));
        }

        if (log.getResourceId() != null) {
            dto.setResourceId(log.getResourceId().getId());
            dto.setResourceTitle(log.getResourceId().getTitle());
        }

        if (log.getEnrollmentId() != null) {
            dto.setEnrollmentId(log.getEnrollmentId().getId());
        }

        return dto;
    }
}

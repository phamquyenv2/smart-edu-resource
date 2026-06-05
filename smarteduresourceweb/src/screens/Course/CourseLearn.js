import { useContext, useEffect, useRef, useState } from "react";
import { Alert, Badge, Button, Col, Container, Form, Row } from "react-bootstrap";
import { Link, useNavigate, useParams } from "react-router-dom";

import { MyUserContext } from "../../configs/Context";
import MySpinner from "../../components/common/MySpinner";
import { authApis, endpoints } from "../../configs/Apis";


const CourseLearn = () => {
    const { id } = useParams();
    const [user] = useContext(MyUserContext);
    const nav = useNavigate();

    const [learnData, setLearnData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState("");

    // Selected lesson state
    const [activeLesson, setActiveLesson] = useState(null);
    const [expandedChapters, setExpandedChapters] = useState({});
    const [completedResources, setCompletedResources] = useState([]);
    const [completingResourceId, setCompletingResourceId] = useState(null);
    const [progressErr, setProgressErr] = useState("");

    // Tabs: 'notes' | 'group-chat' | 'dm'
    const [activeTab, setActiveTab] = useState("group-chat");

    // Group chat
    const [groupMsgs, setGroupMsgs] = useState([]);
    const [chatRooms, setChatRooms] = useState([]);
    const [groupRoom, setGroupRoom] = useState(null);
    const [dmRoom, setDmRoom] = useState(null);
    const [dmMsgs, setDmMsgs] = useState([]);
    const [dmInput, setDmInput] = useState("");
    const [chatInput, setChatInput] = useState("");
    const chatEndRef = useRef(null);

    const getData = (res) => {
        const raw = res.data?.data ?? res.data;

        if (Array.isArray(raw)) return raw;

        return raw?.items || raw?.content || raw?.data || [];
    };


    const normalizeMessage = (m) => {
        const senderName =
            m.sender?.fullName ||
            m.user?.fullName ||
            m.createdBy?.fullName ||
            m.senderName ||
            "Người dùng";

        const senderId =
            m.sender?.id ||
            m.user?.id ||
            m.createdBy?.id ||
            m.senderId;

        return {
            id: m.id,
            sender: senderName,
            isInstructor: m.sender?.role === "LECTURER" || m.user?.role === "LECTURER" || m.createdBy?.role === "LECTURER",
            isMine: senderId === user?.id,
            content: m.content,
            time: m.createdAt
                ? new Date(m.createdAt).toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" })
                : ""
        };
    };

    const hasCourseAccess = () => {
        return learnData?.hasAccess === true && learnData?.enrollmentStatus === "SUCCESS";
    };

    const showCourseAccessAlert = () => {
        alert("Bạn phải thuộc khóa học này thì mới có thể thảo luận hoặc nhắn tin riêng cho giảng viên của môn này.");
    };

    useEffect(() => {
        if (!user) {
            nav(`/login?next=/courses/${id}/learn`);
            return;
        }
        loadLearnPage();
    }, [id, user]);

    const loadLearnPage = async () => {
        setLoading(true);
        setErr("");

        try {
            const response = await authApis().get(endpoints['course-learn'](id));
            const data = response.data.data;
            setLearnData(data);
            await loadChatRooms(data);

            // Auto-expand first chapter and select first lesson
            if (data.chapters && data.chapters.length > 0) {
                const firstChapter = data.chapters[0];
                setExpandedChapters({ [firstChapter.chapterNum]: true });
                if (firstChapter.lessons && firstChapter.lessons.length > 0) {
                    setActiveLesson(firstChapter.lessons[0]);
                }
            }
        } catch (ex) {
            setErr(ex.response?.data?.message || "Không tìm thấy nội dung khóa học. Vui lòng thử lại.");
        } finally {
            setLoading(false);
        }
    };

    const loadChatRooms = async (courseData) => {
        setGroupRoom(null);
        setDmRoom(null);
        setGroupMsgs([]);
        setDmMsgs([]);
        if (!courseData?.hasAccess || courseData?.enrollmentStatus !== "SUCCESS") {
            return;
        }
        try {
            const res = await authApis().get(endpoints["chat-rooms"]);
            const rooms = getData(res);
            setChatRooms(rooms);

            const courseId = Number(id);

            let group = rooms.find(r =>
                Number(r.courseId) === courseId &&
                (r.type === "CLASS" || r.type === "GROUP" || r.roomType === "CLASS" || r.roomType === "GROUP")
            );

            if (!group) {
                const groupRes = await authApis().post(
                    endpoints["chat-class-room-by-course"](courseId)
                );

                group = groupRes.data.data;
            }

            setGroupRoom(group || null);

            if (group?.createdBy?.fullName) {
                setLearnData(prev => ({
                    ...prev,
                    lecturerName: group.createdBy.fullName,
                    lecturerTitle: group.createdBy.email
                }));
            }

            if (group?.id) {
                const msgRes = await authApis().get(endpoints["chat-messages"](group.id));
                setGroupMsgs(getData(msgRes).map(normalizeMessage));
            } else {
                setGroupMsgs([]);
            }

            let privateRoom = null;

            try {
                const dmRes = await authApis().post(
                    endpoints["chat-private-room-by-course"](courseId)
                );

                privateRoom = dmRes.data.data;
            } catch (dmErr) {
                console.error("Không tạo được phòng chat riêng", dmErr);
                privateRoom = null;
            }

            setDmRoom(privateRoom || null);

            if (privateRoom?.id) {
                const msgRes = await authApis().get(endpoints["chat-messages"](privateRoom.id));
                setDmMsgs(getData(msgRes).map(normalizeMessage));
            } else {
                setDmMsgs([]);
            }

        } catch (err) {
            console.error(err);
            setGroupMsgs([]);
            setDmMsgs([]);
            setGroupRoom(null);
            setDmRoom(null);
        }
    };

    const toggleChapter = (chapterNum) => {
        setExpandedChapters(prev => ({ ...prev, [chapterNum]: !prev[chapterNum] }));
    };

    const sendGroupMsg = async (e) => {
        e.preventDefault();

        if (!hasCourseAccess()) {
            showCourseAccessAlert();
            return;
        }

        if (!chatInput.trim()) return;

        if (!groupRoom?.id) {
            alert("Chưa có phòng thảo luận cho khóa học này.");
            return;
        }

        try {
            const res = await authApis().post(
                endpoints["chat-send-message"](groupRoom.id),
                {
                    content: chatInput.trim()
                }
            );

            const newMsg = normalizeMessage(res.data.data || res.data);

            setGroupMsgs(prev => [...prev, newMsg]);
            setChatInput("");

            setTimeout(() => chatEndRef.current?.scrollIntoView({ behavior: "smooth" }), 50);
        } catch (err) {
            console.error(err);
            alert("Gửi tin nhắn thất bại.");
        }
    };

    const sendDmMsg = async (e) => {
        e.preventDefault();

        if (!hasCourseAccess()) {
            showCourseAccessAlert();
            return;
        }

        if (!dmInput.trim()) return;

        if (!dmRoom?.id) {
            alert("Chưa có phòng nhắn tin riêng với giảng viên.");
            return;
        }

        try {
            const res = await authApis().post(
                endpoints["chat-send-message"](dmRoom.id),
                {
                    content: dmInput.trim()
                }
            );

            const newMsg = normalizeMessage(res.data.data || res.data);

            setDmMsgs(prev => [...prev, newMsg]);
            setDmInput("");

            setTimeout(() => chatEndRef.current?.scrollIntoView({ behavior: "smooth" }), 50);
        } catch (err) {
            console.error(err);
            alert("Gửi tin nhắn thất bại.");
        }
    };

    const getLessonIcon = (lesson) => {
        if (isLessonLocked(lesson)) return <i className="bi bi-lock-fill" />;
        if (lesson.itemType === "VIDEO") return <i className="bi bi-play-circle-fill" />;
        if (lesson.itemType === "QUIZ") return <i className="bi bi-pencil-square" />;
        return <i className="bi bi-file-earmark-text-fill" />;
    };

    const isLessonLocked = (lesson) => !lesson.isFree && !learnData?.hasAccess;

    const completeResource = async () => {
        if (!activeLesson?.resourceId || completingResourceId) return;

        setProgressErr("");
        setCompletingResourceId(activeLesson.resourceId);
        try {
            await authApis().post(endpoints['resource-complete'](activeLesson.resourceId));
            setCompletedResources(items => [...new Set([...items, activeLesson.resourceId])]);
        } catch (ex) {
            setProgressErr(ex.response?.data?.message || "Không thể cập nhật tiến độ học tập.");
        } finally {
            setCompletingResourceId(null);
        }
    };

    const getLessonTypeBadge = (lesson) => {
        if (lesson.itemType === "VIDEO") return <Badge bg="danger" className="cl-type-badge">Video</Badge>;
        if (lesson.itemType === "QUIZ") return <Badge bg="warning" text="dark" className="cl-type-badge">Quiz</Badge>;
        return <Badge bg="secondary" className="cl-type-badge">Tài liệu</Badge>;
    };

    const renderViewer = () => {
        if (!activeLesson) {
            return (
                <div className="cl-viewer-empty">
                    <div className="cl-viewer-empty-icon">▶</div>
                    <div>Chọn một bài học để bắt đầu</div>
                </div>
            );
        }

        if (isLessonLocked(activeLesson)) {
            return (
                <div className="cl-viewer-empty">
                    <div className="cl-viewer-empty-icon"><i className="bi bi-lock-fill" /></div>
                    <div>Nội dung này yêu cầu đăng ký và thanh toán thành công.</div>
                </div>
            );
        }

        if (activeLesson.itemType === "VIDEO") {
            return (
                <div className="cl-video-wrapper">
                    {activeLesson.fileUrl ? (
                        <video controls className="cl-video" src={activeLesson.fileUrl}>
                            Trình duyệt không hỗ trợ phát video.
                        </video>
                    ) : (
                        <div className="cl-viewer-empty">
                            <span>▶ {activeLesson.title}</span>
                            <p className="mt-2">Video chưa có sẵn</p>
                        </div>
                    )}
                </div>
            );
        }

        if (activeLesson.itemType === "QUIZ") {
            return (
                <div className="cl-quiz-card">
                    <div className="cl-quiz-icon">✎</div>
                    <h4>{activeLesson.quizTitle || activeLesson.title}</h4>
                    {activeLesson.durationMinutes && (
                        <p className="cl-quiz-meta">{activeLesson.durationMinutes} phút · {activeLesson.questionCount || "?"} câu hỏi</p>
                    )}
                    <Button
                        className="cl-quiz-start-btn"
                        onClick={() => nav(`/quizzes/${activeLesson.quizId}/take`)}
                    >
                        Bắt đầu làm bài
                    </Button>
                </div>
            );
        }

        // DOCUMENT
        return (
            <div className="cl-doc-viewer">
                {activeLesson.fileUrl ? (
                    <iframe
                        src={activeLesson.fileUrl}
                        className="cl-doc-iframe"
                        title={activeLesson.title}
                    />
                ) : (
                    <div className="cl-viewer-empty">
                        <span>📄 {activeLesson.title}</span>
                        <p className="mt-2">Tài liệu chưa có sẵn</p>
                    </div>
                )}
            </div>
        );
    };

    if (loading) return <MySpinner />;

    if (err) {
        return (
            <Container className="py-5">
                <Alert variant="danger">{err}</Alert>
                <Link to={`/courses/${id}`} className="btn btn-outline-primary">← Quay lại khóa học</Link>
            </Container>
        );
    }

    if (!learnData) return null;

    return (
        <div className="cl-page">
            {/* Mini Header */}
            <div className="cl-topbar">
                <div className="cl-topbar-left">
                    <Link to="/courses" className="cl-topbar-logo">SmartEdu</Link>
                    <span className="cl-topbar-sep">›</span>
                    <span className="cl-topbar-course">{learnData.courseName}</span>
                </div>
            </div>

            <div className="cl-layout">
                {/* LEFT: Curriculum Sidebar */}
                <div className="cl-sidebar">
                    <div className="cl-sidebar-header">
                        <div className="cl-sidebar-title">Nội dung khóa học</div>
                        <div className="cl-sidebar-meta">
                            {learnData.totalChapters} chương · {learnData.totalLessons} bài học
                        </div>
                    </div>

                    <div className="cl-curriculum">
                        {(learnData.chapters || []).map(chapter => (
                            <div key={chapter.chapterNum} className="cl-chapter">
                                <button
                                    className="cl-chapter-header"
                                    onClick={() => toggleChapter(chapter.chapterNum)}
                                >
                                    <span>{chapter.chapterTitle}</span>
                                    <span className="cl-chapter-toggle">
                                        {expandedChapters[chapter.chapterNum] ? '▲' : '▼'}
                                    </span>
                                </button>

                                {expandedChapters[chapter.chapterNum] && (
                                    <div className="cl-lessons">
                                        {(chapter.lessons || []).map(lesson => (
                                            <button
                                                key={lesson.id}
                                                className={`cl-lesson-btn ${activeLesson?.id === lesson.id ? 'active' : ''}`}
                                                onClick={() => setActiveLesson(lesson)}
                                            >
                                                <span className="cl-lesson-icon">{getLessonIcon(lesson)}</span>
                                                <span className="cl-lesson-name">{lesson.title}</span>
                                                {isLessonLocked(lesson) && (
                                                    <span className="badge bg-danger">Đã khóa</span>
                                                )}
                                                {lesson.isFree && (
                                                    <span className="cl-free-tag">Miễn phí</span>
                                                )}
                                            </button>
                                        ))}
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                </div>

                {/* RIGHT: Main content */}
                <div className="cl-main">
                    {/* Lesson title bar */}
                    {activeLesson && (
                        <div className="cl-lesson-bar">
                            <div className="cl-lesson-bar-left">
                                {getLessonTypeBadge(activeLesson)}
                                <span className="cl-lesson-bar-title">
                                    Chương {activeLesson.chapterNum} · Bài {activeLesson.lessonNum}: {activeLesson.title}
                                </span>
                            </div>
                            {activeLesson.resourceId && !isLessonLocked(activeLesson) && (
                                <Button
                                    className="cl-complete-btn"
                                    onClick={completeResource}
                                    disabled={completedResources.includes(activeLesson.resourceId)
                                        || completingResourceId === activeLesson.resourceId}
                                >
                                    {completedResources.includes(activeLesson.resourceId)
                                        ? "Đã hoàn thành"
                                        : completingResourceId === activeLesson.resourceId
                                            ? "Đang cập nhật..."
                                            : "Đánh dấu hoàn thành"}
                                </Button>
                            )}
                        </div>
                    )}
                    {progressErr && <Alert variant="danger" className="mb-0 rounded-0">{progressErr}</Alert>}

                    {/* Viewer area */}
                    <div className="cl-viewer">
                        {renderViewer()}
                    </div>

                    {/* Bottom tabs: Thảo luận nhóm | Nhắn tin GV | Ghi chú */}
                    <div className="cl-bottom">
                        <div className="cl-tab-bar">
                            {[
                                ["group-chat", `Thảo luận nhóm (${learnData.courseName?.split(' ').slice(0, 3).join(' ')})`],
                                ["dm", `Nhắn tin với ${learnData.lecturerName || 'Giảng viên'}`],
                            ].map(([key, label]) => (
                                <button
                                    key={key}
                                    className={`cl-tab-btn ${activeTab === key ? 'active' : ''}`}
                                    onClick={() => {
                                        if (!hasCourseAccess()) {
                                            showCourseAccessAlert();
                                            return;
                                        }
                                        setActiveTab(key);
                                    }}
                                >
                                    {label}
                                </button>
                            ))}
                        </div>

                        {/* Tab: Group Chat */}
                        {activeTab === "group-chat" && (
                            <div className="cl-chat-panel">
                                <div className="cl-messages">
                                    {groupMsgs.map(m => (
                                        <div key={m.id} className={`cl-msg-row ${m.isMine ? 'mine' : ''}`}>
                                            {!m.isMine && (
                                                <div className={`cl-msg-avatar ${m.isInstructor ? 'instructor' : ''}`}>
                                                    {m.sender.charAt(0)}
                                                </div>
                                            )}
                                            <div className="cl-msg-body">
                                                {!m.isMine && (
                                                    <span className={`cl-msg-sender ${m.isInstructor ? 'instructor' : ''}`}>
                                                        {m.sender}{m.isInstructor && ' · Giảng viên'}
                                                    </span>
                                                )}
                                                <div className={`cl-msg-bubble ${m.isMine ? 'mine' : ''}`}>
                                                    {m.content}
                                                    <span className="cl-msg-time">{m.time}</span>
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                    <div ref={chatEndRef} />
                                </div>
                                <Form className="cl-chat-input" onSubmit={sendGroupMsg}>
                                    <Form.Control
                                        type="text"
                                        placeholder="Gửi tin nhắn đến cả lớp..."
                                        value={chatInput}
                                        onChange={e => setChatInput(e.target.value)}
                                    />
                                    <Button type="submit" className="cl-chat-send">Gửi</Button>
                                </Form>
                            </div>
                        )}

                        {/* Tab: DM Instructor */}
                        {activeTab === "dm" && (
                            <div className="cl-chat-panel">
                                <div className="cl-messages">
                                    {dmMsgs.length === 0 ? (
                                        <div className="text-muted p-3">
                                            Chưa có tin nhắn với giảng viên.
                                        </div>
                                    ) : (
                                        dmMsgs.map(m => (
                                            <div key={m.id} className={`cl-msg-row ${m.isMine ? "mine" : ""}`}>
                                                {!m.isMine && (
                                                    <div className={`cl-msg-avatar ${m.isInstructor ? "instructor" : ""}`}>
                                                        {m.sender.charAt(0)}
                                                    </div>
                                                )}
                                                <div className="cl-msg-body">
                                                    {!m.isMine && (
                                                        <span className={`cl-msg-sender ${m.isInstructor ? "instructor" : ""}`}>
                                                            {m.sender}{m.isInstructor && " · Giảng viên"}
                                                        </span>
                                                    )}
                                                    <div className={`cl-msg-bubble ${m.isMine ? "mine" : ""}`}>
                                                        {m.content}
                                                        <span className="cl-msg-time">{m.time}</span>
                                                    </div>
                                                </div>
                                            </div>
                                        ))
                                    )}
                                    <div ref={chatEndRef} />
                                </div>

                                <Form className="cl-chat-input" onSubmit={sendDmMsg}>
                                    <Form.Control
                                        type="text"
                                        placeholder={`Gửi tin nhắn đến ${learnData.lecturerName || "giảng viên"}...`}
                                        value={dmInput}
                                        onChange={e => setDmInput(e.target.value)}
                                    />
                                    <Button type="submit" className="cl-chat-send">Gửi</Button>
                                </Form>
                            </div>
                        )}

                        {/* Tab: Notes */}
                        {activeTab === "notes" && (
                            <div className="cl-notes-panel">
                                <Form.Control
                                    as="textarea"
                                    rows={6}
                                    placeholder="Viết ghi chú cá nhân cho bài học này..."
                                    className="cl-notes-area"
                                />
                                <Button className="cl-notes-save mt-2">Lưu ghi chú</Button>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}

export default CourseLearn;

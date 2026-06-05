import { useContext, useEffect, useRef, useState } from "react";
import { Alert, Badge, Button, Form } from "react-bootstrap";
import { Link, useNavigate, useParams } from "react-router-dom";

import { MyUserContext } from "../../configs/Context";
import MySpinner from "../../components/common/MySpinner";
import { authApis, endpoints } from "../../configs/Apis";

const formatTime = (dateStr) => {
    if (!dateStr) return "";
    const d = new Date(dateStr);
    return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
};

const getDocViewerUrl = (fileUrl, format) => {
    if (!fileUrl) return null;
    const fmt = (format || "").toUpperCase();
    if (["PPTX", "DOCX", "XLSX"].includes(fmt)) {
        return `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(fileUrl)}`;
    }
    if (fmt === "PDF") return fileUrl;
    return fileUrl;
};

const CourseLearn = () => {
    const { id } = useParams();
    const [user] = useContext(MyUserContext);
    const nav = useNavigate();

    const [learnData, setLearnData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState("");

    const [activeLesson, setActiveLesson] = useState(null);
    const [expandedChapters, setExpandedChapters] = useState({});
    const [completedLessonIds, setCompletedLessonIds] = useState([]);
    const [completingResourceId, setCompletingResourceId] = useState(null);
    const [progressErr, setProgressErr] = useState("");

    const [activeTab, setActiveTab] = useState("group-chat");
    const [bottomCollapsed, setBottomCollapsed] = useState(false);

    const [chatRoomId, setChatRoomId] = useState(null);
    const [chatMessages, setChatMessages] = useState([]);
    // Group chat
    const [groupMsgs, setGroupMsgs] = useState([]);
    const [chatRooms, setChatRooms] = useState([]);
    const [groupRoom, setGroupRoom] = useState(null);
    const [dmRoom, setDmRoom] = useState(null);
    const [dmMsgs, setDmMsgs] = useState([]);
    const [dmInput, setDmInput] = useState("");
    const [chatInput, setChatInput] = useState("");
    const [chatLoading, setChatLoading] = useState(false);
    const chatEndRef = useRef(null);

    const [sidebarOpen, setSidebarOpen] = useState(true);
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
            const learnRes = await authApis().get(endpoints['course-learn'](id));
            const data = learnRes.data.data;
            setLearnData(data);
            await loadChatRooms(data);

            if (data.chapters && data.chapters.length > 0) {
                const expanded = {};
                const completedIds = [];
                data.chapters.forEach(ch => { expanded[ch.chapterNum] = true; });
                data.chapters.forEach(ch => {
                    (ch.lessons || []).forEach(lesson => {
                        if (lesson.completed) completedIds.push(lesson.id);
                    });
                });
                setExpandedChapters(expanded);
                setCompletedLessonIds(completedIds);

                for (const ch of data.chapters) {
                    const first = (ch.lessons || []).find(l => l.isFree || data.hasAccess);
                    if (first) { setActiveLesson(first); break; }
                }
            }

            try {
                const chatRes = await authApis().get(endpoints['chat-rooms']);
                const rooms = chatRes.data?.data?.result || chatRes.data?.data || [];
                const courseRoom = rooms.find(r => r.courseId === parseInt(id));
                if (courseRoom) {
                    setChatRoomId(courseRoom.id);
                    loadMessages(courseRoom.id);
                }
            } catch (chatEx) {
                console.warn("Chat rooms not available:", chatEx);
            }
        } catch (ex) {
            setErr(ex.response?.data?.message || "Không tìm thấy nội dung khóa học. Vui lòng thử lại.");
        } finally {
            setLoading(false);
        }
    };

    const loadMessages = async (roomId) => {
        setChatLoading(true);
        try {
            const res = await authApis().get(endpoints['chat-messages'](roomId));
            const msgs = res.data?.data?.result || res.data?.data || [];
            setChatMessages(Array.isArray(msgs) ? msgs : []);
            setTimeout(() => chatEndRef.current?.scrollIntoView({ behavior: 'smooth' }), 100);
        } catch (ex) {
            console.error("Failed to load messages:", ex);
        } finally {
            setChatLoading(false);
        }
    };

    const sendMessage = async (e) => {
        e.preventDefault();
        if (!chatInput.trim() || !chatRoomId) return;

        const msgText = chatInput;
        setChatInput("");

        try {
            await authApis().post(endpoints['chat-send-message'](chatRoomId), { content: msgText });
            loadMessages(chatRoomId);
        } catch (ex) {
            console.error("Failed to send message:", ex);
            setChatInput(msgText); 
    const loadChatRooms = async (courseData) => {
        try {
            const res = await authApis().get(endpoints["chat-rooms"]);
            const rooms = getData(res);
            setChatRooms(rooms);

            const courseId = Number(id);

            const group = rooms.find(r =>
                Number(r.courseId) === Number(id) &&
                (r.type === "CLASS" || r.type === "GROUP" || r.roomType === "CLASS" || r.roomType === "GROUP")
            );

            const dm = rooms.find(r =>
                (r.courseId === courseId || r.course?.id === courseId) &&
                (r.type === "DM" || r.roomType === "DM" || r.isGroup === false)
            );

            setGroupRoom(group || null);
            setDmRoom(dm || null);

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

            if (dm?.id) {
                const msgRes = await authApis().get(endpoints["chat-messages"](dm.id));
                setDmMsgs(getData(msgRes).map(normalizeMessage));
            } else {
                setDmMsgs([]);
            }
        } catch (err) {
            console.error(err);
            setGroupMsgs([]);
            setDmMsgs([]);
        }
    };

    const toggleChapter = (chapterNum) => {
        setExpandedChapters(prev => ({ ...prev, [chapterNum]: !prev[chapterNum] }));
    };

    const sendGroupMsg = async (e) => {
        e.preventDefault();

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

        if (!dmInput.trim()) return;

        if (!dmRoom?.id) {
            alert("Chưa có phòng nhắn tin với giảng viên.");
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

    const toggleChapter = (chapterNum) => {
        setExpandedChapters(prev => ({ ...prev, [chapterNum]: !prev[chapterNum] }));
    };

    const isLessonLocked = (lesson) => !lesson.isFree && !learnData?.hasAccess;

    const completeResource = async () => {
        if (!activeLesson?.resourceId || completingResourceId) return;
        setProgressErr("");
        setCompletingResourceId(activeLesson.resourceId);
        try {
            await authApis().post(endpoints['resource-complete'](activeLesson.resourceId));
            const updatedLesson = {
                ...activeLesson,
                resourceCompleted: true,
                completed: !activeLesson.quizId || activeLesson.quizCompleted
            };
            setActiveLesson(updatedLesson);
            setLearnData(data => ({
                ...data,
                chapters: (data.chapters || []).map(chapter => ({
                    ...chapter,
                    lessons: (chapter.lessons || []).map(lesson =>
                        lesson.id === activeLesson.id ? updatedLesson : lesson
                    )
                }))
            }));
            if (updatedLesson.completed) {
                setCompletedLessonIds(items => [...new Set([...items, activeLesson.id])]);
            }
        } catch (ex) {
            setProgressErr(ex.response?.data?.message || "Không thể cập nhật tiến độ học tập.");
        } finally {
            setCompletingResourceId(null);
        }
    };

    const getLessonIcon = (lesson) => {
        if (isLessonLocked(lesson)) return <i className="bi bi-lock-fill" />;
        if (lesson.itemType === "VIDEO") return <i className="bi bi-play-circle-fill" />;
        if (lesson.itemType === "QUIZ") return <i className="bi bi-pencil-square" />;
        const fmt = (lesson.format || "").toUpperCase();
        if (fmt === "PPTX") return <i className="bi bi-easel-fill" />;
        return <i className="bi bi-file-earmark-text-fill" />;
    };

    const getLessonTypeBadge = (lesson) => {
        if (lesson.resourceId && lesson.quizId) return <Badge bg="primary" className="cl-type-badge">Bài học + Quiz</Badge>;
        if (lesson.itemType === "VIDEO") return <Badge bg="danger" className="cl-type-badge">Video</Badge>;
        if (lesson.itemType === "QUIZ") return <Badge bg="warning" text="dark" className="cl-type-badge">Quiz</Badge>;
        const fmt = (lesson.format || "").toUpperCase();
        if (fmt === "PPTX") return <Badge bg="info" className="cl-type-badge">Slide</Badge>;
        if (fmt === "PDF") return <Badge bg="secondary" className="cl-type-badge">PDF</Badge>;
        return <Badge bg="secondary" className="cl-type-badge">Tài liệu</Badge>;
    };

    const renderQuizPanel = (lesson) => {
        if (!lesson?.quizId) return null;

        return (
            <div className="cl-inline-quiz">
                <div className="cl-inline-quiz-main">
                    <div className="cl-inline-quiz-icon">
                        <i className="bi bi-pencil-square" />
                    </div>
                    <div>
                        <h5>{lesson.quizTitle || "Bài kiểm tra"}</h5>
                        <div className="cl-quiz-meta">
                            {lesson.durationMinutes && <span><i className="bi bi-clock me-1" />{lesson.durationMinutes} phút</span>}
                            {lesson.questionCount && <span><i className="bi bi-list-check me-1" />{lesson.questionCount} câu hỏi</span>}
                            {lesson.quizCompleted && <span><i className="bi bi-check-circle me-1" />Đã hoàn thành</span>}
                        </div>
                    </div>
                </div>
                <Button className="cl-quiz-start-btn" onClick={() => nav(`/quizzes/${lesson.quizId}/take`)}>
                    <i className="bi bi-play-fill me-1" />{lesson.quizCompleted ? "Làm lại" : "Bắt đầu làm bài"}
                </Button>
            </div>
        );
    };

    const getCompletedCount = () => {
        if (!learnData?.chapters) return 0;
        let count = 0;
        learnData.chapters.forEach(ch => {
            (ch.lessons || []).forEach(l => {
                if (completedLessonIds.includes(l.id)) count++;
            });
        });
        return count;
    };

    const renderViewer = () => {
        if (!activeLesson) {
            return (
                <div className="cl-viewer-empty">
                    <i className="bi bi-collection-play cl-viewer-empty-icon" />
                    <div>Chọn một bài học để bắt đầu</div>
                </div>
            );
        }

        if (isLessonLocked(activeLesson)) {
            return (
                <div className="cl-viewer-empty">
                    <i className="bi bi-lock-fill cl-viewer-empty-icon" />
                    <div className="fw-semibold">Bài học bị khóa</div>
                    <div className="cl-viewer-hint">Nội dung này yêu cầu đăng ký và thanh toán thành công.</div>
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
                            <i className="bi bi-play-circle cl-viewer-empty-icon" />
                            <div>{activeLesson.title}</div>
                            <div className="cl-viewer-hint">Video chưa có sẵn</div>
                        </div>
                    )}
                    {renderQuizPanel(activeLesson)}
                </div>
            );
        }

        if (activeLesson.itemType === "QUIZ") {
            return (
                <div className="cl-quiz-card">
                    <div className="cl-quiz-icon-wrap">
                        <i className="bi bi-pencil-square" />
                    </div>
                    <h4>{activeLesson.quizTitle || activeLesson.title}</h4>
                    {(activeLesson.durationMinutes || activeLesson.questionCount) && (
                        <div className="cl-quiz-meta">
                            {activeLesson.durationMinutes && <span><i className="bi bi-clock me-1" />{activeLesson.durationMinutes} phút</span>}
                            {activeLesson.questionCount && <span><i className="bi bi-list-check me-1" />{activeLesson.questionCount} câu hỏi</span>}
                        </div>
                    )}
                    <Button className="cl-quiz-start-btn" onClick={() => nav(`/quizzes/${activeLesson.quizId}/take`)}>
                        <i className="bi bi-play-fill me-1" />Bắt đầu làm bài
                    </Button>
                </div>
            );
        }

        // DOCUMENT / SLIDE viewer
        const viewerUrl = getDocViewerUrl(activeLesson.fileUrl, activeLesson.format);
        if (viewerUrl) {
            return (
                <div className="cl-lesson-stack">
                    <div className="cl-doc-viewer">
                        <iframe
                            src={viewerUrl}
                            className="cl-doc-iframe"
                            title={activeLesson.title}
                            allowFullScreen
                        />
                    </div>
                    {renderQuizPanel(activeLesson)}
                </div>
            );
        }

        return (
            <div className="cl-viewer-empty">
                <i className="bi bi-file-earmark-x cl-viewer-empty-icon" />
                <div>{activeLesson.title}</div>
                <div className="cl-viewer-hint">Tài liệu chưa có sẵn</div>
            </div>
        );
    };

    if (loading) return <MySpinner />;

    if (err) {
        return (
            <div className="cl-error-page">
                <Alert variant="danger">{err}</Alert>
                <Link to={`/courses/${id}`} className="btn btn-outline-primary">
                    <i className="bi bi-arrow-left me-1" />Quay lại khóa học
                </Link>
            </div>
        );
    }

    if (!learnData) return null;

    const progress = learnData.totalLessons > 0
        ? Math.round((getCompletedCount() / learnData.totalLessons) * 100)
        : 0;

    return (
        <div className="cl-page">
            {/* ── Topbar ── */}
            <div className="cl-topbar">
                <div className="cl-topbar-left">
                    <Link to="/courses" className="cl-topbar-logo">SmartEdu</Link>
                    <span className="cl-topbar-sep">›</span>
                    <span className="cl-topbar-course">{learnData.courseName}</span>
                </div>
                <div className="cl-topbar-right">
                    <div className="cl-progress-mini">
                        <div className="cl-progress-mini-bar">
                            <div className="cl-progress-mini-fill" style={{ width: `${progress}%` }} />
                        </div>
                        <span className="cl-progress-mini-text">{progress}%</span>
                    </div>
                </div>
            </div>

            <div className="cl-layout">
                {/* ── LEFT: Curriculum Sidebar ── */}
                <div className={`cl-sidebar ${sidebarOpen ? '' : 'collapsed'}`}>
                    <div className="cl-sidebar-header">
                        <div className="cl-sidebar-title">
                            <i className="bi bi-journal-text me-2" />Nội dung khóa học
                        </div>
                        <div className="cl-sidebar-meta">
                            {learnData.totalChapters} chương · {learnData.totalLessons} bài học
                            {learnData.hasAccess && <Badge bg="success" className="ms-2" style={{ fontSize: '0.65rem' }}>Đã đăng ký</Badge>}
                        </div>
                    </div>

                    <div className="cl-curriculum">
                        {(learnData.chapters || []).map((chapter, chIdx) => (
                            <div key={chapter.chapterNum} className="cl-chapter">
                                <button
                                    className="cl-chapter-header"
                                    onClick={() => toggleChapter(chapter.chapterNum)}
                                >
                                    <div className="cl-chapter-info">
                                        <span className="cl-chapter-name">{chapter.chapterTitle}</span>
                                        <span className="cl-chapter-count">{(chapter.lessons || []).length} bài học</span>
                                    </div>
                                    <i className={`bi bi-chevron-${expandedChapters[chapter.chapterNum] ? 'up' : 'down'} cl-chapter-toggle`} />
                                </button>

                                {expandedChapters[chapter.chapterNum] && (
                                    <div className="cl-lessons">
                                        {(chapter.lessons || []).map(lesson => {
                                            const isActive = activeLesson?.id === lesson.id;
                                            const isCompleted = completedLessonIds.includes(lesson.id);
                                            const locked = isLessonLocked(lesson);

                                            return (
                                                <button
                                                    key={lesson.id}
                                                    className={`cl-lesson-btn ${isActive ? 'active' : ''} ${locked ? 'locked' : ''} ${isCompleted ? 'completed' : ''}`}
                                                    onClick={() => !locked && setActiveLesson(lesson)}
                                                    disabled={locked}
                                                >
                                                    <span className="cl-lesson-icon">{getLessonIcon(lesson)}</span>
                                                    <div className="cl-lesson-info">
                                                        <span className="cl-lesson-name">{lesson.title}</span>
                                                        <span className="cl-lesson-detail">
                                                            {lesson.itemType === "QUIZ" && lesson.questionCount && `${lesson.questionCount} câu`}
                                                            {lesson.itemType === "VIDEO" && "Video"}
                                                            {lesson.itemType === "DOCUMENT" && (lesson.format || "Tài liệu")}
                                                            {lesson.pageCount && ` · ${lesson.pageCount} trang`}
                                                        </span>
                                                    </div>
                                                    {locked && <i className="bi bi-lock-fill cl-lesson-lock" />}
                                                    {lesson.isFree && <span className="cl-free-tag">Miễn phí</span>}
                                                    {isCompleted && <i className="bi bi-check-circle-fill cl-lesson-done" />}
                                                </button>
                                            );
                                        })}
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                </div>

                <button className="cl-sidebar-toggle" onClick={() => setSidebarOpen(p => !p)} title={sidebarOpen ? 'Ẩn sidebar' : 'Hiện sidebar'}>
                    <i className={`bi bi-chevron-${sidebarOpen ? 'left' : 'right'}`} />
                </button>

                <div className="cl-main">
                    {activeLesson && (
                        <div className="cl-lesson-bar">
                            <div className="cl-lesson-bar-left">
                                {getLessonTypeBadge(activeLesson)}
                                <span className="cl-lesson-bar-title">
                                    Chương {activeLesson.chapterNum} · Bài {activeLesson.lessonNum}: {activeLesson.title}
                                </span>
                            </div>
                            <div className="cl-lesson-bar-right">
                                {activeLesson.resourceId && !isLessonLocked(activeLesson) && (
                                    <Button
                                        className={`cl-complete-btn ${activeLesson.resourceCompleted ? 'done' : ''}`}
                                        onClick={completeResource}
                                        disabled={activeLesson.resourceCompleted
                                            || completingResourceId === activeLesson.resourceId}
                                    >
                                        {activeLesson.resourceCompleted
                                            ? <><i className="bi bi-check-circle-fill me-1" />Đã hoàn thành</>
                                            : completingResourceId === activeLesson.resourceId
                                                ? <><i className="bi bi-arrow-repeat me-1 spin" />Đang cập nhật...</>
                                                : <><i className="bi bi-check2 me-1" />Đánh dấu hoàn thành</>}
                                    </Button>
                                )}
                            </div>
                        </div>
                    )}
                    {progressErr && <Alert variant="danger" className="mb-0 rounded-0 py-2" style={{ fontSize: '0.82rem' }}>{progressErr}</Alert>}

                    <div className="cl-viewer">
                        {renderViewer()}
                    </div>

                    <div className={`cl-bottom ${bottomCollapsed ? 'collapsed' : ''}`}>
                        <div className="cl-bottom-header">
                            <div className="cl-tab-bar">
                                <button
                                    className={`cl-tab-btn ${activeTab === 'group-chat' ? 'active' : ''}`}
                                    onClick={() => { setActiveTab("group-chat"); setBottomCollapsed(false); }}
                                >
                                    <i className="bi bi-chat-dots me-1" />Thảo luận nhóm
                                </button>
                                <button
                                    className={`cl-tab-btn ${activeTab === 'dm' ? 'active' : ''}`}
                                    onClick={() => { setActiveTab("dm"); setBottomCollapsed(false); }}
                                >
                                    <i className="bi bi-person-lines-fill me-1" />Giảng viên
                                </button>
                            </div>
                            <button className="cl-bottom-toggle" onClick={() => setBottomCollapsed(p => !p)}>
                                <i className={`bi bi-chevron-${bottomCollapsed ? 'up' : 'down'}`} />
                            </button>
                        </div>

                        {!bottomCollapsed && (
                            <div className="cl-bottom-content">
                                {activeTab === "group-chat" && (
                                    <div className="cl-chat-panel">
                                        <div className="cl-messages">
                                            {chatLoading && (
                                                <div className="cl-chat-loading">
                                                    <i className="bi bi-arrow-repeat spin" /> Đang tải tin nhắn...
                                                </div>
                                            )}
                                            {!chatLoading && chatMessages.length === 0 && chatRoomId && (
                                                <div className="cl-chat-empty">
                                                    <i className="bi bi-chat-square-text" />
                                                    <span>Chưa có tin nhắn nào. Hãy bắt đầu thảo luận!</span>
                                                </div>
                                            )}
                                            {!chatLoading && !chatRoomId && (
                                                <div className="cl-chat-empty">
                                                    <i className="bi bi-chat-square-dots" />
                                                    <span>Chưa có phòng thảo luận cho khóa học này.</span>
                                                </div>
                                            )}
                                            {chatMessages.map(m => {
                                                const isMine = m.senderId === user?.id;
                                                return (
                                                    <div key={m.id} className={`cl-msg-row ${isMine ? 'mine' : ''}`}>
                                                        {!isMine && (
                                                            <div className="cl-msg-avatar">
                                                                {(m.senderName || "?").charAt(0).toUpperCase()}
                                                            </div>
                                                        )}
                                                        <div className="cl-msg-body">
                                                            {!isMine && (
                                                                <span className="cl-msg-sender">{m.senderName}</span>
                                                            )}
                                                            <div className={`cl-msg-bubble ${isMine ? 'mine' : ''}`}>
                                                                <span className="cl-msg-text">{m.content}</span>
                                                                <span className="cl-msg-time">{formatTime(m.sentAt)}</span>
                                                            </div>
                                                        </div>
                                                    </div>
                                                );
                                            })}
                                            <div ref={chatEndRef} />
                                        </div>
                                        {chatRoomId && (
                                            <Form className="cl-chat-input" onSubmit={sendMessage}>
                                                <Form.Control
                                                    type="text"
                                                    placeholder="Gửi tin nhắn đến cả lớp..."
                                                    value={chatInput}
                                                    onChange={e => setChatInput(e.target.value)}
                                                />
                                                <Button type="submit" className="cl-chat-send" disabled={!chatInput.trim()}>
                                                    <i className="bi bi-send-fill" />
                                                </Button>
                                            </Form>
                                        )}
                                    </div>
                                )}
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

                                {activeTab === "dm" && (
                                    <div className="cl-dm-panel">
                                        <div className="cl-dm-card">
                                            <div className="cl-dm-avatar">
                                                {(learnData.lecturerName || "G").charAt(0).toUpperCase()}
                                            </div>
                                            <div className="cl-dm-info">
                                                <div className="cl-dm-name">{learnData.lecturerName || "Giảng viên"}</div>
                                                {learnData.lecturerTitle && (
                                                    <div className="cl-dm-title">{learnData.lecturerTitle}</div>
                                                )}
                                            </div>
                                            <Button className="cl-dm-open-btn" onClick={() => nav('/chat')}>
                                                <i className="bi bi-chat-left-text me-1" />Nhắn tin riêng
                                            </Button>
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}

export default CourseLearn;

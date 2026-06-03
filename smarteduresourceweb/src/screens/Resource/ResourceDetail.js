import { useContext, useEffect, useState } from "react";
import { Alert, Badge, Button, Col, Container, Form, Row } from "react-bootstrap";
import { Link, useNavigate, useParams } from "react-router-dom";

import MySpinner from "../../components/common/MySpinner";
import ResourceCard from "../../components/common/ResourceCard";
import { MyUserContext } from "../../configs/Context";
import Apis, { authApis, endpoints } from "../../configs/Apis";

const COMMENT_TYPE = "COMMENT";
const NOTE_TYPE = "ANNOTATION";

const formatDate = (date) => {
    if (!date) return "";
    return new Date(date).toLocaleDateString("vi-VN");
};

const getFormatLabel = (format) => format || "RESOURCE";

const formatFileSize = (size) => {
    if (!size) return "";
    if (size < 1024) return `${size} B`;
    if (size < 1024 * 1024) return `${Math.round(size / 1024)} KB`;
    return `${Math.round(size / 1024 / 1024)} MB`;
};

const levelVariant = (level) => {
    switch (level) {
        case "BEGINNER":
            return "success";
        case "INTERMEDIATE":
            return "warning";
        case "ADVANCED":
            return "danger";
        default:
            return "secondary";
    }
};

const formatLevel = (level) => {
    switch (level) {
        case "BEGINNER":
            return "Cơ bản";
        case "INTERMEDIATE":
            return "Trung bình";
        case "ADVANCED":
            return "Nâng cao";
        default:
            return "Không rõ";
    }
};

const getInitial = (name) => (name || "?").trim().charAt(0).toUpperCase();

const ResourceDetail = () => {
    const { id } = useParams();
    const [user] = useContext(MyUserContext);
    const nav = useNavigate();

    const [resource, setResource] = useState(null);
    const [relatedResources, setRelatedResources] = useState([]);
    const [interactions, setInteractions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState("");
    const [actionErr, setActionErr] = useState("");
    const [commentText, setCommentText] = useState("");
    const [replyText, setReplyText] = useState("");
    const [replyingTo, setReplyingTo] = useState(null);
    const [repliesByComment, setRepliesByComment] = useState({});
    const [noteText, setNoteText] = useState("");
    const [editingNoteId, setEditingNoteId] = useState(null);
    const [activeTab, setActiveTab] = useState("desc");

    const comments = interactions.filter(i => i.type === COMMENT_TYPE);
    const notes = user
        ? interactions.filter(i => i.type === NOTE_TYPE && i.userId === user.id)
        : [];

    useEffect(() => {
        const loadResource = async () => {
            setLoading(true);
            setErr("");
            setActionErr("");

            try {
                const [resourceRes, relatedRes, interactionsRes] = await Promise.all([
                    Apis.get(endpoints["resource-detail"](id)),
                    Apis.get(endpoints["resource-related"](id)),
                    Apis.get(endpoints["resource-interactions"](id))
                ]);

                setResource(resourceRes.data.data);
                setRelatedResources(relatedRes.data.data || []);
                setInteractions(interactionsRes.data.data || []);
            } catch (ex) {
                console.error(ex);
                setErr(ex.response?.data?.message || "Không tìm thấy tài liệu.");
            } finally {
                setLoading(false);
            }
        };

        loadResource();
    }, [id]);

    useEffect(() => {
        const loadReplies = async () => {
            const currentComments = interactions.filter(i => i.type === COMMENT_TYPE);

            if (currentComments.length === 0) {
                setRepliesByComment({});
                return;
            }

            try {
                const replyResponses = await Promise.all(
                    currentComments.map(comment => Apis.get(endpoints["interaction-replies"](comment.id)))
                );

                const nextReplies = {};
                replyResponses.forEach((res, index) => {
                    nextReplies[currentComments[index].id] = res.data.data || [];
                });
                setRepliesByComment(nextReplies);
            } catch (ex) {
                console.error(ex);
            }
        };

        loadReplies();
    }, [interactions]);

    const requireLogin = () => {
        if (!user) {
            nav(`/login?next=/resources/${id}`);
            return false;
        }
        return true;
    };

    const createInteraction = async (payload) => {
        if (!requireLogin()) return null;
        const res = await authApis().post(endpoints["resource-interactions-secure"](id), payload);
        return res.data.data;
    };

    const handleComment = async (e) => {
        e.preventDefault();
        if (!commentText.trim()) return;

        setActionErr("");
        try {
            const created = await createInteraction({
                note: commentText.trim(),
                type: COMMENT_TYPE
            });
            if (created) {
                setInteractions(prev => [created, ...prev]);
                setCommentText("");
            }
        } catch (ex) {
            console.error(ex);
            setActionErr(ex.response?.data?.message || "Không thể gửi bình luận.");
        }
    };

    const handleReply = async (e, interactionId) => {
        e.preventDefault();
        if (!replyText.trim()) return;
        if (!requireLogin()) return;

        setActionErr("");
        try {
            const res = await authApis().post(endpoints["interaction-replies-secure"](interactionId), {
                content: replyText.trim()
            });
            setRepliesByComment(prev => ({
                ...prev,
                [interactionId]: [...(prev[interactionId] || []), res.data.data]
            }));
            setReplyText("");
            setReplyingTo(null);
        } catch (ex) {
            console.error(ex);
            setActionErr(ex.response?.data?.message || "Không thể gửi phản hồi.");
        }
    };

    const handleDeleteReply = async (replyId, interactionId) => {
        if (!requireLogin()) return;
        if (!window.confirm("Xóa phản hồi này?")) return;

        setActionErr("");
        try {
            await authApis().delete(endpoints["interaction-reply-detail"](replyId));
            setRepliesByComment(prev => ({
                ...prev,
                [interactionId]: (prev[interactionId] || []).filter(reply => reply.id !== replyId)
            }));
        } catch (ex) {
            console.error(ex);
            setActionErr(ex.response?.data?.message || "Không thể xóa phản hồi.");
        }
    };

    const handleSaveNote = async (e) => {
        e.preventDefault();
        if (!noteText.trim()) return;

        setActionErr("");
        try {
            if (editingNoteId) {
                const res = await authApis().put(endpoints["student-interaction-detail"](editingNoteId), {
                    note: noteText.trim(),
                    type: NOTE_TYPE
                });
                setInteractions(prev => prev.map(item => item.id === editingNoteId ? res.data.data : item));
            } else {
                const created = await createInteraction({
                    note: noteText.trim(),
                    type: NOTE_TYPE
                });
                if (created)
                    setInteractions(prev => [created, ...prev]);
            }

            setNoteText("");
            setEditingNoteId(null);
        } catch (ex) {
            console.error(ex);
            setActionErr(ex.response?.data?.message || "Không thể lưu ghi chú.");
        }
    };

    const handleEditNote = (note) => {
        setEditingNoteId(note.id);
        setNoteText(note.note || "");
    };

    const handleDeleteInteraction = async (interactionId) => {
        if (!requireLogin()) return;
        if (!window.confirm("Xóa nội dung này?")) return;

        setActionErr("");
        try {
            await authApis().delete(endpoints["student-interaction-detail"](interactionId));
            setInteractions(prev => prev.filter(item => item.id !== interactionId));
            if (editingNoteId === interactionId) {
                setEditingNoteId(null);
                setNoteText("");
            }
        } catch (ex) {
            console.error(ex);
            setActionErr(ex.response?.data?.message || "Không thể xóa nội dung.");
        }
    };

    const openResourceFile = () => {
        if (!resource?.fileUrl) return;
        window.open(resource.fileUrl, "_blank", "noopener,noreferrer");
    };

    const renderViewer = () => {
        const format = (resource?.format || "").toUpperCase();

        if (resource?.fileUrl && format === "PDF") {
            return (
                <iframe
                    src={resource.fileUrl}
                    className="cl-doc-iframe"
                    title={resource.title}
                    style={{ minHeight: 360 }}
                />
            );
        }

        if (resource?.fileUrl && ["MP4", "VIDEO"].includes(format)) {
            return (
                <video controls className="cl-video" src={resource.fileUrl}>
                    Trình duyệt không hỗ trợ phát video.
                </video>
            );
        }

        if (resource?.thumbnailUrl) {
            return <img src={resource.thumbnailUrl} alt={resource.title} className="rd-viewer-thumb" />;
        }

        return <div className="rd-viewer-icon"><i className="bi bi-file-earmark-text" /></div>;
    };

    const ownerName = resource?.uploadBy?.fullName || resource?.uploadBy?.username || "Không rõ";
    const categoryItems = [...(resource?.subjects || []), ...(resource?.topics || [])];
    const typeItems = resource?.types || [];
    const tagItems = resource?.tags || [];

    if (loading) return <MySpinner />;
    if (err) return <Container className="py-5"><Alert variant="danger">{err}</Alert></Container>;
    if (!resource) return null;

    return (
        <div className="rd-page">
            <Container className="py-4">
                <nav className="rd-breadcrumb">
                    <Link to="/">Trang chủ</Link>
                    <span>›</span>
                    <Link to="/resources">Khám phá tài liệu</Link>
                    <span>›</span>
                    <span>{resource.title}</span>
                </nav>

                <Row className="g-4">
                    <Col lg={8}>
                        <div className="rd-title-block">
                            <h1 className="rd-title">{resource.title}</h1>
                            <div className="d-flex flex-wrap gap-2">
                                {typeItems.length > 0 ? typeItems.map(type => (
                                    <Badge key={type.id} className="rd-premium-badge">{type.name}</Badge>
                                )) : (
                                    <Badge className="rd-premium-badge">{getFormatLabel(resource.format)}</Badge>
                                )}
                            </div>
                        </div>

                        <div className="rd-viewer">
                            {renderViewer()}
                            <div className="rd-viewer-label">
                                <span>Xem trước tài liệu</span>
                                <small>
                                    {resource.pageCount ? `${resource.pageCount} trang` : getFormatLabel(resource.format)}
                                    {resource.fileSize ? ` · ${formatFileSize(resource.fileSize)}` : ""}
                                </small>
                            </div>
                            <button className="rd-fullscreen-btn" onClick={openResourceFile} disabled={!resource.fileUrl}>
                                {resource.fileUrl ? "Mở tài liệu" : "Chưa có file xem trước"}
                            </button>
                        </div>

                        <div className="rd-tabs">
                            {["desc", "comments", "notes"].map(tab => (
                                <button
                                    key={tab}
                                    className={`rd-tab-btn ${activeTab === tab ? "active" : ""}`}
                                    onClick={() => setActiveTab(tab)}
                                >
                                    {tab === "desc" ? "Mô tả tài liệu" : tab === "comments" ? `Bình luận (${comments.length})` : `Ghi chú (${notes.length})`}
                                </button>
                            ))}
                        </div>

                        {actionErr && <Alert variant="danger">{actionErr}</Alert>}

                        {activeTab === "desc" && (
                            <div className="rd-desc-panel">
                                {resource.description ? (
                                    <p className="mb-0">{resource.description}</p>
                                ) : (
                                    <p className="text-muted mb-0">Tài liệu chưa có mô tả.</p>
                                )}
                            </div>
                        )}

                        {activeTab === "comments" && (
                            <div className="rd-desc-panel">
                                <Form onSubmit={handleComment} className="fb-input-wrapper mb-4">
                                    <div className="fb-comment-avatar">{getInitial(user?.fullName || user?.username || "B")}</div>
                                    <Form.Control
                                        type="text"
                                        placeholder={user ? "Viết bình luận..." : "Đăng nhập để bình luận..."}
                                        value={commentText}
                                        onChange={e => setCommentText(e.target.value)}
                                        disabled={!user}
                                    />
                                </Form>

                                {comments.length === 0 && <div className="text-muted">Chưa có bình luận.</div>}

                                {comments.map(comment => (
                                    <div key={comment.id} className="fb-comment-thread">
                                        <div className="fb-comment-wrapper">
                                            <div className="fb-comment-avatar">{getInitial(comment.fullName || comment.username)}</div>
                                            <div className="fb-comment-body">
                                                <div className="fb-comment-bubble">
                                                    <span className="fb-comment-name">{comment.fullName || comment.username || "Người dùng"}</span>
                                                    <span className="fb-comment-text">{comment.note}</span>
                                                </div>
                                                <div className="fb-comment-actions">
                                                    <span>{comment.createdAt}</span>
                                                    {user && (
                                                        <a href="#!" onClick={e => {
                                                            e.preventDefault();
                                                            setReplyingTo(replyingTo === comment.id ? null : comment.id);
                                                            setReplyText("");
                                                        }}>
                                                            {replyingTo === comment.id ? "Hủy" : "Phản hồi"}
                                                        </a>
                                                    )}
                                                    {user?.id === comment.userId && (
                                                        <a href="#!" onClick={e => { e.preventDefault(); handleDeleteInteraction(comment.id); }}>
                                                            Xóa
                                                        </a>
                                                    )}
                                                </div>
                                                {replyingTo === comment.id && (
                                                    <Form onSubmit={e => handleReply(e, comment.id)} className="fb-input-wrapper mt-2">
                                                        <div className="fb-comment-avatar" style={{ width: 28, height: 28, fontSize: "0.72rem" }}>
                                                            {getInitial(user?.fullName || user?.username || "B")}
                                                        </div>
                                                        <Form.Control
                                                            type="text"
                                                            placeholder={`Trả lời ${comment.fullName || comment.username || "bình luận"}...`}
                                                            value={replyText}
                                                            onChange={e => setReplyText(e.target.value)}
                                                            autoFocus
                                                        />
                                                    </Form>
                                                )}
                                                {(repliesByComment[comment.id] || []).length > 0 && (
                                                    <div className="fb-comment-children">
                                                        {(repliesByComment[comment.id] || []).map(reply => (
                                                            <div key={reply.id} className="fb-comment-thread fb-comment-reply">
                                                                <div className="fb-comment-wrapper">
                                                                    <div className="fb-comment-avatar">{getInitial(reply.fullName || reply.username)}</div>
                                                                    <div className="fb-comment-body">
                                                                        <div className="fb-comment-bubble">
                                                                            <span className="fb-comment-name">{reply.fullName || reply.username || "Người dùng"}</span>
                                                                            <span className="fb-comment-text">{reply.content}</span>
                                                                        </div>
                                                                        <div className="fb-comment-actions">
                                                                            {user?.id === reply.userId && (
                                                                                <a href="#!" onClick={e => {
                                                                                    e.preventDefault();
                                                                                    handleDeleteReply(reply.id, comment.id);
                                                                                }}>
                                                                                    Xóa
                                                                                </a>
                                                                            )}
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        ))}
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}

                        {activeTab === "notes" && (
                            <div className="rd-desc-panel">
                                <Form onSubmit={handleSaveNote}>
                                    <Form.Control
                                        as="textarea"
                                        rows={5}
                                        placeholder={user ? "Ghi chú cá nhân..." : "Đăng nhập để lưu ghi chú..."}
                                        className="mb-2"
                                        value={noteText}
                                        onChange={e => setNoteText(e.target.value)}
                                        disabled={!user}
                                    />
                                    <div className="d-flex gap-2">
                                        <Button variant="outline-primary" size="sm" type="submit" disabled={!user || !noteText.trim()}>
                                            {editingNoteId ? "Cập nhật ghi chú" : "Lưu ghi chú"}
                                        </Button>
                                        {editingNoteId && (
                                            <Button variant="light" size="sm" onClick={() => { setEditingNoteId(null); setNoteText(""); }}>
                                                Hủy
                                            </Button>
                                        )}
                                    </div>
                                </Form>

                                <div className="mt-4">
                                    {notes.length === 0 && <div className="text-muted">Chưa có ghi chú.</div>}
                                    {notes.map(note => (
                                        <div key={note.id} className="border rounded p-3 mb-2">
                                            <div className="d-flex justify-content-between gap-2">
                                                <small className="text-muted">{note.createdAt}</small>
                                                {user?.id === note.userId && (
                                                    <div className="d-flex gap-2">
                                                        <Button variant="link" size="sm" className="p-0" onClick={() => handleEditNote(note)}>
                                                            Sửa
                                                        </Button>
                                                        <Button variant="link" size="sm" className="p-0 text-danger" onClick={() => handleDeleteInteraction(note.id)}>
                                                            Xóa
                                                        </Button>
                                                    </div>
                                                )}
                                            </div>
                                            <div className="mt-2">{note.note}</div>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}
                    </Col>

                    <Col lg={4}>
                        <div className="rd-sidebar">
                            <div className="rd-meta-block">
                                <div className="rd-meta-row">
                                    <span className="rd-meta-label">Tác giả</span>
                                    <span className="rd-meta-value">{ownerName}</span>
                                </div>
                                <div className="rd-meta-row">
                                    <span className="rd-meta-label">Ngày đăng</span>
                                    <span className="rd-meta-value">{formatDate(resource.createdAt)}</span>
                                </div>
                                <div className="rd-meta-row">
                                    <span className="rd-meta-label">Định dạng</span>
                                    <span className="rd-meta-value">{getFormatLabel(resource.format)} {resource.fileSize ? `(${formatFileSize(resource.fileSize)})` : ""}</span>
                                </div>
                                <div className="rd-meta-row">
                                    <span className="rd-meta-label">Cấp độ</span>
                                    <Badge bg={levelVariant(resource.level)} className="rd-level-badge">{formatLevel(resource.level)}</Badge>
                                </div>
                                <div className="rd-meta-row">
                                    <span className="rd-meta-label">Danh mục</span>
                                    <div className="d-flex flex-wrap gap-1 justify-content-end">
                                        {categoryItems.length === 0 && <span className="rd-meta-value">Không có</span>}
                                        {categoryItems.map(item => (
                                            <span key={`${item.id}-${item.name}`} className="rd-cat-pill">{item.name}</span>
                                        ))}
                                    </div>
                                </div>
                            </div>

                            <div className="rd-tags-block">
                                <div className="rd-tags-title">Thẻ tìm kiếm</div>
                                <div className="d-flex flex-wrap gap-2">
                                    {tagItems.length === 0 && <span className="text-muted">Không có thẻ</span>}
                                    {tagItems.map(tag => (
                                        <span key={tag.id} className="rd-tag">#{tag.name}</span>
                                    ))}
                                </div>
                            </div>
                        </div>
                    </Col>
                </Row>

                {relatedResources.length > 0 && (
                    <div className="rd-related">
                        <div className="rd-related-head">
                            <span>Tài liệu liên quan</span>
                            <Link to="/resources">Xem tất cả ›</Link>
                        </div>
                        <Row className="g-3">
                            {relatedResources.map(item => (
                                <Col key={item.id} xs={12} sm={6} lg={4}>
                                    <ResourceCard resource={item} />
                                </Col>
                            ))}
                        </Row>
                    </div>
                )}
            </Container>
        </div>
    );
};

export default ResourceDetail;

import { useEffect, useState } from "react";
import { Badge, Button, Col, Container, Form, Row } from "react-bootstrap";
import { Link, useNavigate, useParams } from "react-router-dom";

import MySpinner from "../../components/common/MySpinner";
import ResourceCard from "../../components/common/ResourceCard";
import Apis, { authApis, endpoints } from "../../configs/Apis";

const formatDate = (date) => {
    if (!date) return "";
    return new Date(date).toLocaleDateString("vi-VN");
};

const getFormatLabel = (format) => {
    if (!format) return "RESOURCE";
    return format;
};

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


const Avatar = ({ name, size = 36 }) => (
    <div className="rd-avatar" style={{ width: size, height: size, fontSize: size * 0.4 }}>
        {name.charAt(0)}
    </div>
);

const ResourceDetail = () => {
    const { id } = useParams();
    const [resource, setResource] = useState(null);
    const [relatedResources, setRelatedResources] = useState([]);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState("");
    const [commentText, setCommentText] = useState("");
    const [replyText, setReplyText] = useState("");
    const [replyingTo, setReplyingTo] = useState(null);
    const [comments, setComments] = useState([]);
    const [activeTab, setActiveTab] = useState("desc");
    const nav = useNavigate();

    useEffect(() => {
        const loadResource = async () => {
            try {
                setLoading(true);

                const res = await Apis.get(endpoints["resource-detail"](id));
                setResource(res.data.data);

            } catch (err) {
                console.error(err);
                setErr("Không tìm thấy tài liệu.");
            } finally {
                setLoading(false);
            }
        };

        loadResource();
    }, [id]);

    const handleComment = (e) => {
        e.preventDefault();
        if (!commentText.trim()) return;
        setComments([...comments, { id: Date.now(), parentId: null, user: { fullName: "Bạn" }, content: commentText, time: "Vừa xong" }]);
        setCommentText("");
    };

    const handleReply = (e, parentId) => {
        e.preventDefault();
        if (!replyText.trim()) return;
        setComments([...comments, { id: Date.now(), parentId, user: { fullName: "Bạn" }, content: replyText, time: "Vừa xong" }]);
        setReplyText("");
        setReplyingTo(null);
    };

    const renderComments = (parentId = null, depth = 0) => {
        const list = comments.filter(c => c.parentId === parentId);
        if (!list.length) return null;
        return list.map(c => {
            const hasChildren = comments.some(ch => ch.parentId === c.id);
            return (
                <div key={c.id} className={`fb-comment-thread ${depth > 0 ? 'fb-comment-reply' : ''}`}>
                    {depth > 0 && <div className="fb-comment-curve-line"></div>}
                    {hasChildren && <div className="fb-comment-line"></div>}
                    <div className="fb-comment-wrapper">
                        <div className="fb-comment-avatar">{c.user.fullName.charAt(0)}</div>
                        <div className="fb-comment-body">
                            <div className="fb-comment-bubble">
                                <span className="fb-comment-name">{c.user.fullName}</span>
                                <span className="fb-comment-text">{c.content}</span>
                            </div>
                            <div className="fb-comment-actions">
                                <span>{c.time}</span>
                                <a href="#!" onClick={e => e.preventDefault()}>Thích</a>
                                <a href="#!" onClick={e => { e.preventDefault(); setReplyingTo(replyingTo === c.id ? null : c.id); setReplyText(""); }}>
                                    {replyingTo === c.id ? "Hủy" : "Phản hồi"}
                                </a>
                            </div>
                            {replyingTo === c.id && (
                                <Form onSubmit={e => handleReply(e, c.id)} className="fb-input-wrapper mt-2">
                                    <div className="fb-comment-avatar" style={{ width: 28, height: 28, fontSize: '0.72rem' }}>B</div>
                                    <Form.Control type="text" placeholder={`Trả lời ${c.user.fullName}...`} value={replyText} onChange={e => setReplyText(e.target.value)} autoFocus />
                                </Form>
                            )}
                        </div>
                    </div>
                    {hasChildren && (
                        <div className="fb-comment-children">{renderComments(c.id, depth + 1)}</div>
                    )}
                </div>
            );
        });
    };

    if (loading) return <MySpinner />;
    if (err) return <Container className="py-5"><div className="rd-error">{err}</div></Container>;
    if (!resource) return null;

    const relatedAll = relatedResources;
    return (
        <div className="rd-page">
            <Container className="py-4">
                {/* Breadcrumb */}
                <nav className="rd-breadcrumb">
                    <Link to="/">Trang chủ</Link>
                    <span>›</span>
                    <Link to="/resources">Khám phá tài liệu</Link>
                    <span>›</span>
                    <span>{resource.title}</span>
                </nav>

                <Row className="g-4">
                    {/* ===== LEFT COLUMN ===== */}
                    <Col lg={8}>
                        {/* Title Block */}
                        <div className="rd-title-block">
                            <h1 className="rd-title">{resource.title}</h1>
                            <Badge className="rd-premium-badge">Premium Resource</Badge>
                        </div>

                        {/* Viewer */}
                        <div className="rd-viewer">
                            {resource.thumbnailUrl ? (
                                <img src={resource.thumbnailUrl} alt={resource.title} className="rd-viewer-thumb" />
                            ) : (
                                <div className="rd-viewer-icon">📄</div>
                            )}
                            <div className="rd-viewer-label">
                                <span>Xem trước tài liệu</span>
                                <small>Trang 1 trên {resource.pageCount || "N/A"} trang</small>
                            </div>
                            <button className="rd-fullscreen-btn">Chế độ toàn màn hình</button>
                        </div>

                        {/* Tabs */}
                        <div className="rd-tabs">
                            {["desc", "comments", "notes"].map(tab => (
                                <button
                                    key={tab}
                                    className={`rd-tab-btn ${activeTab === tab ? 'active' : ''}`}
                                    onClick={() => setActiveTab(tab)}
                                >
                                    {tab === "desc" ? "Mô tả tài liệu" : tab === "comments" ? `Bình luận (${comments.length})` : "Ghi chú"}
                                </button>
                            ))}
                        </div>

                        {activeTab === "desc" && (
                            <div className="rd-desc-panel">
                                <p>{resource.description}</p>
                                <div className="rd-desc-section">
                                    <strong>Đối tượng hướng tới:</strong>
                                    <ul>
                                        <li>Sinh viên năm cuối chuyên ngành {(resource.subjects || []).map(s => s.name).join(", ")}.</li>
                                        <li>Các kỹ sư AI đang làm việc tại doanh nghiệp.</li>
                                        <li>Nghiên cứu sinh trong lĩnh vực {(resource.topics || []).map(t => t.name).join(", ")}.</li>
                                    </ul>
                                </div>
                                <div className="rd-desc-section">
                                    <strong>Kết quả học tập chính:</strong>
                                    <ul>
                                        <li>Hiểu sâu về cơ sở toán học của các thuật toán trong tài liệu này.</li>
                                        <li>Kỹ thuật xử lý dữ liệu nhiều và tối ưu hóa nâng cao.</li>
                                        <li>Quy trình triển khai mô hình vào hệ thống production quy mô lớn.</li>
                                    </ul>
                                </div>
                            </div>
                        )}

                        {activeTab === "comments" && (
                            <div className="rd-desc-panel">
                                <Form onSubmit={handleComment} className="fb-input-wrapper mb-4">
                                    <div className="fb-comment-avatar">B</div>
                                    <Form.Control type="text" placeholder="Viết bình luận..." value={commentText} onChange={e => setCommentText(e.target.value)} />
                                </Form>
                                {renderComments(null)}
                            </div>
                        )}

                        {activeTab === "notes" && (
                            <div className="rd-desc-panel">
                                <Form.Control as="textarea" rows={5} placeholder="Ghi chú cá nhân..." className="mb-2" />
                                <Button variant="outline-primary" size="sm">Lưu ghi chú</Button>
                            </div>
                        )}
                    </Col>

                    {/* ===== RIGHT SIDEBAR ===== */}
                    <Col lg={4}>
                        <div className="rd-sidebar">
                            {/* Download Button */}
                            <Button variant="primary" className="rd-download-btn w-100 mb-4">
                                Tải xuống tài liệu
                            </Button>

                            {/* Meta Info */}
                            <div className="rd-meta-block">
                                <div className="rd-meta-row">
                                    <span className="rd-meta-label">Tác giả</span>
                                    <span className="rd-meta-value">{resource.username}</span>
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
                                    <div className="d-flex flex-wrap gap-1">
                                        {(resource.subjects || []).map(s => (
                                            <span key={s.id} className="rd-cat-pill">{s.name}</span>
                                        ))}
                                    </div>
                                </div>
                            </div>

                            {/* Tags */}
                            <div className="rd-tags-block">
                                <div className="rd-tags-title">Thẻ tìm kiếm</div>
                                <div className="d-flex flex-wrap gap-2">
                                    {(resource.tags || []).map(t => (
                                        <span key={t.id} className="rd-tag">#{t.name}</span>
                                    ))}
                                </div>
                            </div>
                        </div>
                    </Col>
                </Row>

                {/* Related Resources */}
                {relatedAll.length > 0 && (
                    <div className="rd-related">
                        <div className="rd-related-head">
                            <span>Tài liệu liên quan</span>
                            <Link to="/resources">Xem tất cả →</Link>
                        </div>
                        <Row className="g-3">
                            {relatedAll.map(r => (
                                <Col key={r.id} xs={12} sm={6} lg={4}>
                                    <ResourceCard resource={r} />
                                </Col>
                            ))}
                        </Row>
                    </div>
                )}
            </Container>
        </div>
    );
}
export default ResourceDetail;

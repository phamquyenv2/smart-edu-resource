import { useCallback, useContext, useEffect, useState } from "react";
import { Alert, Badge, Button, Col, Container, Form, Modal, Row, Spinner, Accordion, OverlayTrigger, Tooltip } from "react-bootstrap";
import { useNavigate } from "react-router-dom";

import { MyUserContext } from "../../configs/Context";
import MySpinner from "../../components/common/MySpinner";
import { authApis, endpoints } from "../../configs/Apis";

const LearningPath = () => {
    const [user] = useContext(MyUserContext);
    const [loading, setLoading] = useState(true);
    const [paths, setPaths] = useState([]);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const [generating, setGenerating] = useState(false);
    const [activePathId, setActivePathId] = useState(null);
    const [showGenerate, setShowGenerate] = useState(false);
    const [genForm, setGenForm] = useState({ goal: "", additionalInfo: "" });

    const [showEdit, setShowEdit] = useState(false);
    const [editingPath, setEditingPath] = useState(null);
    const [editForm, setEditForm] = useState({ title: "", description: "", goal: "" });

    const nav = useNavigate();

    const getData = (res) => {
        if (Array.isArray(res.data)) return res.data;
        return res.data?.data || res.data || [];
    };

    const loadPaths = useCallback(async () => {
        try {
            setLoading(true);
            setErr("");
            const res = await authApis().get(endpoints["learning-paths"]);
            const data = getData(res);
            setPaths(Array.isArray(data) ? data : []);
            if (Array.isArray(data) && data.length > 0 && !activePathId) {
                setActivePathId(data[0].id);
            }
        } catch (e) {
            console.error(e);
            setPaths([]);
            setErr("Không thể tải lộ trình học tập.");
        } finally {
            setLoading(false);
        }
    }, [activePathId]);

    useEffect(() => {
        if (!user) { nav("/login"); return; }
        loadPaths();
    }, [user, nav, loadPaths]);

    useEffect(() => {
        if (success) { const t = setTimeout(() => setSuccess(""), 4000); return () => clearTimeout(t); }
    }, [success]);

    const handleGenerate = async (e) => {
        e.preventDefault();
        try {
            setGenerating(true);
            setErr("");
            setSuccess("");
            await authApis().post(endpoints["learning-path-generate"], genForm);
            setSuccess("Đã tạo lộ trình học tập bằng AI thành công!");
            setShowGenerate(false);
            setGenForm({ goal: "", additionalInfo: "" });
            await loadPaths();
        } catch (e) {
            console.error(e);
            setErr("Tạo lộ trình AI thất bại. Vui lòng thử lại.");
        } finally {
            setGenerating(false);
        }
    };

    const openEditModal = (path) => {
        setEditingPath(path);
        setEditForm({ title: path.title || "", description: path.description || "", goal: path.goal || "" });
        setShowEdit(true);
    };

    const handleUpdate = async (e) => {
        e.preventDefault();
        if (!editingPath) return;
        try {
            setErr(""); setSuccess("");
            await authApis().put(endpoints["learning-path-detail"](editingPath.id), editForm);
            setSuccess("Cập nhật lộ trình thành công.");
            setShowEdit(false);
            setEditingPath(null);
            await loadPaths();
        } catch (e) {
            console.error(e);
            setErr("Cập nhật thất bại.");
        }
    };

    const handleDeletePath = async (id) => {
        if (!window.confirm("Bạn chắc chắn muốn xóa lộ trình này?")) return;
        try {
            setErr(""); setSuccess("");
            await authApis().delete(endpoints["learning-path-detail"](id));
            setSuccess("Xóa lộ trình thành công.");
            if (activePathId === id) setActivePathId(null);
            await loadPaths();
        } catch (e) {
            console.error(e);
            setErr("Xóa lộ trình thất bại.");
        }
    };

    const handleDeleteItem = async (itemId) => {
        if (!window.confirm("Bạn chắc chắn muốn xóa mục này khỏi lộ trình?")) return;
        try {
            setErr(""); setSuccess("");
            await authApis().delete(endpoints["learning-path-item-delete"](itemId));
            setSuccess("Đã xóa mục khỏi lộ trình.");
            await loadPaths();
        } catch (e) {
            console.error(e);
            setErr("Xóa mục thất bại.");
        }
    };

    const getItemColor = (type) => {
        switch (type) {
            case "COURSE": return "#4338CA";
            case "RESOURCE": return "#059669";
            case "QUIZ": return "#D97706";
            default: return "#64748B";
        }
    };

    const getItemLabel = (type) => {
        switch (type) {
            case "COURSE": return "Khóa học";
            case "RESOURCE": return "Tài liệu";
            case "QUIZ": return "Bài kiểm tra";
            default: return "Mục";
        }
    };

    const navigateToItem = (item) => {
        if (item.itemType === "COURSE" && item.referenceId) nav(`/courses/${item.referenceId}`);
        else if (item.itemType === "RESOURCE" && item.referenceId) nav(`/resources/${item.referenceId}`);
        else if (item.itemType === "QUIZ" && item.referenceId) nav(`/quizzes/${item.referenceId}/take`);
    };

    if (loading) return <MySpinner />;

    const activePath = paths.find(p => p.id === activePathId);

    return (
        <Container className="py-4" style={{ maxWidth: 1100 }}>
            <div className="lp-page-header">
                <div>
                    <h2 className="lp-page-title">
                        <i className="bi bi-signpost-2 me-2"></i>
                        Lộ trình học tập
                    </h2>
                    <p className="lp-page-subtitle">
                        Sử dụng AI để phân tích và đề xuất lộ trình cá nhân hóa phù hợp với mục tiêu của bạn
                    </p>
                </div>
                <Button className="lp-generate-btn" onClick={() => setShowGenerate(true)} disabled={generating}>
                    {generating ? "Đang xử lý..." : "Tạo lộ trình mới"}
                </Button>
            </div>

            {err && <Alert variant="danger" dismissible onClose={() => setErr("")}>{err}</Alert>}
            {success && <Alert variant="success" dismissible onClose={() => setSuccess("")}>{success}</Alert>}

            {paths.length === 0 ? (
                <div className="lp-empty-state">
                    <div className="lp-empty-icon">
                        <i className="bi bi-map"></i>
                    </div>
                    <h4>Chưa có lộ trình học tập</h4>
                    <p>Hãy nhấn <strong>"Bắt đầu tạo lộ trình"</strong> để hệ thống phân tích thông tin cá nhân, trình độ và mục tiêu của bạn, từ đó đề xuất lộ trình phù hợp.</p>
                    <Button className="lp-generate-btn" onClick={() => setShowGenerate(true)}>
                        Bắt đầu tạo lộ trình
                    </Button>
                </div>
            ) : (
                <Row>
                    <Col md={4} className="mb-3">
                        <div className="lp-sidebar">
                            <div className="lp-sidebar-header">
                                <span><i className="bi bi-list-ul me-2"></i>Danh sách lộ trình ({paths.length})</span>
                            </div>
                            {paths.map(p => (
                                <div
                                    key={p.id}
                                    className={`lp-sidebar-item ${activePathId === p.id ? 'active' : ''}`}
                                    onClick={() => setActivePathId(p.id)}
                                >
                                    <div className="lp-sidebar-item-header">
                                        <div className="lp-sidebar-item-title">{p.title || "Lộ trình học tập"}</div>
                                    </div>
                                    <div className="lp-sidebar-item-meta">
                                        <span><i className="bi bi-layers me-1"></i>{(p.items || []).length} mục</span>
                                        {p.createdAt && (
                                            <span>{new Date(p.createdAt).toLocaleDateString('vi-VN')}</span>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    </Col>

                    <Col md={8}>
                        {activePath ? (
                            <div className="lp-detail">
                                <div className="lp-detail-header">
                                    <div className="lp-detail-header-bg"></div>
                                    <div className="lp-detail-header-content">
                                        <div className="d-flex justify-content-between align-items-start">
                                            <div style={{ flex: 1 }}>
                                                <div className="d-flex align-items-center gap-2 mb-2">
                                                    <h3 className="lp-detail-title">{activePath.title || "Lộ trình học tập"}</h3>
                                                </div>
                                                {activePath.goal && (
                                                    <div className="lp-detail-goal">
                                                        <strong>Mục tiêu:</strong> {activePath.goal}
                                                    </div>
                                                )}
                                            </div>
                                            <div className="d-flex gap-2">
                                                <OverlayTrigger placement="top" overlay={<Tooltip>Chỉnh sửa</Tooltip>}>
                                                    <Button variant="light" size="sm" className="lp-action-btn" onClick={() => openEditModal(activePath)}>
                                                        <i className="bi bi-pencil"></i>
                                                    </Button>
                                                </OverlayTrigger>
                                                <OverlayTrigger placement="top" overlay={<Tooltip>Xóa lộ trình</Tooltip>}>
                                                    <Button variant="light" size="sm" className="lp-action-btn danger" onClick={() => handleDeletePath(activePath.id)}>
                                                        <i className="bi bi-trash"></i>
                                                    </Button>
                                                </OverlayTrigger>
                                            </div>
                                        </div>

                                        <div className="lp-stats-row">
                                            <div className="lp-stat-chip">
                                                <span>{(activePath.items || []).length} mục</span>
                                            </div>
                                            <div className="lp-stat-chip">
                                                <span>{(activePath.items || []).filter(i => i.itemType === "COURSE").length} khóa học</span>
                                            </div>
                                            <div className="lp-stat-chip">
                                                <span>{(activePath.items || []).filter(i => i.itemType === "RESOURCE").length} tài liệu</span>
                                            </div>
                                            <div className="lp-stat-chip">
                                                <span>{(activePath.items || []).filter(i => i.itemType === "QUIZ").length} bài kiểm tra</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div className="lp-timeline">
                                    {(activePath.items || []).length === 0 ? (
                                        <div className="lp-no-items">
                                            <i className="bi bi-inbox"></i>
                                            <p>Lộ trình chưa có mục nào. Hãy thử tạo lại với mục tiêu cụ thể hơn.</p>
                                        </div>
                                    ) : (
                                        (activePath.items || [])
                                            .sort((a, b) => (a.orderNumber || 0) - (b.orderNumber || 0))
                                            .map((item, idx) => (
                                                <div key={item.id || idx} className="lp-timeline-item">
                                                    <div className="lp-timeline-connector">
                                                        <div className="lp-timeline-dot" style={{ background: getItemColor(item.itemType) }}>
                                                        </div>
                                                        {idx < (activePath.items || []).length - 1 && <div className="lp-timeline-line"></div>}
                                                    </div>
                                                    <div className="lp-timeline-card" onClick={() => navigateToItem(item)}>
                                                        <div className="lp-timeline-card-header">
                                                            <div>
                                                                <div className="d-flex align-items-center gap-2 mb-1">
                                                                    <Badge
                                                                        className="lp-type-badge"
                                                                        style={{ background: getItemColor(item.itemType), color: '#fff' }}
                                                                    >
                                                                        {getItemLabel(item.itemType)}
                                                                    </Badge>
                                                                    {item.isRequired && (
                                                                        <Badge bg="danger" className="lp-required-badge">Bắt buộc</Badge>
                                                                    )}
                                                                    <span className="lp-step-num">Bước {item.orderNumber || idx + 1}</span>
                                                                </div>
                                                                <h6 className="lp-item-title">{item.referenceName || `Mục #${item.referenceId}`}</h6>
                                                            </div>
                                                            <div className="d-flex gap-1">
                                                                <Button
                                                                    variant="light" size="sm" className="lp-item-action"
                                                                    onClick={(e) => { e.stopPropagation(); navigateToItem(item); }}
                                                                >
                                                                    <i className="bi bi-box-arrow-up-right"></i>
                                                                </Button>
                                                                <Button
                                                                    variant="light" size="sm" className="lp-item-action danger"
                                                                    onClick={(e) => { e.stopPropagation(); handleDeleteItem(item.id); }}
                                                                >
                                                                    <i className="bi bi-x-lg"></i>
                                                                </Button>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            ))
                                    )}
                                </div>
                            </div>
                        ) : (
                            <div className="lp-no-items" style={{ marginTop: 40 }}>
                                <i className="bi bi-hand-index"></i>
                                <p>Chọn một lộ trình từ danh sách bên trái để xem chi tiết</p>
                            </div>
                        )}
                    </Col>
                </Row>
            )}

            <Modal show={showGenerate} onHide={() => !generating && setShowGenerate(false)} size="lg">
                <Modal.Header closeButton className="lp-modal-header">
                    <Modal.Title>
                        Tạo lộ trình cá nhân hóa
                    </Modal.Title>
                </Modal.Header>
                <Form onSubmit={handleGenerate}>
                    <Modal.Body>
                        <div className="lp-gen-info">
                            Hệ thống sẽ phân tích thông tin cá nhân, trình độ và mục tiêu của bạn để đề xuất lộ trình phù hợp bao gồm khóa học, tài liệu và bài tập thực hành.
                        </div>
                        <Form.Group className="mb-3">
                            <Form.Label className="fw-semibold">
                                <i className="bi bi-bullseye me-1"></i>Mục tiêu học tập
                            </Form.Label>
                            <Form.Control
                                as="textarea" rows={3}
                                placeholder="VD: Trở thành lập trình viên Full-stack, Nắm vững Machine Learning cơ bản..."
                                value={genForm.goal}
                                onChange={e => setGenForm({ ...genForm, goal: e.target.value })}
                            />
                            <Form.Text className="text-muted">Mô tả mục tiêu càng cụ thể, lộ trình càng chính xác</Form.Text>
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label className="fw-semibold">
                                <i className="bi bi-chat-dots me-1"></i>Thông tin bổ sung (tùy chọn)
                            </Form.Label>
                            <Form.Control
                                as="textarea" rows={2}
                                placeholder="VD: Tôi đã biết Python cơ bản, muốn học thêm về AI..."
                                value={genForm.additionalInfo}
                                onChange={e => setGenForm({ ...genForm, additionalInfo: e.target.value })}
                            />
                        </Form.Group>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant="secondary" onClick={() => setShowGenerate(false)} disabled={generating}>Hủy</Button>
                        <Button type="submit" className="lp-generate-btn" disabled={generating}>
                            {generating ? (
                                <><Spinner animation="border" size="sm" className="me-2" />Đang xử lý dữ liệu...</>
                            ) : (
                                <>Xác nhận tạo</>
                            )}
                        </Button>
                    </Modal.Footer>
                </Form>
            </Modal>


            <Modal show={showEdit} onHide={() => setShowEdit(false)} centered>
                <Modal.Header closeButton>
                    <Modal.Title><i className="bi bi-pencil-square me-2"></i>Chỉnh sửa lộ trình</Modal.Title>
                </Modal.Header>
                <Form onSubmit={handleUpdate}>
                    <Modal.Body>
                        <Form.Group className="mb-3">
                            <Form.Label className="fw-semibold">Tên lộ trình</Form.Label>
                            <Form.Control value={editForm.title} onChange={e => setEditForm({ ...editForm, title: e.target.value })} />
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label className="fw-semibold">Mục tiêu</Form.Label>
                            <Form.Control value={editForm.goal} onChange={e => setEditForm({ ...editForm, goal: e.target.value })} />
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label className="fw-semibold">Mô tả</Form.Label>
                            <Form.Control as="textarea" rows={3} value={editForm.description} onChange={e => setEditForm({ ...editForm, description: e.target.value })} />
                        </Form.Group>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant="secondary" onClick={() => setShowEdit(false)}>Hủy</Button>
                        <Button type="submit" variant="primary">Lưu thay đổi</Button>
                    </Modal.Footer>
                </Form>
            </Modal>
        </Container>
    );
};

export default LearningPath;
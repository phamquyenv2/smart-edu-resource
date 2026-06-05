import { useContext, useEffect, useState } from "react";
import { Alert, Badge, Button, Col, Container, Form, Modal, ProgressBar, Row } from "react-bootstrap";
import { useNavigate } from "react-router-dom";

import { MyUserContext } from "../../configs/Context";
import MySpinner from "../../components/common/MySpinner";
import { authApis, endpoints } from "../../configs/Apis";

const LearningPath = () => {
    const [user] = useContext(MyUserContext);
    const [loading, setLoading] = useState(true);
    const [steps, setSteps] = useState([]);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const [generating, setGenerating] = useState(false);

    const [showEdit, setShowEdit] = useState(false);
    const [editingStep, setEditingStep] = useState(null);
    const [editForm, setEditForm] = useState({
        title: "",
        description: "",
        progress: 0
    });

    const nav = useNavigate();

    const getData = (res) => {
        if (Array.isArray(res.data)) return res.data;
        return res.data.data || [];
    };

    const mapEnrollmentToStep = (e, index) => ({
        id: e.id,
        courseId: e.courseId,
        title: e.courseName || `Khóa học ${index + 1}`,
        description: `Tiến độ học tập: ${e.overallProgress || 0}%`,
        progress: e.overallProgress || 0,
        status: (e.overallProgress || 0) >= 100
            ? "done"
            : (e.overallProgress || 0) > 0
                ? "active"
                : "pending",
        totalStudyTime: e.totalStudyTime || 0,
        source: "enrollment"
    });

    const normalizeLearningPathStep = (item, index) => ({
        id: item.id,
        title: item.title || item.name || item.courseName || `Bước ${index + 1}`,
        description: item.description || item.desc || item.note || "",
        progress: item.progress || item.overallProgress || 0,
        status: item.status || (
            (item.progress || item.overallProgress || 0) >= 100
                ? "done"
                : (item.progress || item.overallProgress || 0) > 0
                    ? "active"
                    : "pending"
        ),
        completedResources: item.completedResources || item.completed || 0,
        totalResources: item.totalResources || item.resources || 0,
        source: "learning-path"
    });

    const loadLearningPath = async () => {
        try {
            setLoading(true);
            setErr("");

            const res = await authApis().get(endpoints["my-enrollments"]);
            const data = getData(res);
            setSteps(data.map(mapEnrollmentToStep));
        } catch (err) {
            console.error(err);
            setSteps([]);
            setErr("Không thể tải lộ trình học tập.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (!user) {
            nav("/login");
            return;
        }

        loadLearningPath();
    }, [user, nav]);

    const handleGenerate = async () => {
        try {
            setGenerating(true);
            setErr("");
            setSuccess("");

            await authApis().post(endpoints["learning-path-generate"]);

            setSuccess("Đã tạo lộ trình học tập bằng AI.");
            await loadLearningPath();
        } catch (err) {
            console.error(err);
            setErr("Tạo lộ trình AI thất bại.");
        } finally {
            setGenerating(false);
        }
    };

    const openEditModal = (step) => {
        setEditingStep(step);
        setEditForm({
            title: step.title || "",
            description: step.description || "",
            progress: step.progress || 0
        });
        setShowEdit(true);
    };

    const handleUpdate = async (e) => {
        e.preventDefault();

        if (!editingStep) return;

        try {
            setErr("");
            setSuccess("");

            await authApis().put(
                endpoints["learning-path-detail"](editingStep.id),
                {
                    title: editForm.title,
                    description: editForm.description,
                    progress: Number(editForm.progress)
                }
            );

            setSuccess("Cập nhật lộ trình thành công.");
            setShowEdit(false);
            setEditingStep(null);
            await loadLearningPath();
        } catch (err) {
            console.error(err);
            setErr("Cập nhật lộ trình thất bại.");
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm("Bạn chắc chắn muốn xóa bước/lộ trình này?")) return;

        try {
            setErr("");
            setSuccess("");

            await authApis().delete(endpoints["learning-path-detail"](id));

            setSuccess("Xóa thành công.");
            await loadLearningPath();
        } catch (err) {
            console.error(err);
            setErr("Xóa lộ trình thất bại.");
        }
    };

    if (loading) return <MySpinner />;

    const getProgress = (step) =>
        Math.round(step.progress || step.overallProgress || 0);

    const getStatus = (step) => {
        const progress = getProgress(step);

        if (step.status) return step.status;
        if (progress >= 100) return "done";
        if (progress > 0) return "active";
        return "pending";
    };

    const overallProgress = steps.length > 0
        ? Math.round(steps.reduce((sum, s) => sum + getProgress(s), 0) / steps.length)
        : 0;

    return (
        <Container className="py-4">
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h2 style={{ fontSize: "1.35rem", fontWeight: 700, margin: 0 }}>
                    Lộ trình học tập
                </h2>

                <Button
                    variant="primary"
                    size="sm"
                    onClick={handleGenerate}
                    disabled={generating}
                >
                    {generating ? "Đang tạo..." : "Tạo lộ trình AI"}
                </Button>
            </div>

            {err && <Alert variant="danger">{err}</Alert>}
            {success && <Alert variant="success">{success}</Alert>}

            <div className="panel-card mb-4" style={{ padding: "16px" }}>
                <div className="d-flex justify-content-between mb-2">
                    <strong>Tiến độ tổng thể</strong>
                    <span>{overallProgress}%</span>
                </div>
                <ProgressBar now={overallProgress} style={{ height: "8px" }} />
            </div>

            {steps.length === 0 ? (
                <div className="panel-card p-4 text-center text-muted">
                    Chưa có lộ trình học tập
                </div>
            ) : (
                steps.map((step, index) => {
                    const progress = getProgress(step);
                    const status = getStatus(step);

                    return (
                        <div
                            key={step.id || index}
                            className="panel-card mb-3"
                            style={{ padding: "16px", cursor: step.courseId ? "pointer" : "default" }}
                            onClick={() => {
                                if (step.courseId) nav(`/courses/${step.courseId}`);
                            }}
                        >
                            <div className="d-flex align-items-start gap-3">
                                <div
                                    className={`lp-step-circle ${status === "done" ? "done" : status === "active" ? "active" : ""}`}
                                >
                                    {status === "done" ? "✓" : index + 1}
                                </div>

                                <div className="flex-grow-1">
                                    <div className="d-flex justify-content-between align-items-start mb-1">
                                        <div>
                                            <h6 style={{ fontWeight: 700, margin: 0 }}>
                                                {step.title || step.name || "Bước học tập"}
                                            </h6>

                                            <p className="text-muted mb-2" style={{ fontSize: "0.88rem" }}>
                                                {step.desc || step.description || ""}
                                            </p>
                                        </div>

                                        <div className="d-flex gap-2 align-items-center">
                                            <Badge
                                                bg={status === "done" ? "success" : status === "active" ? "primary" : "secondary"}
                                            >
                                                {status === "done"
                                                    ? "Hoàn thành"
                                                    : status === "active"
                                                        ? "Đang học"
                                                        : "Chưa bắt đầu"}
                                            </Badge>

                                            {step.source === "learning-path" && (
                                                <>
                                                    <Button
                                                        variant="outline-primary"
                                                        size="sm"
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            openEditModal(step);
                                                        }}
                                                    >
                                                        Sửa
                                                    </Button>
                                                    <Button
                                                        variant="outline-danger"
                                                        size="sm"
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            handleDelete(step.id);
                                                        }}
                                                    >
                                                        Xóa
                                                    </Button>
                                                </>
                                            )}
                                        </div>
                                    </div>

                                    <div className="d-flex justify-content-between mb-1" style={{ fontSize: "0.8rem" }}>
                                        <span>
                                            Thời gian học: {step.totalStudyTime || 0} phút
                                        </span>
                                        <span>{progress}%</span>
                                    </div>

                                    <ProgressBar now={progress} style={{ height: "6px" }} />
                                </div>
                            </div>
                        </div>
                    );
                })
            )}

            <Modal show={showEdit} onHide={() => setShowEdit(false)} centered>
                <Modal.Header closeButton>
                    <Modal.Title>Sửa lộ trình</Modal.Title>
                </Modal.Header>

                <Form onSubmit={handleUpdate}>
                    <Modal.Body>
                        <Form.Group className="mb-3">
                            <Form.Label>Tên bước</Form.Label>
                            <Form.Control
                                value={editForm.title}
                                onChange={e => setEditForm({ ...editForm, title: e.target.value })}
                            />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label>Mô tả</Form.Label>
                            <Form.Control
                                as="textarea"
                                rows={3}
                                value={editForm.description}
                                onChange={e => setEditForm({ ...editForm, description: e.target.value })}
                            />
                        </Form.Group>

                        <Row>
                            <Col>
                                <Form.Group>
                                    <Form.Label>Tiến độ (%)</Form.Label>
                                    <Form.Control
                                        type="number"
                                        min="0"
                                        max="100"
                                        value={editForm.progress}
                                        onChange={e => setEditForm({ ...editForm, progress: e.target.value })}
                                    />
                                </Form.Group>
                            </Col>
                        </Row>
                    </Modal.Body>

                    <Modal.Footer>
                        <Button variant="secondary" onClick={() => setShowEdit(false)}>
                            Hủy
                        </Button>
                        <Button type="submit" variant="primary">
                            Lưu thay đổi
                        </Button>
                    </Modal.Footer>
                </Form>
            </Modal>
        </Container>
    );
};

export default LearningPath;
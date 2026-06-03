import { useContext, useEffect, useState } from "react";
import { Badge, Container, ProgressBar } from "react-bootstrap";
import { useNavigate } from "react-router-dom";

import { MyUserContext } from "../../configs/Context";
import MySpinner from "../../components/common/MySpinner";
import { authApis, endpoints } from "../../configs/Apis";

const LearningPath = () => {
    const [user] = useContext(MyUserContext);
    const [loading, setLoading] = useState(true);
    const [steps, setSteps] = useState([]);
    const nav = useNavigate();

    useEffect(() => {
        if (!user) { nav('/login'); return; }
        const loadLearningPath = async () => {
            try {
                const res = await authApis().get(endpoints["my-enrollments"]);
                const data = Array.isArray(res.data) ? res.data : res.data.data || [];
                const steps = data.map((e, index) => ({
                    id: e.id,
                    title: e.courseName || e.name || `Khóa học ${index + 1}`,
                    description: `Tiến độ học tập: ${e.overallProgress || 0}%`,
                    progress: e.overallProgress || 0,
                    status: (e.overallProgress || 0) >= 100 ? "done" : (e.overallProgress || 0) > 0 ? "active" : "pending",
                    completedResources: e.completedResources || 0,
                    totalResources: e.totalResources || 0
                }));

                setSteps(steps);
            } catch (err) {
                console.error(err);
                setSteps([]);
            } finally {
                setLoading(false);
            }
        };

        loadLearningPath();
    }, [user, nav]);

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
            <h2 style={{ fontSize: "1.35rem", fontWeight: 700, marginBottom: "12px" }}>
                Lộ trình học tập
            </h2>

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
                        <div key={step.id || index} className="panel-card mb-3" style={{ padding: "16px" }}>
                            <div className="d-flex align-items-start gap-3">
                                <div
                                    className={`lp-step-circle ${status === "done" ? "done" : status === "active" ? "active" : ""}`}
                                >
                                    {status === "done" ? "✓" : index + 1}
                                </div>

                                <div className="flex-grow-1">
                                    <div className="d-flex justify-content-between align-items-center mb-1">
                                        <h6 style={{ fontWeight: 700, margin: 0 }}>
                                            {step.title || step.name || "Bước học tập"}
                                        </h6>

                                        <Badge
                                            bg={status === "done" ? "success" : status === "active" ? "primary" : "secondary"}
                                        >
                                            {status === "done"
                                                ? "Hoàn thành"
                                                : status === "active"
                                                    ? "Đang học"
                                                    : "Chưa bắt đầu"}
                                        </Badge>
                                    </div>

                                    <p className="text-muted mb-2" style={{ fontSize: "0.88rem" }}>
                                        {step.desc || step.description || ""}
                                    </p>

                                    {status !== "pending" && (
                                        <>
                                            <div className="d-flex justify-content-between mb-1" style={{ fontSize: "0.8rem" }}>
                                                <span>
                                                    {(step.completed || step.completedResources || 0)}
                                                    /
                                                    {(step.resources || step.totalResources || 0)} tài liệu
                                                </span>
                                                <span>{progress}%</span>
                                            </div>

                                            <ProgressBar now={progress} style={{ height: "6px" }} />
                                        </>
                                    )}
                                </div>
                            </div>
                        </div>
                    );
                })
            )}
        </Container>
    );
}
export default LearningPath;

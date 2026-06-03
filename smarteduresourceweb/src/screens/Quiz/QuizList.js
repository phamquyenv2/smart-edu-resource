import { useContext, useEffect, useState } from "react";
import { Badge, Button, Col, Container, Row } from "react-bootstrap";
import { useNavigate } from "react-router-dom";

import { MyUserContext } from "../../configs/Context";
import MySpinner from "../../components/common/MySpinner";
import Apis, { endpoints } from "../../configs/Apis";

const QuizList = () => {
    const [user] = useContext(MyUserContext);
    const [loading, setLoading] = useState(true);
    const [quizzes, setQuizzes] = useState([]);
    const nav = useNavigate();

    useEffect(() => {
        if (!user) { nav('/login'); return; }
        const loadQuizzes = async () => {
            try {
                const res = await Apis.get(endpoints["quizzes"]);
                const data = Array.isArray(res.data) ? res.data : res.data.data || [];
                setQuizzes(data);
            } catch (err) {
                console.error(err);
                setQuizzes([]);
            } finally {
                setLoading(false);
            }
        };

        loadQuizzes();
    }, [user, nav]);

    if (loading) return <MySpinner />;

    const getTitle = (q) => q.title || q.name || "Bài kiểm tra";
    const getCourseName = (q) => q.courseName || q.courseTitle || "";
    const getDuration = (q) => q.duration || q.durationMinutes || q.timeLimit || 0;
    const getQuestionCount = (q) => q.questionCount || q.totalQuestions || 0;
    const getBestScore = (q) => q.bestScore ?? q.score ?? null;
    const getStatus = (q) => q.status || q.attemptStatus || "NOT_STARTED";

    return (
        <Container className="py-4">
            <h2 style={{ fontSize: '1.35rem', fontWeight: 700, marginBottom: '20px' }}>Bài kiểm tra</h2>
            {quizzes.length === 0 ? (
                <div className="panel-card p-4 text-center text-muted">
                    Chưa có bài kiểm tra
                </div>
            ) : (
                <Row className="g-3">
                    {quizzes.map(q => {
                        const status = getStatus(q);
                        const isCompleted = status === "COMPLETED" || status === "DONE";
                        const questionCount = getQuestionCount(q);
                        const bestScore = getBestScore(q);

                        return (
                            <Col key={q.id} xs={12} md={6} lg={4}>
                                <div className="panel-card" style={{ padding: "20px" }}>
                                    <div className="d-flex justify-content-between align-items-start mb-2">
                                        <h6 style={{ fontWeight: 600, marginBottom: 0 }}>
                                            {getTitle(q)}
                                        </h6>

                                        <Badge bg={isCompleted ? "success" : "primary"}>
                                            {isCompleted ? "Đã làm" : "Chưa làm"}
                                        </Badge>
                                    </div>

                                    <p style={{ fontSize: "0.82rem", color: "#64748B", marginBottom: "12px" }}>
                                        {getCourseName(q)}
                                    </p>

                                    <div className="d-flex gap-2 mb-3 flex-wrap" style={{ fontSize: "0.78rem" }}>
                                        <Badge bg="light" text="dark">
                                            {getDuration(q)} phút
                                        </Badge>

                                        <Badge bg="light" text="dark">
                                            {questionCount} câu
                                        </Badge>

                                        {bestScore !== null && (
                                            <Badge bg="light" text="dark">
                                                Điểm cao nhất: {bestScore}
                                                {questionCount ? `/${questionCount}` : ""}
                                            </Badge>
                                        )}
                                    </div>

                                    <Button
                                        variant={isCompleted ? "outline-primary" : "primary"}
                                        size="sm"
                                        className="w-100"
                                        onClick={() =>
                                            nav(isCompleted
                                                ? `/quizzes/${q.id}/result`
                                                : `/quizzes/${q.id}/take`
                                            )
                                        }
                                    >
                                        {isCompleted ? "Xem kết quả" : "Bắt đầu làm bài"}
                                    </Button>
                                </div>
                            </Col>
                        );
                    })}
                </Row>
            )}
        </Container>
    );
}
export default QuizList;

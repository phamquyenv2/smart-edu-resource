import { useEffect, useState } from "react";
import { Badge, Button, Col, Container, ListGroup, Row } from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";

import MySpinner from "../../components/common/MySpinner";
import { authApis, endpoints } from "../../configs/Apis";

const QuizResult = () => {
    const { id } = useParams();
    const [loading, setLoading] = useState(true);
    const [result, setResult] = useState(null);
    const nav = useNavigate();

    useEffect(() => {
        const loadResult = async () => {
            try {
                const res = await authApis().get(endpoints["student-quiz-results"]);
                const data = Array.isArray(res.data) ? res.data : res.data.data || [];

                const found = data.find(r =>
                    r.quizId === parseInt(id) ||
                    r.id === parseInt(id)
                );

                setResult(found || null);
            } catch (err) {
                console.error(err);
                setResult(null);
            } finally {
                setLoading(false);
            }
        };

        loadResult();
    }, [id]);

    if (loading) return <MySpinner />;

    if (!result) {
        return (
            <Container className="py-5 text-center">
                <h5>Không tìm thấy kết quả bài làm</h5>
                <Button className="mt-3" onClick={() => nav("/quizzes")}>
                    Về danh sách
                </Button>
            </Container>
        );
    }

    const quizTitle = result.quizTitle || result.title || "Bài kiểm tra";
    const score = result.score ?? result.correctAnswers ?? 0;
    const total = result.totalQuestions || result.questionCount || result.answers?.length || 10;
    const percentage = total > 0 ? Math.round((score / total) * 100) : 0;
    const answers = result.answers || result.details || [];

    return (
        <Container className="py-4">
            <a href="#!" className="detail-back" onClick={e => { e.preventDefault(); nav('/quizzes'); }}>← Quay lại danh sách</a>

            <div className="panel-card mb-4" style={{ padding: "32px", textAlign: "center" }}>
                <h4 style={{ fontWeight: 700, marginBottom: "16px" }}>Kết quả: {quizTitle}</h4>

                <div className="score-circle">
                    <span className="score-val">{score}/{total}</span>
                </div>

                <p style={{ fontSize: "1.1rem", fontWeight: 600, color: percentage >= 70 ? "#059669" : "#DC2626" }}>
                    {percentage >= 70 ? "Đạt" : "Chưa đạt"} — {percentage}%
                </p>

                <div className="d-flex justify-content-center gap-3 mt-3">
                    <Button variant="outline-primary" onClick={() => nav(`/quizzes/${id}/take`)}>
                        Làm lại
                    </Button>
                    <Button variant="primary" onClick={() => nav("/quizzes")}>
                        Về danh sách
                    </Button>
                </div>
            </div>

            <div className="panel-card">
                <div className="panel-head">Chi tiết kết quả</div>

                <ListGroup variant="flush">
                    {answers.length === 0 ? (
                        <ListGroup.Item>Chưa có chi tiết đáp án</ListGroup.Item>
                    ) : (
                        answers.map((a, idx) => {
                            const isCorrect = a.correct ?? a.isCorrect ?? false;

                            return (
                                <ListGroup.Item key={a.id || idx} style={{ fontSize: "0.88rem" }}>
                                    <Row>
                                        <Col xs={1}>
                                            <Badge bg={isCorrect ? "success" : "danger"} style={{ width: "24px" }}>
                                                {isCorrect ? "✓" : "✗"}
                                            </Badge>
                                        </Col>
                                        <Col>
                                            <strong>Câu {idx + 1}:</strong> {a.questionContent || a.question || a.content}
                                            <br />
                                            <small className="text-muted">
                                                Bạn chọn: {a.selectedAnswerContent || a.selectedAnswer || "—"}
                                            </small>

                                            {!isCorrect && (
                                                <>
                                                    <br />
                                                    <small style={{ color: "#059669" }}>
                                                        Đáp án đúng: {a.correctAnswerContent || a.correctAnswer || "—"}
                                                    </small>
                                                </>
                                            )}
                                        </Col>
                                    </Row>
                                </ListGroup.Item>
                            );
                        })
                    )}
                </ListGroup>
            </div>
        </Container>
    );
}
export default QuizResult;

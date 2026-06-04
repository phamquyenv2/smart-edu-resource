import { useEffect, useState } from "react";
import { Alert, Badge, Button, Col, Container, Form, Row } from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";

import MySpinner from "../../components/common/MySpinner";
import { authApis, endpoints } from "../../configs/Apis";

const QuizTaking = () => {
    const { id } = useParams();
    const [quiz, setQuiz] = useState(null);
    const [loading, setLoading] = useState(true);
    const [currentQ, setCurrentQ] = useState(0);
    const [answers, setAnswers] = useState({});
    const [timeLeft, setTimeLeft] = useState(0);
    const nav = useNavigate();

    useEffect(() => {
        const load = async () => {
            try {
                await new Promise(r => setTimeout(r, 400));
                const res = await authApis().get(
                    endpoints["student-quiz-detail"](id)
                );

                const quizData = res.data.data;

                setQuiz(quizData);
                setTimeLeft((quizData.durationMinutes || 30) * 60);
            } catch (ex) { console.error(ex); } finally { setLoading(false); }
        };
        load();
    }, [id]);

    useEffect(() => {
        if (timeLeft <= 0) return;
        const interval = setInterval(() => setTimeLeft(t => t - 1), 1000);
        return () => clearInterval(interval);
    }, [timeLeft]);

    const handleAnswer = (questionId, optionId) => {
        setAnswers({ ...answers, [questionId]: optionId });
    };

    const handleSubmit = async () => {
        try {

            const payload = {
                answers: Object.entries(answers).map(
                    ([questionId, optionId]) => ({
                        questionId: parseInt(questionId),
                        optionId: optionId
                    })
                )
            };

            await authApis().post(
                endpoints["student-quiz-submit"](id),
                payload
            );

            nav(`/quizzes/${id}/result`);

        } catch (err) {
            console.error(err);
            alert("Nộp bài thất bại!");
        }
    };

    if (loading) return <MySpinner />;
    if (!quiz) return <Container className="py-4"><Alert variant="danger">Không tìm thấy bài kiểm tra.</Alert></Container>;

    const questions = quiz?.questions || [];
    const q = questions[currentQ];
    const mins = Math.floor(timeLeft / 60);
    const secs = timeLeft % 60;

    return (
        <div>
            <div className="quiz-header">
                <Container>
                    <div className="d-flex justify-content-between align-items-center">
                        <strong style={{ fontSize: '0.95rem' }}>{quiz.title}</strong>
                        <div className="d-flex align-items-center gap-3">
                            <Badge bg={timeLeft < 60 ? "danger" : "secondary"} style={{ fontSize: '0.88rem' }}>
                                {String(mins).padStart(2, '0')}:{String(secs).padStart(2, '0')}
                            </Badge>
                            <Button variant="primary" size="sm" onClick={handleSubmit}>Nộp bài</Button>
                        </div>
                    </div>
                </Container>
            </div>

            <Container className="py-4">
                <Row>
                    <Col md={8}>
                        {q && (
                            <div className="panel-card" style={{ padding: '24px' }}>
                                <div className="d-flex justify-content-between mb-3">
                                    <strong>Câu {currentQ + 1}/{questions.length}</strong>
                                    <Badge bg="light" text="dark">{q.type === "SINGLE" ? "Chọn 1 đáp án" : "Chọn nhiều"}</Badge>
                                </div>
                                <p style={{ fontSize: '1rem', lineHeight: 1.6, marginBottom: '20px' }}>{q.content}</p>
                                <Form>
                                    {q.options.map(opt => (
                                        <Form.Check
                                            key={opt.id}
                                            type="radio"
                                            name={`q-${q.id}`}
                                            id={`opt-${opt.id}`}
                                            label={opt.content}
                                            checked={answers[q.id] === opt.id}
                                            onChange={() => handleAnswer(q.id, opt.id)}
                                            className="mb-2"
                                            style={{ fontSize: '0.9rem', padding: '8px 8px 8px 28px' }}
                                        />
                                    ))}
                                </Form>
                                <div className="d-flex justify-content-between mt-4">
                                    <Button variant="outline-secondary" size="sm" disabled={currentQ === 0} onClick={() => setCurrentQ(currentQ - 1)}>Câu trước</Button>
                                    <Button variant="outline-secondary" size="sm" disabled={currentQ >= questions.length - 1} onClick={() => setCurrentQ(currentQ + 1)}>Câu tiếp</Button>
                                </div>
                            </div>
                        )}
                    </Col>
                    <Col md={4}>
                        <div className="panel-card" style={{ padding: '16px' }}>
                            <h6 style={{ fontSize: '0.88rem', fontWeight: 600, marginBottom: '12px' }}>Danh sách câu hỏi</h6>
                            <div className="question-nav">
                                {questions.map((qq, idx) => (
                                    <button
                                        key={qq.id}
                                        className={`q-btn ${answers[qq.id] ? 'answered' : ''} ${idx === currentQ ? 'current' : ''}`}
                                        onClick={() => setCurrentQ(idx)}
                                    >
                                        {idx + 1}
                                    </button>
                                ))}
                            </div>
                            <div className="mt-3" style={{ fontSize: '0.78rem', color: '#94A3B8' }}>
                                Đã trả lời: {Object.keys(answers).length}/{questions.length}
                            </div>
                        </div>
                    </Col>
                </Row>
            </Container>
        </div>
    );
}
export default QuizTaking;

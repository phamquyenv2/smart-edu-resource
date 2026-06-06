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
    const [submitting, setSubmitting] = useState(false);
    const [result, setResult] = useState(null);
    const nav = useNavigate();

    useEffect(() => {
        const load = async () => {
            try {
                await new Promise(r => setTimeout(r, 400));
                const res = await authApis().get(
                    endpoints["student-quiz-detail"](id)
                );

                const quizData = res.data.data;
                const normalizedQuestions = Array.isArray(quizData?.questions)
                    ? quizData.questions.map(question => {
                        const options = Array.isArray(question?.options)
                            ? question.options
                            : Array.isArray(question?.answers)
                                ? question.answers
                                : [];

                        return {
                            ...question,
                            options
                        };
                    })
                    : [];

                setQuiz({
                    ...quizData,
                    questions: normalizedQuestions
                });
                
                const draftKey = `quiz_draft_${id}`;
                const draftStr = localStorage.getItem(draftKey);
                let restored = false;
                
                if (draftStr) {
                    try {
                        const draft = JSON.parse(draftStr);
                        const rem = Math.floor((draft.endTime - Date.now()) / 1000);
                        if (rem > 0) {
                            setAnswers(draft.answers || {});
                            setTimeLeft(rem);
                            restored = true;
                        } else {
                            localStorage.removeItem(draftKey);
                        }
                    } catch(e) {
                        localStorage.removeItem(draftKey);
                    }
                }
                
                if (!restored) {
                    const defaultTime = (quizData.durationMinutes || 30) * 60;
                    setTimeLeft(defaultTime);
                    localStorage.setItem(draftKey, JSON.stringify({
                        answers: {},
                        endTime: Date.now() + defaultTime * 1000
                    }));
                }
            } catch (ex) { console.error(ex); } finally { setLoading(false); }
        };
        load();
    }, [id]);

    useEffect(() => {
        if (timeLeft <= 0 || result || submitting) return;
        const interval = setInterval(() => setTimeLeft(t => t - 1), 1000);
        return () => clearInterval(interval);
    }, [timeLeft, result, submitting]);

    useEffect(() => {
        if (timeLeft === 0 && quiz && !result && !submitting) {
            handlePerformSubmit(true);
        }
    }, [timeLeft, quiz, result, submitting]);

    const handleAnswer = (questionId, optionId) => {
        const newAnswers = { ...answers, [questionId]: optionId };
        setAnswers(newAnswers);
        
        const draftKey = `quiz_draft_${id}`;
        const draftStr = localStorage.getItem(draftKey);
        if (draftStr) {
            try {
                const draft = JSON.parse(draftStr);
                draft.answers = newAnswers;
                localStorage.setItem(draftKey, JSON.stringify(draft));
            } catch(e) {}
        }
    };

    const handleSubmit = () => {
        if (!window.confirm("Bạn có chắc chắn muốn nộp bài?")) return;
        handlePerformSubmit(false);
    };

    const handlePerformSubmit = async (isAutoSubmit = false) => {
        if (submitting) return;
        setSubmitting(true);
        try {
            const payload = {
                answers: Object.entries(answers).map(
                    ([questionId, optionId]) => ({
                        questionId: parseInt(questionId),
                        optionId: optionId
                    })
                )
            };

            const res = await authApis().post(
                endpoints["student-quiz-submit"](id),
                payload
            );

            if (isAutoSubmit) {
                alert("Hết thời gian! Bài kiểm tra của bạn đã được tự động nộp.");
            }
            setResult(res.data?.data);
            localStorage.removeItem(`quiz_draft_${id}`);
        } catch (err) {
            console.error(err);
            alert("Nộp bài thất bại!");
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) return <MySpinner />;
    if (!quiz) return <Container className="py-4"><Alert variant="danger">Không tìm thấy bài kiểm tra.</Alert></Container>;

    if (result) {
        const courseId = result.courseId || quiz?.courseId;
        return (
            <Container className="py-5" style={{ maxWidth: 600 }}>
                <div className="panel-card text-center p-5 shadow-sm" style={{ borderRadius: '16px' }}>
                    <div className="mb-4">
                        <i className="bi bi-check-circle-fill text-success" style={{ fontSize: '4.5rem' }}></i>
                    </div>
                    <h3 className="mb-2 fw-bold">Nộp bài thành công!</h3>
                    <p className="text-muted mb-4">Kết quả bài kiểm tra của bạn đã được ghi nhận.</p>
                    
                    <div className="bg-light p-4 mb-4 text-start" style={{ borderRadius: '12px', border: '1px solid #E2E8F0' }}>
                        <Row className="mb-3 align-items-center">
                            <Col xs={6} className="text-secondary fw-semibold">Tổng điểm:</Col>
                            <Col xs={6} className="fw-bold fs-4 text-primary text-end">{result.score || 0}</Col>
                        </Row>
                        <hr className="my-2" style={{ borderColor: '#CBD5E1' }} />
                        <Row className="mb-2 align-items-center">
                            <Col xs={6} className="text-secondary">Số câu trả lời đúng:</Col>
                            <Col xs={6} className="fw-bold text-success text-end">{result.correctAnswers || 0} / {result.totalQuestions || 0}</Col>
                        </Row>
                        <Row className="mb-2 align-items-center">
                            <Col xs={6} className="text-secondary">Số câu trả lời sai:</Col>
                            <Col xs={6} className="fw-bold text-danger text-end">{result.wrongAnswers || 0}</Col>
                        </Row>
                        <Row className="align-items-center">
                            <Col xs={6} className="text-secondary">Thời gian nộp:</Col>
                            <Col xs={6} className="fw-semibold text-end" style={{ fontSize: '0.9rem' }}>{result.submittedAt}</Col>
                        </Row>
                    </div>
                    <div className="d-flex gap-3 justify-content-center">
                        <Button 
                            variant="outline-primary" 
                            style={{ padding: '10px 20px', borderRadius: '10px', fontWeight: 600 }}
                            onClick={() => nav(courseId ? `/courses/${courseId}/learn` : "/courses")}
                        >
                            <i className="bi bi-arrow-left me-2"></i>Quay lại bài học
                        </Button>
                    </div>
                </div>
            </Container>
        );
    }

    const questions = Array.isArray(quiz?.questions) ? quiz.questions : [];
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
                            <Button 
                                variant="primary" 
                                size="sm" 
                                onClick={handleSubmit} 
                                disabled={submitting || timeLeft <= 0}
                            >
                                {submitting ? "Đang nộp..." : "Nộp bài"}
                            </Button>
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
                                    {(Array.isArray(q.options) ? q.options : []).map(opt => (
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

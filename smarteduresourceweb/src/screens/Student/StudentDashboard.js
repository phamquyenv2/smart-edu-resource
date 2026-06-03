import { useContext, useEffect, useState } from "react";
import { Badge, Col, Container, ListGroup, ProgressBar, Row } from "react-bootstrap";
import { useNavigate } from "react-router-dom";

import { MyUserContext } from "../../configs/Context";
import MySpinner from "../../components/common/MySpinner";
import Apis, { authApis, endpoints } from "../../configs/Apis";

const StudentDashboard = () => {
    const [user] = useContext(MyUserContext);
    const [loading, setLoading] = useState(true);
    const [dashboard, setDashboard] = useState(null);
    const [recentResources, setRecentResources] = useState([]);
    const [myCourses, setMyCourses] = useState([]);
    const [quizzes, setQuizzes] = useState([]);
    const nav = useNavigate();

    useEffect(() => {
        if (!user) { nav('/login'); return; }
        //const t = setTimeout(() => setLoading(false), 400);
        //return () => clearTimeout(t);
        const loadDashboard = async () => {
            try {
                const [dashboardRes, coursesRes, resourcesRes, quizzesRes] = await Promise.all([
                    authApis().get(endpoints["student-dashboard"]),
                    authApis().get(endpoints["my-enrollments"]),
                    Apis.get(endpoints["resources"]),
                    Apis.get(endpoints["quizzes"])
                ]);
                setDashboard(dashboardRes.data.data);

                setMyCourses(coursesRes.data.data || []);

                const resourcesData = resourcesRes.data.data;
                const resources = Array.isArray(resourcesData) ? resourcesData : resourcesData?.items || [];
                setRecentResources(resources.slice(0, 4));

                const quizzesData = quizzesRes.data.data;
                setQuizzes(Array.isArray(quizzesData) ? quizzesData.slice(0, 2) : []);
            } catch (err) {
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        loadDashboard();
    }, [user, nav]);

    if (loading) return <MySpinner />;

    const stats = [
        { val: dashboard?.completedResources ?? 0, lbl: "Tài liệu đã hoàn thành" },
        { val: `${dashboard?.totalStudyTime ?? 0} phút`, lbl: "Thời gian học" },
        { val: dashboard?.totalEnrollments ?? 0, lbl: "Khóa học" },
        { val: `${Math.round(dashboard?.learningProgress ?? 0)}%`, lbl: "Hoàn thành" },
    ];

    return (
        <Container className="py-4">
            <div className="welcome-banner">
                <h3>Xin chào, {user ? user.fullName : "Sinh viên"}</h3>
                <p>Tiếp tục hành trình học tập của bạn</p>
            </div>

            <Row className="g-3 mb-4">
                {stats.map((s, i) => (
                    <Col key={i} xs={6} md={3}>
                        <div className="dash-stat">
                            <div className="val">{s.val}</div>
                            <div className="lbl">{s.lbl}</div>
                        </div>
                    </Col>
                ))}
            </Row>

            <Row className="g-4">
                <Col lg={8}>
                    <div className="panel-card mb-4">
                        <div className="panel-head">Tài liệu mới nhất</div>
                        <ListGroup variant="flush">
                            {recentResources.length === 0 && (
                                <ListGroup.Item>Chưa có tài liệu</ListGroup.Item>
                            )}

                            {recentResources.map(r => (
                                <ListGroup.Item
                                    key={r.id}
                                    action
                                    onClick={() => nav(`/resources/${r.id}`)}
                                    className="d-flex justify-content-between"
                                    style={{ fontSize: "0.88rem" }}
                                >
                                    <span>
                                        <Badge bg="light" text="dark" className="me-2">
                                            {r.format || r.resourceTypeName || "RESOURCE"}
                                        </Badge>
                                        {r.title}
                                    </span>
                                    <small className="text-muted">
                                        {r.createdAt || ""}
                                    </small>
                                </ListGroup.Item>
                            ))}
                        </ListGroup>
                    </div>

                    <div className="panel-card">
                        <div className="panel-head">Tiến độ khóa học</div>
                        <ListGroup variant="flush">
                            {myCourses.length === 0 && (
                                <ListGroup.Item>Bạn chưa đăng ký khóa học nào</ListGroup.Item>
                            )}

                            {myCourses.map(e => {
                                const courseId = e.courseId || e.id;
                                const courseName = e.courseName || e.name || "Khóa học";
                                const progress = e.overallProgress || e.progress || 0;

                                return (
                                    <ListGroup.Item
                                        key={e.id}
                                        action
                                        onClick={() => nav(`/courses/${courseId}`)}
                                        style={{ fontSize: "0.88rem" }}
                                    >
                                        <div className="d-flex justify-content-between mb-1">
                                            <span>{courseName}</span>
                                            <span className="text-muted">{progress}%</span>
                                        </div>
                                        <ProgressBar
                                            now={progress}
                                            variant={progress >= 80 ? "success" : progress >= 50 ? "info" : "warning"}
                                            style={{ height: "6px" }}
                                        />
                                    </ListGroup.Item>
                                );
                            })}
                        </ListGroup>
                    </div>
                </Col>

                <Col lg={4}>
                    <div className="panel-card mb-4">
                        <div className="panel-head">Bài kiểm tra</div>
                        <ListGroup variant="flush">
                            {quizzes.length === 0 && (
                                <ListGroup.Item>Chưa có bài kiểm tra</ListGroup.Item>
                            )}

                            {quizzes.map(q => (
                                <ListGroup.Item
                                    key={q.id}
                                    action
                                    onClick={() => nav(`/quizzes/${q.id}/take`)}
                                    style={{ fontSize: "0.85rem" }}
                                >
                                    <strong>{q.title || q.name}</strong><br />
                                    <small className="text-muted">{q.courseName || ""}</small><br />
                                    <Badge bg="light" text="dark" className="mt-1">
                                        {q.durationMinutes ? `${q.durationMinutes} phút` : "Quiz"}
                                    </Badge>
                                </ListGroup.Item>
                            ))}
                        </ListGroup>
                    </div>

                    <div className="panel-card">
                        <div className="panel-head">Gợi ý tài liệu</div>
                        <ListGroup variant="flush">
                            {recentResources.length === 0 && (
                                <ListGroup.Item>Chưa có gợi ý</ListGroup.Item>
                            )}

                            {recentResources.slice(0, 2).map(r => (
                                <ListGroup.Item
                                    key={r.id}
                                    action
                                    onClick={() => nav(`/resources/${r.id}`)}
                                    style={{ fontSize: "0.85rem" }}
                                >
                                    <strong>{r.title}</strong><br />
                                    <small className="text-muted">
                                        {r.description || "Tài liệu phù hợp với quá trình học của bạn"}
                                    </small>
                                </ListGroup.Item>
                            ))}
                        </ListGroup>
                    </div>
                </Col>
            </Row>
        </Container>
    );
};

export default StudentDashboard;

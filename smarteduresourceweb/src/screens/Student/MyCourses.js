import { useContext, useEffect, useState } from "react";
import { Badge, Button, Col, Container, ListGroup, ProgressBar, Row } from "react-bootstrap";
import { Link, useNavigate } from "react-router-dom";

import { MyUserContext } from "../../configs/Context";
import MySpinner from "../../components/common/MySpinner";
import { authApis, endpoints } from "../../configs/Apis";

const MyCourses = () => {
    const [user] = useContext(MyUserContext);
    const [enrollments, setEnrollments] = useState([]);
    const [loading, setLoading] = useState(true);
    const nav = useNavigate();

    useEffect(() => {
        if (!user) { nav('/login'); return; }
        const loadMyCourses = async () => {
            try {
                let res = await authApis().get(endpoints["my-enrollments"]);
                setEnrollments(res.data.data || []);
            } catch (err) {
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        loadMyCourses();
    }, [user, nav]);

    if (loading) return <MySpinner />;

    const active = enrollments.filter(e => e.status === "ACTIVE" || e.status === "SUCCESS" || e.status === "ENROLLED");
    const completed = enrollments.filter(e => e.status === "COMPLETED");

    const getCourseId = (e) => e.courseId || e.id;
    const getCourseName = (e) => e.courseName || e.name || "Khóa học";
    const getProgress = (e) => Math.round(e.overallProgress || e.progress || 0);

    const renderList = (list, title, emptyMsg) => (
        <div className="panel-card mb-4">
            <div className="panel-head d-flex justify-content-between align-items-center">
                <span>{title}</span>
                <Badge bg="secondary">{list.length}</Badge>
            </div>
            {list.length === 0 ? (
                <div className="p-4 text-center" style={{ fontSize: '0.88rem', color: 'var(--text-muted)' }}>
                    {emptyMsg}
                </div>
            ) : (
                <ListGroup variant="flush">
                    {list.map(e => (
                        <ListGroup.Item
                            key={e.id}
                            action
                            onClick={() => nav(`/courses/${e.courseId}/learn`)}
                            style={{ fontSize: '0.88rem', cursor: 'pointer' }}
                        >
                            <Row className="align-items-center g-2">
                                <Col xs={12} md={5}>
                                    <strong>{e.courseName}</strong>
                                    {e.studentCode && (
                                        <><br /><small className="text-muted">MSSV: {e.studentCode}</small></>
                                    )}
                                </Col>
                                <Col xs={8} md={4}>
                                    <div className="d-flex align-items-center gap-2">
                                        <ProgressBar
                                            now={e.overallProgress || 0}
                                            className="flex-grow-1"
                                            style={{ height: '6px' }}
                                            variant={(e.overallProgress || 0) >= 80 ? "success" : "info"}
                                        />
                                        <small className="text-muted">{e.overallProgress || 0}%</small>
                                    </div>
                                </Col>
                                <Col xs={4} md={2} className="text-end">
                                    <Badge
                                        bg={e.status === "COMPLETED" ? "success" : "primary"}
                                        style={{ fontSize: '0.72rem' }}
                                    >
                                        {e.status === "COMPLETED" ? "Hoàn thành" : "Đang học"}
                                    </Badge>
                                </Col>
                            </Row>
                        </ListGroup.Item>
                    ))}
                </ListGroup>
            )}
        </div>
    );

    return (
        <Container className="py-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 style={{ fontSize: '1.35rem', fontWeight: 700, margin: 0 }}>Khóa học của tôi</h2>
                <Link to="/courses" style={{ fontSize: '0.85rem' }}>Khám phá thêm →</Link>
            </div>

            {enrollments.length === 0 ? (
                <div className="panel-card p-5 text-center">
                    <div style={{ fontSize: '2.5rem', marginBottom: '12px' }}>📚</div>
                    <h5 style={{ fontWeight: 700, marginBottom: '8px' }}>Bạn chưa đăng ký khóa học nào</h5>
                    <p style={{ fontSize: '0.88rem', color: 'var(--text-muted)', marginBottom: '20px' }}>
                        Khám phá hàng trăm khóa học chất lượng cao và bắt đầu hành trình học tập của bạn.
                    </p>
                    <Button className="cd-enroll-btn" onClick={() => nav('/courses')}>
                        Khám phá khóa học
                    </Button>
                </div>
            ) : (
                <>
                    {renderList(active, "Đang học", "Bạn chưa có khóa học đang học nào.")}
                    {completed.length > 0 && renderList(completed, "Đã hoàn thành", "")}
                </>
            )}
        </Container>
    );
}
export default MyCourses;

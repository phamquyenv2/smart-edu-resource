import { useEffect, useState } from "react";
import { Col, Container, Row } from "react-bootstrap";
import { Link, useNavigate } from "react-router-dom";

import MySpinner from "../../components/common/MySpinner";
import ResourceCard from "../../components/common/ResourceCard";
import CourseCard from "../../components/common/CourseCard";
import Apis, { endpoints } from "../../configs/Apis";

const Home = () => {
    const [loading, setLoading] = useState(true);
    const [resources, setResources] = useState([]);
    const [courses, setCourses] = useState([]);
    const [subjects, setSubjects] = useState([]);
    const nav = useNavigate();

    useEffect(() => {
        const loadHome = async () => {
            try {
                setLoading(true);

                const [resourcesRes, coursesRes, subjectsRes] = await Promise.all([
                    Apis.get(endpoints["resources"]),
                    Apis.get(endpoints["courses"]),
                    Apis.get(endpoints["subjects"])
                ]);

                const resourcePayload = Array.isArray(resourcesRes.data)
                    ? resourcesRes.data
                    : resourcesRes.data.data || [];
                const resourceData = Array.isArray(resourcePayload)
                    ? resourcePayload
                    : resourcePayload.items || [];

                const courseData = Array.isArray(coursesRes.data)
                    ? coursesRes.data
                    : coursesRes.data.data || [];

                const subjectData = Array.isArray(subjectsRes.data)
                    ? subjectsRes.data
                    : subjectsRes.data.data || [];

                setResources(resourceData);
                setCourses(courseData);
                setSubjects(subjectData);
            } catch (err) {
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        loadHome();
    }, []);

    if (loading) return <MySpinner />;

    return (
        <div>
            <section className="hero">
                <Container>
                    <div className="hero-pill">
                        ✨ Trợ lý AI cá nhân hóa lộ trình học
                    </div>
                    <h1>
                        Nền tảng
                        <span className="highlight">Học liệu số thông minh</span>
                    </h1>
                    <p>
                        Nền tảng học tập thông minh kết hợp nguồn tài nguyên phong phú và trợ lý ảo
                        phân tích sâu, giúp bạn chinh phục tri thức một cách tối ưu và chính xác nhất.
                    </p>
                    <div className="hero-buttons">
                        <button className="btn-hero-primary" onClick={() => nav('/courses')}>
                            Bắt đầu học ngay &nbsp;→
                        </button>
                        <button className="btn-hero-outline" onClick={() => nav('/resources')}>
                            Khám phá tài liệu
                        </button>
                    </div>
                </Container>
            </section>

            <Container>
                <Row className="g-3 stat-row">
                    {[
                        { num: `${resources.length}+`, label: "Tài liệu" },
                        { num: `${courses.length}+`, label: "Khóa học" },
                        { num: "10+", label: "Sinh viên" },
                        { num: "10+", label: "Giảng viên" },
                    ].map((s, i) => (
                        <Col key={i} xs={6} md={3}>
                            <div className="stat-box">
                                <div className="num">{s.num}</div>
                                <div className="label">{s.label}</div>
                            </div>
                        </Col>
                    ))}
                </Row>
            </Container>

            <section className="section">
                <Container>
                    <div className="section-head">
                        <h2>Tài liệu nổi bật</h2>
                        <Link to="/resources">Xem tất cả</Link>
                    </div>
                    <Row className="g-3">
                        {resources.slice(0, 6).map(r => (
                            <Col key={r.id} xs={12} sm={6} lg={4}>
                                <ResourceCard resource={r} />
                            </Col>
                        ))}
                    </Row>
                </Container>
            </section>

            <section className="section" style={{ background: '#fff' }}>
                <Container>
                    <div className="section-head">
                        <h2>Khóa học phổ biến</h2>
                        <Link to="/courses">Xem tất cả</Link>
                    </div>
                    <Row className="g-3">
                        {courses.slice(0, 4).map(c => (
                            <Col key={c.id} xs={12} sm={6} lg={3}>
                                <CourseCard course={c} />
                            </Col>
                        ))}
                    </Row>
                </Container>
            </section>

            <section className="section">
                <Container>
                    <h2 style={{ textAlign: 'center', marginBottom: '20px', fontSize: '1.35rem' }}>Danh mục môn học</h2>
                    <Row className="g-3 justify-content-center">
                        {subjects.map(s => (
                            <Col key={s.id} xs={6} sm={4} md={3} lg={2}>
                                <div className="cat-card" onClick={() => nav(`/resources?subjectId=${s.id}`)}>
                                    <p className="cat-name">{s.name}</p>
                                </div>
                            </Col>
                        ))}
                    </Row>
                </Container>
            </section>
        </div>
    );
}

export default Home;

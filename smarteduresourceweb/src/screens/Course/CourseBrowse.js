import { useEffect, useState } from "react";
import { Button, Col, Container, Form, Row } from "react-bootstrap";
import { useNavigate } from "react-router-dom";

import MySpinner from "../../components/common/MySpinner";
import CourseCard from "../../components/common/CourseCard";
import Apis, { endpoints } from "../../configs/Apis";

const CourseBrowse = () => {
    const [courses, setCourses] = useState([]);
    const [subjects, setSubjects] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filters, setFilters] = useState({ kw: "", subjectId: "", priceType: "" });
    const nav = useNavigate();
    const [page, setPage] = useState(1);
    const pageSize = 10;
    const [hasNextPage, setHasNextPage] = useState(false);

    useEffect(() => {
        const load = async () => {
            try {
                setLoading(true);

                const params = [`page=${page}`];

                if (filters.kw)
                    params.push(`keyword=${encodeURIComponent(filters.kw)}`);

                if (filters.subjectId)
                    params.push(`subjectId=${filters.subjectId}`);

                if (filters.priceType === "free")
                    params.push("isPaid=false");

                if (filters.priceType === "paid")
                    params.push("isPaid=true");

                const courseUrl = endpoints["courses"] + "?" + params.join("&");

                const countParams = params.filter(p => !p.startsWith("page="));
                const countUrl = endpoints["courses-count"] +
                    (countParams.length > 0 ? "?" + countParams.join("&") : "");

                const [coursesRes, countRes, subjectsRes] = await Promise.all([
                    Apis.get(courseUrl),
                    Apis.get(countUrl),
                    Apis.get(endpoints["subjects"])
                ]);

                const courseData = Array.isArray(coursesRes.data)
                    ? coursesRes.data
                    : coursesRes.data.data || [];


                if (courseData.length === 0 && page > 1) {
                    setPage(page - 1);
                    return;
                }

                const subjectData = Array.isArray(subjectsRes.data)
                    ? subjectsRes.data
                    : subjectsRes.data.data || [];



                setCourses(courseData);
                setSubjects(subjectData);
                const totalCourses = countRes.data.data || 0;
                setHasNextPage(page * pageSize < totalCourses);
            } catch (ex) {
                console.error(ex);
                setCourses([]);
            } finally {
                setLoading(false);
            }
        };

        load();
    }, [filters, page]);

    const updateFilter = (field, value) => {
        setPage(1);
        setFilters(prev => ({
            ...prev,
            [field]: value
        }));
    };

    const clearFilters = () => {
        setPage(1);
        setFilters({ kw: "", subjectId: "", priceType: "" });
    };

    return (
        <Container className="py-4">
            <h2 style={{ fontSize: "1.35rem", fontWeight: 700, marginBottom: "20px" }}>
                Khóa học
            </h2>

            <Row className="mb-4 g-2">
                <Col md={4}>
                    <Form.Control
                        type="text"
                        placeholder="Tìm kiếm khóa học..."
                        value={filters.kw}
                        onChange={e => updateFilter("kw", e.target.value)}
                        style={{ fontSize: "0.88rem" }}
                    />
                </Col>

                <Col md={3}>
                    <Form.Select
                        value={filters.subjectId}
                        onChange={e => updateFilter("subjectId", e.target.value)}
                        style={{ fontSize: "0.88rem" }}
                    >
                        <option value="">Tất cả môn học</option>
                        {subjects.map(s => (
                            <option key={s.id} value={s.id}>
                                {s.name}
                            </option>
                        ))}
                    </Form.Select>
                </Col>

                <Col md={3}>
                    <Form.Select
                        value={filters.priceType}
                        onChange={e => updateFilter("priceType", e.target.value)}
                        style={{ fontSize: "0.88rem" }}
                    >
                        <option value="">Tất cả</option>
                        <option value="free">Miễn phí</option>
                        <option value="paid">Có phí</option>
                    </Form.Select>
                </Col>

                <Col md={2}>
                    <Button
                        variant="outline-secondary"
                        className="w-100"
                        size="sm"
                        onClick={clearFilters}
                    >
                        Xóa lọc
                    </Button>
                </Col>
            </Row>

            {loading ? (
                <MySpinner />
            ) : courses.length === 0 ? (
                <div className="empty-state">
                    <h5>Không tìm thấy khóa học</h5>
                </div>
            ) : (
                <>
                    <Row className="g-3">
                        {courses.map(c => (
                            <Col key={c.id} xs={12} sm={6} lg={4}>
                                <CourseCard course={c} />
                            </Col>
                        ))}
                    </Row>

                    <div className="d-flex justify-content-center gap-2 mt-4">
                        <Button
                            variant="outline-secondary"
                            size="sm"
                            disabled={page === 1}
                            onClick={() => setPage(page - 1)}
                        >
                            Trang trước
                        </Button>

                        <Button variant="light" size="sm" disabled>
                            Trang {page}
                        </Button>

                        <Button
                            variant="outline-secondary"
                            size="sm"
                            disabled={!hasNextPage}
                            onClick={() => setPage(page + 1)}
                        >
                            Trang sau
                        </Button>
                    </div>
                </>
            )}
        </Container>
    );
};
export default CourseBrowse;

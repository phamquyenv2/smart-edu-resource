import { useEffect, useState } from "react";
import { Button, Col, Container, Form, Pagination, Row } from "react-bootstrap";
import { useNavigate, useSearchParams } from "react-router-dom";

import MySpinner from "../../components/common/MySpinner";
import CourseCard from "../../components/common/CourseCard";
import Apis, { endpoints } from "../../configs/Apis";

const CourseBrowse = () => {
    const [courses, setCourses] = useState([]);
    const [subjects, setSubjects] = useState([]);
    const [loading, setLoading] = useState(true);
    const [q] = useSearchParams();
    const nav = useNavigate();
    const kwParam = q.get("kw") || "";
    const subjectIdParam = q.get("subjectId") || "";
    const priceTypeParam = q.get("priceType") || "";
    const pageParam = Number.parseInt(q.get("page"), 10);
    const currentPage = Number.isInteger(pageParam) && pageParam > 0 ? pageParam : 1;
    const [filters, setFilters] = useState({ kw: kwParam, subjectId: subjectIdParam, priceType: priceTypeParam });
    const [searchText, setSearchText] = useState(kwParam);
    const pageSize = 9;
    const [totalPages, setTotalPages] = useState(1);

    useEffect(() => {
        const load = async () => {
            try {
                setLoading(true);

                const params = [`page=${currentPage}`];

                if (kwParam)
                    params.push(`keyword=${encodeURIComponent(kwParam)}`);

                if (subjectIdParam)
                    params.push(`subjectId=${subjectIdParam}`);

                if (priceTypeParam === "free")
                    params.push("isPaid=false");

                if (priceTypeParam === "paid")
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


                if (courseData.length === 0 && currentPage > 1) {
                    const fallbackParams = new URLSearchParams();
                    if (kwParam)
                        fallbackParams.set("kw", kwParam);
                    if (subjectIdParam)
                        fallbackParams.set("subjectId", subjectIdParam);
                    if (priceTypeParam)
                        fallbackParams.set("priceType", priceTypeParam);
                    if (currentPage - 1 > 1)
                        fallbackParams.set("page", currentPage - 1);

                    nav(fallbackParams.toString() ? `?${fallbackParams.toString()}` : "?");
                    return;
                }

                const subjectData = Array.isArray(subjectsRes.data)
                    ? subjectsRes.data
                    : subjectsRes.data.data || [];



                setCourses(courseData);
                setSubjects(subjectData);
                const totalCourses = countRes.data.data || 0;
                setTotalPages(Math.max(1, Math.ceil(totalCourses / pageSize)));
            } catch (ex) {
                console.error(ex);
                setCourses([]);
            } finally {
                setLoading(false);
            }
        };

        load();
    }, [kwParam, subjectIdParam, priceTypeParam, currentPage, nav]);

    useEffect(() => {
        setFilters({ kw: kwParam, subjectId: subjectIdParam, priceType: priceTypeParam });
        setSearchText(kwParam);
    }, [kwParam, subjectIdParam, priceTypeParam]);

    const buildFilterParams = (nextFilters, page = 1) => {
        const params = new URLSearchParams();

        if (nextFilters.kw?.trim())
            params.set("kw", nextFilters.kw.trim());
        if (nextFilters.subjectId)
            params.set("subjectId", nextFilters.subjectId);
        if (nextFilters.priceType)
            params.set("priceType", nextFilters.priceType);
        if (page > 1)
            params.set("page", page);

        return params;
    };

    const updateFilter = (field, value) => {
        const nextFilters = { ...filters, [field]: value };
        setFilters(nextFilters);
        const params = buildFilterParams(nextFilters);
        nav(params.toString() ? `?${params.toString()}` : "?");
    };

    const clearFilters = () => {
        setSearchText("");
        setFilters({ kw: "", subjectId: "", priceType: "" });
        nav("?");
    };

    const handlePageChange = (page) => {
        const params = buildFilterParams(filters, page);
        nav(params.toString() ? `?${params.toString()}` : "?");
    };

    const handleSearchKeyDown = (e) => {
        if (e.key === "Enter") {
            const nextFilters = { ...filters, kw: searchText.trim() };
            setFilters(nextFilters);
            const params = buildFilterParams(nextFilters);
            nav(params.toString() ? `?${params.toString()}` : "?");
        }
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
                        value={searchText}
                        onChange={e => setSearchText(e.target.value)}
                        onKeyDown={handleSearchKeyDown}
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

                    {totalPages > 1 && (
                        <div className="d-flex justify-content-center mt-4">
                            <Pagination>
                                {Array.from({ length: totalPages }, (_, i) => i + 1).map(num => (
                                    <Pagination.Item key={num} active={num === currentPage} onClick={() => handlePageChange(num)}>
                                        {num}
                                    </Pagination.Item>
                                ))}
                            </Pagination>
                        </div>
                    )}
                </>
            )}
        </Container>
    );
};
export default CourseBrowse;

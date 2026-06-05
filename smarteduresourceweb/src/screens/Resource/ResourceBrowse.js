import { useEffect, useState } from "react";
import { Badge, Button, Col, Container, Form, Pagination, Row } from "react-bootstrap";
import { useNavigate, useSearchParams } from "react-router-dom";

import MySpinner from "../../components/common/MySpinner";
import ResourceCard from "../../components/common/ResourceCard";
import Apis, { endpoints } from "../../configs/Apis";

const ResourceBrowse = () => {
    const [resources, setResources] = useState([]);
    const [subjects, setSubjects] = useState([]);
    const [topics, setTopics] = useState([]);
    const [resourceTypes, setResourceTypes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [q] = useSearchParams();
    const kwParam = q.get("kw") || "";
    const subjectIdParam = q.get("subjectId") || "";
    const topicIdParam = q.get("topicId") || "";
    const typeIdParam = q.get("typeId") || "";
    const levelParam = q.get("level") || "";
    const pageParam = Number.parseInt(q.get("page"), 10);
    const currentPage = Number.isInteger(pageParam) && pageParam > 0 ? pageParam : 1;
    const [filters, setFilters] = useState({
        kw: kwParam,
        subjectId: subjectIdParam,
        topicId: topicIdParam,
        typeId: typeIdParam,
        level: levelParam,
    });
    const [sortBy, setSortBy] = useState("newest");
    const [totalItems, setTotalItems] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const nav = useNavigate();

    useEffect(() => {
        const loadResources = async () => {
            try {
                setLoading(true);

                let url = endpoints["resources"];

                const params = [];
                params.push(`page=${currentPage}`);

                if (kwParam)
                    params.push(`keyword=${encodeURIComponent(kwParam)}`);

                if (subjectIdParam)
                    params.push(`subjectId=${subjectIdParam}`);

                if (topicIdParam)
                    params.push(`topicId=${topicIdParam}`);

                if (typeIdParam)
                    params.push(`typeId=${typeIdParam}`);

                if (levelParam)
                    params.push(`level=${levelParam}`);

                if (params.length > 0)
                    url += "?" + params.join("&");

                const [resourcesRes, subjectsRes, topicsRes, typesRes] = await Promise.all([
                    Apis.get(url),
                    Apis.get(endpoints["subjects"]),
                    Apis.get(endpoints["topics"]),
                    Apis.get(endpoints["resource-types"])
                ]);

                const pageData = resourcesRes.data.data;
                let list = pageData?.items || [];

                list.sort((a, b) => {
                    const dateA = new Date(a.createdAt || 0).getTime();
                    const dateB = new Date(b.createdAt || 0).getTime();
                    return sortBy === "oldest" ? dateA - dateB : dateB - dateA;
                });

                setResources(list);
                setTotalItems(pageData?.totalItems || 0);
                setTotalPages(pageData?.totalPages || 1);
                setSubjects(subjectsRes.data.data || []);
                setTopics(topicsRes.data.data || []);
                setResourceTypes(typesRes.data.data || []);

            } catch (err) {
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        loadResources();
    }, [kwParam, subjectIdParam, topicIdParam, typeIdParam, levelParam, currentPage, sortBy]);

    useEffect(() => {
        setFilters({
            kw: kwParam,
            subjectId: subjectIdParam,
            topicId: topicIdParam,
            typeId: typeIdParam,
            level: levelParam,
        });
    }, [kwParam, subjectIdParam, topicIdParam, typeIdParam, levelParam]);

    const buildFilterParams = (nextFilters, page = 1) => {
        const params = new URLSearchParams();

        if (nextFilters.kw?.trim())
            params.set("kw", nextFilters.kw.trim());
        if (nextFilters.subjectId)
            params.set("subjectId", nextFilters.subjectId);
        if (nextFilters.topicId)
            params.set("topicId", nextFilters.topicId);
        if (nextFilters.typeId)
            params.set("typeId", nextFilters.typeId);
        if (nextFilters.level)
            params.set("level", nextFilters.level);
        if (page > 1)
            params.set("page", page);

        return params;
    };

    const handleSearch = (e) => {
        e.preventDefault();
        nav(`?${buildFilterParams(filters).toString()}`);
    };

    const handleFilterChange = (field, value) => {
        const nextFilters = { ...filters, [field]: value };
        setFilters(nextFilters);
        nav(`?${buildFilterParams(nextFilters).toString()}`);
    };

    const handlePageChange = (page) => {
        nav(`?${buildFilterParams(filters, page).toString()}`);
    };

    const clearFilters = () => {
        setFilters({ kw: "", subjectId: "", topicId: "", typeId: "", level: "" });
        nav("?");
    };

    return (
        <Container fluid className="py-4 px-4">
            <Row>
                <Col lg={3} className="mb-4">
                    <div className="filter-panel">
                        <h5>Bộ lọc</h5>
                        <Form.Group className="mb-3">
                            <Form.Label>Từ khóa</Form.Label>
                            <Form.Control type="text" placeholder="Tìm kiếm..." value={filters.kw} onChange={e => setFilters({ ...filters, kw: e.target.value })} onKeyDown={e => { if (e.key === "Enter") handleSearch(e); }} />
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Môn học</Form.Label>
                            <Form.Select value={filters.subjectId} onChange={e => handleFilterChange("subjectId", e.target.value)}>
                                <option value="">Tất cả</option>
                                {subjects.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                            </Form.Select>
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Chủ đề</Form.Label>
                            <Form.Select value={filters.topicId} onChange={e => handleFilterChange("topicId", e.target.value)}>
                                <option value="">Tất cả</option>
                                {topics.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
                            </Form.Select>
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Loại</Form.Label>
                            <Form.Select value={filters.typeId} onChange={e => handleFilterChange("typeId", e.target.value)}>
                                <option value="">Tất cả</option>
                                {resourceTypes.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
                            </Form.Select>
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Cấp độ</Form.Label>
                            <Form.Select value={filters.level} onChange={e => handleFilterChange("level", e.target.value)}>
                                <option value="">Tất cả</option>
                                <option value="BEGINNER">Cơ bản</option>
                                <option value="INTERMEDIATE">Trung bình</option>
                                <option value="ADVANCED">Nâng cao</option>
                            </Form.Select>
                        </Form.Group>
                        <Button variant="outline-secondary" size="sm" className="w-100" onClick={clearFilters}>Xóa bộ lọc</Button>
                    </div>
                </Col>
                <Col lg={9}>
                    <div className="d-flex justify-content-between align-items-center mb-3">
                        <h4 className="mb-0 d-flex align-items-center gap-2 text-primary" style={{ fontSize: '1.25rem', fontWeight: 700 }}>
                            Tài liệu 
                            <Badge pill bg="secondary" className="fw-medium text-white" style={{ fontSize: '0.85rem', padding: '0.4em 0.7em', opacity: 0.85 }}>
                                {totalItems}
                            </Badge>
                        </h4>
                        <Form.Select value={sortBy} onChange={e => setSortBy(e.target.value)} style={{ width: '180px', fontSize: '0.85rem' }}>
                            <option value="newest">Mới nhất</option>
                            <option value="oldest">Cũ nhất</option>
                        </Form.Select>
                    </div>
                    {loading ? <MySpinner /> : resources.length === 0 ? (
                        <div className="empty-state"><h5>Không tìm thấy tài liệu</h5><p>Thử thay đổi bộ lọc</p></div>
                    ) : (
                        <>
                            <Row className="g-3">
                                {resources.map(r => (
                                    <Col key={r.id} xs={12} sm={6} xl={4}>
                                        <ResourceCard resource={r} />
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
                </Col>
            </Row>
        </Container>
    );
}
export default ResourceBrowse;

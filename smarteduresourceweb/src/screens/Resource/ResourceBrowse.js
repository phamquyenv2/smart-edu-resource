import { useEffect, useState } from "react";
import { Badge, Button, Col, Container, Form, Row } from "react-bootstrap";
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
    const [searchParams] = useSearchParams();
    const [filters, setFilters] = useState({
        kw: searchParams.get("kw") || "",
        subjectId: searchParams.get("subjectId") || "",
        topicId: "", typeId: "", level: "",
    });
    const [sortBy, setSortBy] = useState("newest");
    const nav = useNavigate();

    useEffect(() => {
        const loadResources = async () => {
            try {
                setLoading(true);

                let url = endpoints["resources"];

                const params = [];

                if (filters.kw)
                    params.push(`kw=${encodeURIComponent(filters.kw)}`);

                if (filters.subjectId)
                    params.push(`subjectId=${filters.subjectId}`);

                if (filters.topicId)
                    params.push(`topicId=${filters.topicId}`);

                if (filters.typeId)
                    params.push(`typeId=${filters.typeId}`);

                if (filters.level)
                    params.push(`level=${filters.level}`);

                if (params.length > 0)
                    url += "?" + params.join("&");

                const [resourcesRes, subjectsRes, topicsRes, typesRes] = await Promise.all([
                    Apis.get(url),
                    Apis.get(endpoints["subjects"]),
                    Apis.get(endpoints["topics"]),
                    Apis.get(endpoints["resource-types"])
                ]);

                let list = resourcesRes.data.data || [];

                if (sortBy === "az") {
                    list.sort((a, b) =>
                        (a.title || "").localeCompare(b.title || "")
                    );
                }

                setResources(list);
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
    }, [filters, sortBy]);

    const clearFilters = () => setFilters({ kw: "", subjectId: "", topicId: "", typeId: "", level: "" });

    return (
        <Container fluid className="py-4 px-4">
            <Row>
                <Col lg={3} className="mb-4">
                    <div className="filter-panel">
                        <h5>Bộ lọc</h5>
                        <Form.Group className="mb-3">
                            <Form.Label>Từ khóa</Form.Label>
                            <Form.Control type="text" placeholder="Tìm kiếm..." value={filters.kw} onChange={e => setFilters({ ...filters, kw: e.target.value })} />
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Môn học</Form.Label>
                            <Form.Select value={filters.subjectId} onChange={e => setFilters({ ...filters, subjectId: e.target.value })}>
                                <option value="">Tất cả</option>
                                {subjects.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                            </Form.Select>
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Chủ đề</Form.Label>
                            <Form.Select value={filters.topicId} onChange={e => setFilters({ ...filters, topicId: e.target.value })}>
                                <option value="">Tất cả</option>
                                {topics.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
                            </Form.Select>
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Loại</Form.Label>
                            <Form.Select value={filters.typeId} onChange={e => setFilters({ ...filters, typeId: e.target.value })}>
                                <option value="">Tất cả</option>
                                {resourceTypes.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
                            </Form.Select>
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Cấp độ</Form.Label>
                            <Form.Select value={filters.level} onChange={e => setFilters({ ...filters, level: e.target.value })}>
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
                        <h4 className="mb-0" style={{ fontSize: '1.1rem', fontWeight: 600 }}>
                            Tài liệu <Badge bg="secondary" style={{ fontSize: '0.75rem' }}>{resources.length}</Badge>
                        </h4>
                        <Form.Select value={sortBy} onChange={e => setSortBy(e.target.value)} style={{ width: '180px', fontSize: '0.85rem' }}>
                            <option value="newest">Mới nhất</option>
                            <option value="oldest">Cũ nhất</option>
                            <option value="az">A → Z</option>
                        </Form.Select>
                    </div>
                    {loading ? <MySpinner /> : resources.length === 0 ? (
                        <div className="empty-state"><h5>Không tìm thấy tài liệu</h5><p>Thử thay đổi bộ lọc</p></div>
                    ) : (
                        <Row className="g-3">
                            {resources.map(r => (
                                <Col key={r.id} xs={12} sm={6} xl={4}>
                                    <ResourceCard resource={r} />
                                </Col>
                            ))}
                        </Row>
                    )}
                </Col>
            </Row>
        </Container>
    );
}
export default ResourceBrowse;

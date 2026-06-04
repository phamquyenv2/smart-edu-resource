import React, { useContext, useEffect, useRef, useState } from "react";
import { Alert, Button, Form, Row, Col, Badge, ListGroup, InputGroup, Spinner } from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";
import { MyUserContext } from "../../configs/Context";
import Apis, { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/common/MySpinner";
import "./Lecturer.css";

const LecturerResourceForm = () => {
    const [user] = useContext(MyUserContext);
    const { id } = useParams();
    const nav = useNavigate();

    const [loading, setLoading] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [err, setErr] = useState("");

    const [subjects, setSubjects] = useState([]);
    const [resourceTypes, setResourceTypes] = useState([]);

    // Form data
    const [formData, setFormData] = useState({
        title: "",
        description: "",
        level: "",
        subjectIds: [],
        typeIds: [],
        relatedResourceIds: [],
    });

    const fileRef = useRef();
    const thumbnailRef = useRef();

    // Related resources state
    const [searchRelKw, setSearchRelKw] = useState("");
    const [searchResults, setSearchResults] = useState([]);
    const [selectedRelated, setSelectedRelated] = useState([]);
    const [searchingRel, setSearchingRel] = useState(false);

    useEffect(() => {
        if (!user || (user.role !== "LECTURER" && user.role !== "ADMIN")) {
            nav('/login'); return;
        }

        const initData = async () => {
            setLoading(true);
            try {
                await Promise.all([loadSubjects(), loadResourceTypes()]);
                if (id) {
                    await loadResourceDetail(id);
                }
            } catch (error) {
                setErr("Lỗi khi tải dữ liệu khởi tạo.");
            } finally {
                setLoading(false);
            }
        };

        initData();
    }, [user, nav, id]);

    const handleSearchKeyDown = (e) => {
        if (e.key === 'Enter') {
            e.preventDefault(); // Ngăn submit form chính
            if (searchRelKw.trim().length > 0) {
                searchResources(searchRelKw);
            } else {
                setSearchResults([]);
            }
        }
    };

    const loadSubjects = async () => {
        let res = await Apis.get(endpoints['subjects']);
        setSubjects(res.data.data || []);
    };

    const loadResourceTypes = async () => {
        let res = await Apis.get(endpoints['resource-types']);
        setResourceTypes(res.data.data || []);
    };

    const loadResourceDetail = async (resourceId) => {
        try {
            let res = await authApis().get(endpoints['lecturer-resource-detail'](resourceId));
            const r = res.data.data;

            setFormData({
                title: r.title || "",
                description: r.description || "",
                level: r.level || "",
                subjectIds: r.subjects?.map(s => s.id) || [],
                typeIds: r.types?.map(t => t.id) || [],
                relatedResourceIds: r.relatedResources?.map(resource => resource.id) || [],
            });

            if (r.relatedResources) {
                setSelectedRelated(r.relatedResources);
            }
        } catch (ex) {
            setErr("Không thể tải thông tin học liệu.");
        }
    };

    const searchResources = async (keyword) => {
        setSearchingRel(true);
        try {
            let url = endpoints['lecturer-resources'] + `?page=1&keyword=${keyword}`;
            let res = await authApis().get(url);
            let results = res.data.data?.items || [];
            if (id) {
                results = results.filter(r => r.id.toString() !== id.toString());
            }
            setSearchResults(results);
        } catch (error) {
            console.error("Lỗi tìm kiếm học liệu", error);
        } finally {
            setSearchingRel(false);
        }
    };

    const handleAddRelated = (resource) => {
        if (!selectedRelated.find(r => r.id === resource.id)) {
            const newSelected = [...selectedRelated, resource];
            setSelectedRelated(newSelected);
            setFormData({
                ...formData,
                relatedResourceIds: newSelected.map(r => r.id)
            });
        }
    };

    const handleRemoveRelated = (resourceId) => {
        const newSelected = selectedRelated.filter(r => r.id !== resourceId);
        setSelectedRelated(newSelected);
        setFormData({
            ...formData,
            relatedResourceIds: newSelected.map(r => r.id)
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            setIsSubmitting(true);
            setErr("");
            let data = new FormData();

            Object.entries(formData).forEach(([key, value]) => {
                if (value !== undefined && value !== null && value !== "") {
                    if (Array.isArray(value)) {
                        value.forEach(item => data.append(key, item));
                    } else {
                        data.append(key, value);
                    }
                }
            });

            if (fileRef.current && fileRef.current.files[0]) {
                data.append("file", fileRef.current.files[0]);
            } else if (!id) {
                setErr("Vui lòng chọn file tài liệu.");
                setIsSubmitting(false);
                return;
            }

            if (thumbnailRef.current && thumbnailRef.current.files[0]) {
                data.append("thumbnailFile", thumbnailRef.current.files[0]);
            }

            if (id) {
                await authApis().put(endpoints['lecturer-resource-detail'](id), data, {
                    headers: { 'Content-Type': 'multipart/form-data' }
                });
            } else {
                await authApis().post(endpoints['lecturer-resources'], data, {
                    headers: { 'Content-Type': 'multipart/form-data' }
                });
            }

            nav('/lecturer/resources');
        } catch (ex) {
            console.error(ex);
            setErr("Có lỗi xảy ra khi lưu học liệu.");
        } finally {
            setIsSubmitting(false);
        }
    };

    if (loading) return <MySpinner />;

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h4 className="mb-0">
                    <Button variant="link" className="text-decoration-none p-0 me-2 text-dark" onClick={() => nav('/lecturer/resources')}>
                        <i className="bi bi-arrow-left"></i>
                    </Button>
                    {id ? "Sửa học liệu" : "Upload học liệu mới"}
                </h4>
            </div>

            {err && <Alert variant="danger">{err}</Alert>}

            <Form onSubmit={handleSubmit}>
                <Row>
                    <Col lg={8} className="mb-4">
                        <div className="lecturer-panel mb-0">
                            <div className="panel-header">Thông tin cơ bản</div>
                            <div className="p-4">
                                <Form.Group className="mb-3">
                                    <Form.Label className="fw-semibold">Tiêu đề <span className="text-danger">*</span></Form.Label>
                                    <Form.Control
                                        type="text"
                                        placeholder="Nhập tiêu đề học liệu"
                                        value={formData.title}
                                        onChange={e => setFormData({ ...formData, title: e.target.value })}
                                        required
                                    />
                                </Form.Group>

                                <Row>
                                    <Col md={6}>
                                        <Form.Group className="mb-3">
                                            <Form.Label className="fw-semibold">Môn học <span className="text-danger">*</span></Form.Label>
                                            <Form.Select
                                                value={formData.subjectIds?.[0] || ''}
                                                onChange={e => setFormData({
                                                    ...formData,
                                                    subjectIds: e.target.value ? [parseInt(e.target.value)] : [],
                                                })}
                                                required
                                            >
                                                <option value="">-- Chọn môn học --</option>
                                                {subjects.map(s => (
                                                    <option key={s.id} value={s.id}>{s.name}</option>
                                                ))}
                                            </Form.Select>
                                        </Form.Group>
                                    </Col>
                                    <Col md={6}>
                                        <Form.Group className="mb-3">
                                            <Form.Label className="fw-semibold">Loại tài liệu <span className="text-danger">*</span></Form.Label>
                                            <Form.Select
                                                value={formData.typeIds?.[0] || ''}
                                                onChange={e => setFormData({
                                                    ...formData,
                                                    typeIds: e.target.value ? [parseInt(e.target.value)] : [],
                                                })}
                                                required
                                            >
                                                <option value="">-- Chọn loại tài liệu --</option>
                                                {resourceTypes.map(t => (
                                                    <option key={t.id} value={t.id}>{t.name}</option>
                                                ))}
                                            </Form.Select>
                                        </Form.Group>
                                    </Col>
                                </Row>

                                <Form.Group className="mb-3">
                                    <Form.Label className="fw-semibold">Độ khó (Level)</Form.Label>
                                    <Form.Select
                                        value={formData.level || ''}
                                        onChange={e => setFormData({ ...formData, level: e.target.value })}
                                    >
                                        <option value="">-- Chọn độ khó --</option>
                                        <option value="BEGINNER">Cơ bản (Beginner)</option>
                                        <option value="INTERMEDIATE">Trung bình (Intermediate)</option>
                                        <option value="ADVANCED">Nâng cao (Advanced)</option>
                                    </Form.Select>
                                </Form.Group>

                                <Form.Group className="mb-3">
                                    <Form.Label className="fw-semibold">Mô tả</Form.Label>
                                    <Form.Control
                                        as="textarea"
                                        rows={4}
                                        placeholder="Mô tả nội dung của tài liệu này..."
                                        value={formData.description || ''}
                                        onChange={e => setFormData({ ...formData, description: e.target.value })}
                                    />
                                </Form.Group>
                            </div>
                        </div>

                        <div className="lecturer-panel mt-4">
                            <div className="panel-header">Tập tin đính kèm</div>
                            <div className="p-4">
                                <Form.Group className="mb-4">
                                    <Form.Label className="fw-semibold">File tài liệu {id ? "(Để trống nếu không thay đổi)" : <span className="text-danger">*</span>}</Form.Label>
                                    <Form.Control type="file" ref={fileRef} />
                                </Form.Group>

                                <Form.Group className="mb-2">
                                    <Form.Label className="fw-semibold">Ảnh đại diện (Thumbnail)</Form.Label>
                                    <Form.Control type="file" accept="image/*" ref={thumbnailRef} />
                                </Form.Group>
                            </div>
                        </div>
                    </Col>

                    <Col lg={4}>
                        <div className="lecturer-panel sticky-top" style={{ top: "80px" }}>
                            <div className="panel-header">
                                <span><i className="bi bi-link-45deg me-2"></i>Tài liệu liên quan</span>
                                <div className="text-muted small mt-1 fw-normal">
                                    Gợi ý cho học viên các tài liệu tham khảo khác.
                                </div>
                            </div>
                            <div className="p-4">
                                <Form.Group className="mb-3">
                                    <InputGroup>
                                        <InputGroup.Text className="bg-white text-muted">
                                            <i className="bi bi-search"></i>
                                        </InputGroup.Text>
                                        <Form.Control
                                            type="text"
                                            placeholder="Nhập tên tài liệu..."
                                            value={searchRelKw}
                                            onChange={(e) => {
                                                setSearchRelKw(e.target.value);
                                                if (e.target.value.trim() === '') setSearchResults([]);
                                            }}
                                            onKeyDown={handleSearchKeyDown}
                                        />
                                    </InputGroup>
                                </Form.Group>

                                {searchRelKw.trim() && (
                                    <div className="border rounded bg-light p-2 mb-4" style={{ maxHeight: "200px", overflowY: "auto" }}>
                                        {searchingRel ? (
                                            <div className="text-center py-2"><Spinner animation="border" size="sm" /></div>
                                        ) : searchResults.length > 0 ? (
                                            <ListGroup variant="flush">
                                                {searchResults.map(res => (
                                                    <ListGroup.Item
                                                        key={res.id}
                                                        className="d-flex justify-content-between align-items-center px-2 py-1 bg-transparent border-0"
                                                    >
                                                        <div className="text-truncate me-2" style={{ maxWidth: "200px", fontSize: "0.85rem" }}>
                                                            {res.title}
                                                        </div>
                                                        <Button
                                                            variant="outline-primary"
                                                            size="sm"
                                                            className="p-0 px-1"
                                                            onClick={() => handleAddRelated(res)}
                                                            disabled={selectedRelated.some(r => r.id === res.id)}
                                                        >
                                                            <i className="bi bi-plus"></i>
                                                        </Button>
                                                    </ListGroup.Item>
                                                ))}
                                            </ListGroup>
                                        ) : (
                                            <div className="text-center text-muted small py-2">Không tìm thấy tài liệu</div>
                                        )}
                                    </div>
                                )}

                                <div>
                                    <h6 className="fw-semibold mb-2" style={{ fontSize: "0.9rem" }}>
                                        Đã chọn ({selectedRelated.length})
                                    </h6>
                                    {selectedRelated.length > 0 ? (
                                        <div className="d-flex flex-wrap gap-2">
                                            {selectedRelated.map(res => (
                                                <Badge
                                                    key={res.id}
                                                    bg="primary"
                                                    className="d-flex align-items-center p-2 rounded-pill fw-normal"
                                                    style={{ backgroundColor: "rgba(99, 102, 241, 0.1) !important", color: "#6366f1 !important", border: "1px solid #6366f1" }}
                                                >
                                                    <span className="text-truncate" style={{ maxWidth: "150px" }}>{res.title}</span>
                                                    <i
                                                        className="bi bi-x-circle-fill ms-2"
                                                        style={{ cursor: "pointer" }}
                                                        onClick={() => handleRemoveRelated(res.id)}
                                                    ></i>
                                                </Badge>
                                            ))}
                                        </div>
                                    ) : (
                                        <div className="text-muted small fst-italic border rounded p-3 text-center bg-light">
                                            Chưa có tài liệu liên quan nào được chọn. Hãy tìm kiếm và thêm ở trên.
                                        </div>
                                    )}
                                </div>
                            </div>
                            <div className="bg-white pt-0 pb-4 px-4 d-flex justify-content-end mt-3 gap-2">
                                <Button variant="light" onClick={() => nav('/lecturer/resources')} disabled={isSubmitting}>
                                    Hủy bỏ
                                </Button>
                                <Button variant="primary" type="submit" disabled={isSubmitting} style={{ backgroundColor: "#6366f1", borderColor: "#6366f1" }}>
                                    {isSubmitting ? (
                                        <><Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2" /> Đang lưu...</>
                                    ) : (
                                        <><i className="bi bi-cloud-arrow-up me-2"></i> {id ? "Cập nhật học liệu" : "Upload học liệu"}</>
                                    )}
                                </Button>
                            </div>
                        </div>
                    </Col>
                </Row>
            </Form>
        </>
    );
};

export default LecturerResourceForm;

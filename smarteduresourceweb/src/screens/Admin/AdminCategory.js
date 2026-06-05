import { useContext, useEffect, useState } from "react";
import { Alert, Button, Form, Modal, Nav, Pagination, Table } from "react-bootstrap";
import { useNavigate, useSearchParams } from "react-router-dom";

import { MyUserContext } from "../../configs/Context";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/common/MySpinner";
import useSubmissionGuard from "../../hooks/useSubmissionGuard";
import "./Admin.css";

const AdminCategory = () => {
    const [user] = useContext(MyUserContext);
    const [items, setItems] = useState([]);
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState("");
    const [showModal, setShowModal] = useState(false);
    const [editingItem, setEditingItem] = useState(null);
    const [formData, setFormData] = useState({});
    const { isSubmitting, runSubmission } = useSubmissionGuard();
    const nav = useNavigate();
    const [q] = useSearchParams();
    const activeTab = q.get("tab") || "subjects";
    const tabs = [
        {
            key: "subjects",
            label: "Môn học",
            listEndpoint: "admin-subjects",
            endpoint: "admin-subjects",
            detailEndpoint: "admin-subject-detail"
        }, {
            key: "topics",
            label: "Chủ đề",
            listEndpoint: "admin-topics",
            endpoint: "admin-topics",
            detailEndpoint: "admin-topic-detail"
        }, {
            key: "resource-tags",
            label: "Thẻ tài nguyên",
            listEndpoint: "admin-resource-tags",
            endpoint: "admin-resource-tags",
            detailEndpoint: "admin-resource-tag-detail"
        }, {
            key: "resource-types",
            label: "Loại tài liệu",
            listEndpoint: "admin-resource-types",
            endpoint: "admin-resource-types",
            detailEndpoint: "admin-resource-type-detail"
        },];

    const currentTab = tabs.find(t => t.key === activeTab) || tabs[0];
    useEffect(() => {
        if (!user || user.role !== "ADMIN") { nav('/login'); return; }
        loadItems();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [user, nav, activeTab, currentPage]);

    useEffect(() => {
        setCurrentPage(1);
    }, [activeTab]);

    const loadItems = async () => {
        try {
            setLoading(true);
            setErr("");
            let res = await authApis().get(endpoints[currentTab.listEndpoint], { params: { page: currentPage } });
            const pageData = res.data.data;
            if (pageData?.totalPages && currentPage > pageData.totalPages) {
                setCurrentPage(pageData.totalPages);
                return;
            }
            setItems(pageData?.items || []);
            setTotalPages(pageData?.totalPages || 1);
        } catch (ex) {
            console.error(ex);
            setErr("Không thể tải danh sách.");
        } finally {
            setLoading(false);
        }
    };

    const handleOpenCreate = () => {
        setEditingItem(null);
        setFormData({});
        setShowModal(true);
    };

    const handleOpenEdit = (item) => {
        setEditingItem(item);
        setFormData({ name: item.name || "", description: item.description || "" });
        setShowModal(true);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        await runSubmission(async () => {
            try {
                setErr("");
                if (editingItem) {
                    await authApis().put(endpoints[currentTab.detailEndpoint](editingItem.id), formData);
                } else {
                    await authApis().post(endpoints[currentTab.endpoint], formData);
                }
                setShowModal(false);
                if (!editingItem) {
                    if (currentPage === 1) {
                        loadItems();
                    } else {
                        setCurrentPage(1);
                    }
                } else {
                    loadItems();
                }
            } catch (ex) {
                console.error(ex);
                setErr(ex.response?.data?.message || "Có lỗi xảy ra khi lưu.");
            }
        });
    };

    const handleDelete = async (id) => {
        if (!window.confirm("Bạn có chắc chắn muốn xóa?")) return;
        try {
            await authApis().delete(endpoints[currentTab.detailEndpoint](id));
            loadItems();
        } catch (ex) {
            console.error(ex);
            setErr("Không thể xóa mục này.");
        }
    };

    const handleTabChange = (tab) => {
        const params = new URLSearchParams();
        if (tab !== "subjects") params.set("tab", tab);
        nav(`?${params.toString()}`);
    };

    return (
        <>
            <h4 className="mb-4">Quản lý Danh mục</h4>

            {err && <Alert variant="danger">{err}</Alert>}

            <Nav variant="tabs" className="mb-3">
                {tabs.map(t => (
                    <Nav.Item key={t.key}>
                        <Nav.Link active={activeTab === t.key} onClick={() => handleTabChange(t.key)}>
                            {t.label}
                        </Nav.Link>
                    </Nav.Item>
                ))}
            </Nav>

            <div className="d-flex justify-content-end mb-3">
                <Button variant="primary" size="sm" onClick={handleOpenCreate}>
                    <i className="bi bi-plus-lg me-1"></i> Thêm {currentTab.label}
                </Button>
            </div>

            {loading ? <MySpinner /> : (
                <div className="admin-panel">
                    <Table hover responsive className="mb-0" style={{ tableLayout: 'fixed' }}>
                        <thead>
                            <tr>
                                <th style={{ width: '10%' }}>ID</th>
                                <th style={{ width: activeTab === 'subjects' ? '40%' : '70%' }}>Tên</th>
                                {activeTab === 'subjects' && <th style={{ width: '30%' }}>Mô tả</th>}
                                <th style={{ width: '20%' }}>Hành động</th>
                            </tr>
                        </thead>
                        <tbody>
                            {items.map(item => (
                                <tr key={item.id}>
                                    <td>{item.id}</td>
                                    <td className="text-truncate">{item.name}</td>
                                    {activeTab === 'subjects' && (
                                        <td className="text-truncate" style={{ fontSize: '0.85rem' }}>
                                            {item.description || "—"}
                                        </td>
                                    )}
                                    <td>
                                        <Button variant="outline-primary" size="sm" className="me-1"
                                            onClick={() => handleOpenEdit(item)}>
                                            <i className="bi bi-pencil"></i>
                                        </Button>
                                        <Button variant="outline-danger" size="sm"
                                            onClick={() => handleDelete(item.id)}>
                                            <i className="bi bi-trash"></i>
                                        </Button>
                                    </td>
                                </tr>
                            ))}
                            {items.length === 0 && (
                                <tr><td colSpan={activeTab === 'subjects' ? 4 : 3} className="text-center text-muted py-3">Chưa có dữ liệu</td></tr>
                            )}
                        </tbody>
                    </Table>
                    {totalPages > 1 && (
                        <div className="d-flex justify-content-center mt-4">
                            <Pagination>
                                {Array.from({ length: totalPages }, (_, i) => i + 1).map(num => (
                                    <Pagination.Item key={num} active={num === currentPage} onClick={() => setCurrentPage(num)}>
                                        {num}
                                    </Pagination.Item>
                                ))}
                            </Pagination>
                        </div>
                    )}

                </div>
            )}

            <Modal className="admin-theme" show={showModal} onHide={() => setShowModal(false)}>
                <Modal.Header closeButton>
                    <Modal.Title>{editingItem ? "Sửa" : "Thêm"} {currentTab.label}</Modal.Title>
                </Modal.Header>
                <Form onSubmit={handleSubmit}>
                    <Modal.Body>
                        <Form.Group className="mb-3">
                            <Form.Label>Tên</Form.Label>
                            <Form.Control type="text" value={formData.name || ''}
                                onChange={e => setFormData({ ...formData, name: e.target.value })} required />
                        </Form.Group>
                        {activeTab === 'subjects' && (
                            <Form.Group className="mb-3">
                                <Form.Label>Mô tả</Form.Label>
                                <Form.Control as="textarea" rows={3} value={formData.description || ''}
                                    onChange={e => setFormData({ ...formData, description: e.target.value })} />
                            </Form.Group>
                        )}
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant="secondary" onClick={() => setShowModal(false)} disabled={isSubmitting}>Hủy</Button>
                        <Button variant="primary" type="submit" disabled={isSubmitting}>
                            {isSubmitting ? "Đang lưu..." : "Lưu"}
                        </Button>
                    </Modal.Footer>
                </Form>
            </Modal>
        </>
    );
}

export default AdminCategory;

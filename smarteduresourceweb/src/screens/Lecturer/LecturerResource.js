import { useContext, useEffect, useState } from "react";
import { Alert, Button, Form, Table, InputGroup, Pagination } from "react-bootstrap";
import { useNavigate, useSearchParams } from "react-router-dom";

import { MyUserContext } from "../../configs/Context";
import { authApis, endpoints } from "../../configs/Apis";

import MySpinner from "../../components/common/MySpinner";
import "./Lecturer.css";

const LecturerResource = () => {
    const [user] = useContext(MyUserContext);
    const [resources, setResources] = useState([]);
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState("");
    const nav = useNavigate();
    const [q] = useSearchParams();
    const kwParam = q.get("kw") || "";
    const [searchKw, setSearchKw] = useState(kwParam);
    const pageParam = Number.parseInt(q.get("page"), 10);
    const currentPage = Number.isInteger(pageParam) && pageParam > 0 ? pageParam : 1;
    const [totalPages, setTotalPages] = useState(1);

    useEffect(() => {
        if (!user || (user.role !== "LECTURER" && user.role !== "ADMIN")) {
            nav('/login'); return;
        }
        loadResources();
    }, [user, nav, kwParam, currentPage]);

    useEffect(() => {
        setSearchKw(kwParam);
    }, [kwParam]);

    const loadResources = async () => {
        try {
            setLoading(true);
            setErr("");
            let url = endpoints['lecturer-resources'] + `?page=${currentPage}`;
            if (kwParam) {
                url += `&keyword=${kwParam}`;
            }
            let res = await authApis().get(url);
            const pageData = res.data.data;
            setResources(pageData?.items || []);
            setTotalPages(pageData?.totalPages || 1);
        } catch (ex) {
            console.error(ex);
            setErr("Không thể tải danh sách học liệu.");
        } finally {
            setLoading(false);
        }
    };

    const handleOpenCreate = () => {
        nav('/lecturer/resources/create');
    };

    const handleOpenEdit = (r) => {
        nav(`/lecturer/resources/${r.id}/edit`);
    };

    const handleDelete = async (id) => {
        if (!window.confirm("Bạn có chắc chắn muốn xóa học liệu này?")) return;
        try {
            await authApis().delete(endpoints['lecturer-resource-detail'](id));
            loadResources();
        } catch (ex) {
            console.error(ex);
            setErr("Không thể xóa học liệu.");
        }
    };

    const handleSearch = (e) => {
        e.preventDefault();
        const params = new URLSearchParams();
        if (searchKw.trim()) params.set("kw", searchKw.trim());
        nav(`?${params.toString()}`);
    };

    const handlePageChange = (page) => {
        const params = new URLSearchParams();
        if (kwParam) params.set("kw", kwParam);
        if (page > 1) params.set("page", page);
        nav(`?${params.toString()}`);
    };

    if (loading) return <MySpinner />;

    console.log(resources);

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h4 className="mb-0">Quản lý Học liệu</h4>
                <div className="d-flex align-items-center w-50">
                    <Form onSubmit={handleSearch} className="w-100 me-3">
                        <InputGroup>
                            <Form.Control
                                type="text"
                                placeholder="Tìm kiếm..."
                                value={searchKw}
                                onChange={(e) => setSearchKw(e.target.value)}
                            />
                            <Button variant="outline-secondary" type="submit">
                                <i className="bi bi-search"></i>
                            </Button>
                        </InputGroup>
                    </Form>
                    <Button style={{ backgroundColor: "#6366f1", borderColor: "#6366f1", whiteSpace: "nowrap" }} variant="primary" size="sm" onClick={handleOpenCreate}>
                        <i className="bi bi-plus-lg me-1"></i> Upload học liệu
                    </Button>
                </div>
            </div>

            {err && <Alert variant="danger">{err}</Alert>}

            <div className="lecturer-panel">
                <Table hover responsive className="mb-0">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Tiêu đề</th>
                            <th>Môn học</th>
                            <th>Độ khó</th>
                            <th>Loại</th>
                            <th>Tài liệu</th>
                            <th>Thumbnail</th>
                            <th>Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        {resources.map(r => (
                            <tr key={r.id}>
                                <td>{r.id}</td>
                                <td>{r.title}</td>
                                <td>{r.subjects?.map(s => s.name).join(", ") || "—"}</td>
                                <td>
                                    {r.level === "BEGINNER" ? "Cơ bản" :
                                        r.level === "INTERMEDIATE" ? "Trung bình" :
                                            r.level === "ADVANCED" ? "Nâng cao" : "—"}
                                </td>
                                <td>{r.types?.map(t => t.name).join(", ") || "—"}</td>
                                <td>
                                    {r.fileUrl ? (
                                        <a href={r.fileUrl} target="_blank" rel="noreferrer" className="text-decoration-none">
                                            <i className="bi bi-box-arrow-up-right me-1"></i> Xem file
                                        </a>
                                    ) : "—"}
                                </td>
                                <td>
                                    {r.thumbnailUrl ? (
                                        <a href={r.thumbnailUrl} target="_blank" rel="noreferrer" className="text-decoration-none">
                                            <i className="bi bi-box-arrow-up-right me-1"></i> Xem ảnh
                                        </a>
                                    ) : "—"}
                                </td>
                                <td>
                                    <Button variant="outline-primary" size="sm" className="me-1"
                                        onClick={() => handleOpenEdit(r)}>
                                        <i className="bi bi-pencil"></i>
                                    </Button>
                                    <Button variant="outline-danger" size="sm"
                                        onClick={() => handleDelete(r.id)}>
                                        <i className="bi bi-trash"></i>
                                    </Button>
                                </td>
                            </tr>
                        ))}
                        {resources.length === 0 && (
                            <tr><td colSpan="8" className="text-center text-muted py-3">Chưa có học liệu</td></tr>
                        )}
                    </tbody>
                </Table>

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
            </div>
        </>
    );
}

export default LecturerResource;

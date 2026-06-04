import { useContext, useEffect, useState } from "react";
import { Button, Container, Form } from "react-bootstrap";
import { useNavigate, useSearchParams } from "react-router-dom";

import MySpinner from "../../components/common/MySpinner";
import { MyUserContext } from "../../configs/Context";
import Apis, { authApis, endpoints } from "../../configs/Apis";

const NewThread = () => {
    const [user] = useContext(MyUserContext);
    const [searchParams] = useSearchParams();
    const categoryId = searchParams.get("categoryId");

    const [categories, setCategories] = useState([]);
    const [selectedCategoryId, setSelectedCategoryId] = useState(categoryId || "");
    const [title, setTitle] = useState("");
    const [content, setContent] = useState("");
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);

    const nav = useNavigate();

    useEffect(() => {
        if (!user) {
            nav(`/login?next=/forum/new-thread${categoryId ? `?categoryId=${categoryId}` : ""}`);
            return;
        }

        const loadCategories = async () => {
            try {
                const res = await Apis.get(endpoints["forum-categories"]);
                const data = Array.isArray(res.data) ? res.data : res.data.data || [];
                setCategories(data);
            } catch (err) {
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        loadCategories();
    }, [user, nav, categoryId]);

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!title.trim() || !content.trim() || !selectedCategoryId) {
            alert("Vui lòng nhập đầy đủ danh mục, tiêu đề và nội dung.");
            return;
        }

        try {
            setSubmitting(true);

            const res = await authApis().post(
                endpoints["forum-thread-create"],
                {
                    title: title.trim(),
                    content: content.trim(),
                    categoryId: parseInt(selectedCategoryId)
                }
            );

            const data = Array.isArray(res.data) ? res.data : res.data.data || res.data;

            nav(data?.id ? `/forum/threads/${data.id}` : `/forum?categoryId=${selectedCategoryId}`);
        } catch (err) {
            console.error(err);
            alert("Tạo chủ đề thất bại. Kiểm tra token hoặc endpoint backend.");
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) return <MySpinner />;

    return (
        <Container className="py-4">
            <a
                href="#!"
                className="detail-back"
                onClick={e => {
                    e.preventDefault();
                    nav(-1);
                }}
            >
                ← Quay lại
            </a>

            <h2 style={{ fontSize: "1.35rem", fontWeight: 700, marginBottom: "16px" }}>
                Tạo chủ đề mới
            </h2>

            <div className="panel-card" style={{ padding: "18px" }}>
                <Form onSubmit={handleSubmit}>
                    <Form.Group className="mb-3">
                        <Form.Label>Danh mục</Form.Label>
                        <Form.Select
                            value={selectedCategoryId}
                            onChange={e => setSelectedCategoryId(e.target.value)}
                        >
                            <option value="">Chọn danh mục</option>
                            {categories.map(c => (
                                <option key={c.id} value={c.id}>
                                    {c.name}
                                </option>
                            ))}
                        </Form.Select>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Tiêu đề</Form.Label>
                        <Form.Control
                            value={title}
                            onChange={e => setTitle(e.target.value)}
                            placeholder="Nhập tiêu đề chủ đề..."
                        />
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Nội dung</Form.Label>
                        <Form.Control
                            as="textarea"
                            rows={5}
                            value={content}
                            onChange={e => setContent(e.target.value)}
                            placeholder="Nhập nội dung thảo luận..."
                        />
                    </Form.Group>

                    <Button type="submit" variant="primary" disabled={submitting}>
                        {submitting ? "Đang tạo..." : "Tạo chủ đề"}
                    </Button>
                </Form>
            </div>
        </Container>
    );
};

export default NewThread;
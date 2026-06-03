import { useEffect, useState } from "react";
import { Button, Container, Form } from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";

import MySpinner from "../../components/common/MySpinner";
import Apis, { authApis, endpoints } from "../../configs/Apis";

const ForumThread = () => {
    const { threadId } = useParams();
    const [loading, setLoading] = useState(true);
    const [thread, setThread] = useState(null);
    const [posts, setPosts] = useState([]);
    const [replyText, setReplyText] = useState("");
    const nav = useNavigate();

    useEffect(() => {
        const loadThread = async () => {
            try {
                setLoading(true);

                const [threadRes, postsRes] = await Promise.all([
                    Apis.get(endpoints["forum-thread-detail"](threadId)),
                    Apis.get(endpoints["forum-posts"](threadId))
                ]);

                const threadData = Array.isArray(threadRes.data)
                    ? threadRes.data
                    : threadRes.data.data;

                const postsData = Array.isArray(postsRes.data)
                    ? postsRes.data
                    : postsRes.data.data || [];

                setThread(threadData);
                setPosts(postsData);
            } catch (err) {
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        loadThread();
    }, [threadId]);

    const handleReply = async (e) => {
        e.preventDefault();
        if (!replyText.trim()) return;
        try {
            const res = await authApis().post(
                endpoints["forum-posts"](threadId),
                {
                    content: replyText
                }
            );

            const newPost = Array.isArray(res.data) ? res.data : res.data.data || res.data;

            setPosts([...posts, newPost]);
            setReplyText("");
        } catch (err) {
            console.error(err);
            alert("Bạn cần đăng nhập hoặc không có quyền trả lời.");
        }
    };

    if (loading) return <MySpinner />;

    const getAuthorName = (p) =>
        p.authorFullName ||
        p.authorName ||
        p.username ||
        p.userFullName ||
        p.author?.fullName ||
        "Người dùng";

    const getCreatedAt = (p) =>
        p.createdAt || p.created_at || "";

    return (
        <Container className="py-4">
            <a href="#!" className="detail-back" onClick={e => { e.preventDefault(); nav(-1); }}>← Quay lại</a>

            <h4 style={{ fontWeight: 700, marginBottom: "20px" }}>
                {thread?.title || "Chủ đề diễn đàn"}
            </h4>

            <div className="panel-card mb-4">
                <div style={{ padding: '16px' }}>
                    {posts.length === 0 ? (
                        <div className="empty-state">
                            <h5>Chưa có bài viết nào</h5>
                        </div>
                    ) : (
                        posts.map(p => {
                            const authorName = getAuthorName(p);

                            return (
                                <div key={p.id} className="post-item">
                                    <div className="d-flex justify-content-between align-items-center mb-1">
                                        <div className="d-flex align-items-center gap-2">
                                            <span
                                                className="user-avatar-circle"
                                                style={{
                                                    width: "28px",
                                                    height: "28px",
                                                    fontSize: "0.7rem"
                                                }}
                                            >
                                                {authorName.charAt(0)}
                                            </span>

                                            <strong style={{ fontSize: "0.88rem" }}>
                                                {authorName}
                                            </strong>
                                        </div>

                                        <small className="text-muted">
                                            {getCreatedAt(p)}
                                        </small>
                                    </div>

                                    <p style={{ fontSize: "0.9rem", lineHeight: 1.6, margin: "8px 0 0" }}>
                                        {p.content}
                                    </p>
                                </div>
                            );
                        })
                    )}
                </div>
            </div>

            <div className="panel-card" style={{ padding: "16px" }}>
                <h6 style={{ fontSize: "0.88rem", fontWeight: 600, marginBottom: "12px" }}>
                    Trả lời
                </h6>

                <Form onSubmit={handleReply}>
                    <Form.Control
                        as="textarea"
                        rows={3}
                        placeholder="Viết câu trả lời..."
                        value={replyText}
                        onChange={e => setReplyText(e.target.value)}
                        className="mb-2"
                    /><Button type="submit" variant="primary" size="sm">Gửi</Button>
                </Form>
            </div>
        </Container>
    );
}
export default ForumThread;

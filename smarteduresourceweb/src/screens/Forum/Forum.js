import { useEffect, useState } from "react";
import { Badge, Button, Container, Pagination } from "react-bootstrap";
import { useNavigate, useSearchParams } from "react-router-dom";

import MySpinner from "../../components/common/MySpinner";
import Apis, { endpoints } from "../../configs/Apis";

const PAGE_SIZE = 10;

const Forum = () => {
    const [loading, setLoading] = useState(true);
    const [categories, setCategories] = useState([]);
    const [threads, setThreads] = useState([]);
    const [categoryName, setCategoryName] = useState("Chủ đề");
    const [totalPages, setTotalPages] = useState(1);

    const [searchParams] = useSearchParams();
    const categoryId = searchParams.get("categoryId");

    const pageParam = Number.parseInt(searchParams.get("page"), 10);
    const currentPage = Number.isInteger(pageParam) && pageParam > 0 ? pageParam : 1;

    const nav = useNavigate();

    const normalizePageData = (res) => {
        const raw = res.data?.data ?? res.data;

        if (Array.isArray(raw)) {
            return {
                items: raw,
                totalPages: raw.length === PAGE_SIZE ? currentPage + 1 : currentPage
            };
        }

        return {
            items: raw?.items || raw?.content || raw?.data || [],
            totalPages: raw?.totalPages || 1
        };
    };

    const buildUrl = (endpoint, params = {}) => {
        const query = new URLSearchParams(params).toString();
        return query ? `${endpoint}?${query}` : endpoint;
    };

    const getListFromResponse = (res) => {
        const raw = res.data?.data ?? res.data;

        if (Array.isArray(raw)) return raw;

        return raw?.items || raw?.content || raw?.data || [];
    };

    const countReplies = async (threadId) => {
        try {
            const res = await Apis.get(endpoints["forum-posts"](threadId));
            return getListFromResponse(res).length;
        } catch (err) {
            console.error(err);
            return 0;
        }
    };

    useEffect(() => {
        const loadForum = async () => {
            try {
                setLoading(true);

                if (!categoryId) {
                    const res = await Apis.get(
                        buildUrl(endpoints["forum-categories"], {
                            page: currentPage
                        })
                    );

                    const pageData = normalizePageData(res);

                    const catsWithCount = await Promise.all(
                        pageData.items.map(async (cat) => {
                            try {
                                const threadRes = await Apis.get(
                                    buildUrl(endpoints["forum-threads"], {
                                        categoryId: cat.id,
                                        page: 1
                                    })
                                );

                                const threadPageData = normalizePageData(threadRes);

                                return {
                                    ...cat,
                                    threadCount:
                                        cat.threadCount ||
                                        cat.totalThreads ||
                                        threadPageData.items.length
                                };
                            } catch (err) {
                                console.error(err);
                                return {
                                    ...cat,
                                    threadCount: cat.threadCount || cat.totalThreads || 0
                                };
                            }
                        })
                    );

                    setCategories(catsWithCount);
                    setTotalPages(pageData.totalPages);
                    return;
                }

                const catRes = await Apis.get(endpoints["forum-categories"]);
                const catPageData = normalizePageData(catRes);
                const selected = catPageData.items.find(c => c.id === parseInt(categoryId));
                setCategoryName(selected?.name || "Chủ đề");

                const threadRes = await Apis.get(
                    buildUrl(endpoints["forum-threads"], {
                        categoryId,
                        page: currentPage
                    })
                );

                const threadPageData = normalizePageData(threadRes);

                if (threadPageData.items.length === 0 && currentPage > 1) {
                    handlePageChange(currentPage - 1);
                    return;
                }

                const threadsWithReplyCount = await Promise.all(
                    threadPageData.items.map(async (t) => ({
                        ...t,
                        replyCount: await countReplies(t.id)
                    }))
                );

                setThreads(threadsWithReplyCount);
                setTotalPages(threadPageData.totalPages);

            } catch (err) {
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        loadForum();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [categoryId, currentPage]);

    const handlePageChange = (page) => {
        const params = new URLSearchParams();

        if (categoryId) {
            params.set("categoryId", categoryId);
        }

        if (page > 1) {
            params.set("page", page);
        }

        nav(`/forum${params.toString() ? `?${params.toString()}` : ""}`);
    };

    const renderPagination = () => {
        if (totalPages <= 1) return null;

        return (
            <div className="d-flex justify-content-center mt-4">
                <Pagination>
                    {Array.from({ length: totalPages }, (_, i) => i + 1).map(num => (
                        <Pagination.Item
                            key={num}
                            active={num === currentPage}
                            onClick={() => handlePageChange(num)}
                        >
                            {num}
                        </Pagination.Item>
                    ))}
                </Pagination>
            </div>
        );
    };

    if (loading) return <MySpinner />;

    if (!categoryId) {
        return (
            <Container className="py-4">
                <h2 style={{ fontSize: "1.35rem", fontWeight: 700, marginBottom: "16px" }}>
                    Diễn đàn
                </h2>

                <div className="panel-card">
                    {categories.length === 0 ? (
                        <div className="empty-state">
                            <h5>Chưa có danh mục diễn đàn</h5>
                        </div>
                    ) : (
                        categories.map(cat => (
                            <div
                                key={cat.id}
                                className="forum-cat-item"
                                onClick={() => nav(`/forum?categoryId=${cat.id}`)}
                            >
                                <div>
                                    <h6 style={{ fontWeight: 600, marginBottom: "2px" }}>
                                        {cat.name}
                                    </h6>
                                    <small className="text-muted">
                                        {cat.description}
                                    </small>
                                </div>

                                <Badge bg="secondary">
                                    {cat.threadCount || 0} chủ đề
                                </Badge>
                            </div>
                        ))
                    )}
                </div>

                {renderPagination()}
            </Container>
        );
    }

    return (
        <Container className="py-4">
            <a
                href="#!"
                className="detail-back"
                onClick={e => {
                    e.preventDefault();
                    nav("/forum");
                }}
            >
                ← Danh mục
            </a>

            <div className="d-flex justify-content-between align-items-center mb-3">
                <h2 style={{ fontSize: "1.35rem", fontWeight: 700, margin: 0 }}>
                    {categoryName}
                </h2>

                <Button
                    variant="primary"
                    size="sm"
                    onClick={() => nav(`/forum/new-thread?categoryId=${categoryId}`)}
                >
                    Tạo chủ đề
                </Button>
            </div>

            <div className="panel-card">
                {threads.length === 0 ? (
                    <div className="empty-state">
                        <h5>Chưa có chủ đề nào</h5>
                    </div>
                ) : (
                    threads.map(t => (
                        <div
                            key={t.id}
                            className="thread-row"
                            onClick={() => nav(`/forum/threads/${t.id}`)}
                        >
                            <div className="d-flex justify-content-between align-items-start">
                                <div>
                                    <h6 style={{ fontWeight: 600, marginBottom: "4px", fontSize: "0.92rem" }}>
                                        {t.isPinned && (
                                            <Badge bg="danger" className="me-2" style={{ fontSize: "0.68rem" }}>
                                                Ghim
                                            </Badge>
                                        )}
                                        {t.title}
                                    </h6>

                                    <small className="text-muted">
                                        {t.createdBy.fullName || "Người dùng"}
                                        {" · "}
                                        {t.createdAt? new Date(t.createdAt).toLocaleDateString("vi-VN") : ""}
                                    </small>
                                </div>

                                <Badge bg="light" text="dark">
                                    {t.replyCount ?? 0} trả lời
                                </Badge>
                            </div>
                        </div>
                    ))
                )}
            </div>

            {renderPagination()}
        </Container>
    );
};

export default Forum;
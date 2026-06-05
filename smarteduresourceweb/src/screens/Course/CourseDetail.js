import { useContext, useEffect, useState } from "react";
import { Alert, Badge, Button, Col, Container, Row } from "react-bootstrap";
import { Link, useNavigate, useParams } from "react-router-dom";

import { MyUserContext } from "../../configs/Context";
import MySpinner from "../../components/common/MySpinner";
import CourseCard from "../../components/common/CourseCard";
import Apis, { authApis, endpoints } from "../../configs/Apis";
import cookies from "react-cookies";

const formatPrice = (price) => {
    if (!price) return "Miễn phí";
    return `${Number(price).toLocaleString("vi-VN")}đ`;
};

const formatDate = (date) => {
    if (!date) return "Chưa cập nhật";
    return new Date(date).toLocaleDateString("vi-VN");
};

const formatLevel = (level) => {
    switch (level) {
        case "BEGINNER":
            return "Cơ bản";
        case "INTERMEDIATE":
            return "Trung bình";
        case "ADVANCED":
            return "Nâng cao";
        default:
            return "Chưa phân loại";
    }
};

const levelVariant = (level) => {
    switch (level) {
        case "BEGINNER":
            return "success";
        case "INTERMEDIATE":
            return "warning";
        case "ADVANCED":
            return "danger";
        default:
            return "secondary";
    }
};

const buildChaptersFromLessons = (lessons = []) => {
    const map = {};
    lessons.forEach(lesson => {
        const chapterNum = lesson.chapterNum || 1;
        if (!map[chapterNum]) map[chapterNum] = [];
        map[chapterNum].push(lesson);
    });

    return Object.entries(map)
        .sort((a, b) => Number(a[0]) - Number(b[0]))
        .map(([chapterNum, chapterLessons]) => ({
            chapterNum: Number(chapterNum),
            chapterTitle: `Chương ${chapterNum}`,
            lessons: chapterLessons.sort((a, b) => (a.lessonNum || 0) - (b.lessonNum || 0))
        }));
};

const CourseDetail = () => {
    const { id } = useParams();
    const [user] = useContext(MyUserContext);
    const [course, setCourse] = useState(null);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState("");
    const [expandedSections, setExpandedSections] = useState({});
    const [showAllSections, setShowAllSections] = useState(false);
    const [isEnrolled, setIsEnrolled] = useState(false);
    const [enrolling, setEnrolling] = useState(false);
    const [enrollErr, setEnrollErr] = useState("");
    const [enrollSuccess, setEnrollSuccess] = useState("");
    const [activeTab, setActiveTab] = useState("intro");
    const nav = useNavigate();

    useEffect(() => {
        const load = async () => {
            try {
                setLoading(true);
                const res = await Apis.get(
                    endpoints["course-detail"](id)
                );

                const found = res.data.data;
                if (found) {
                    let courseData = found;
                    if (!found.chapters || found.chapters.length === 0) {
                        const lessonsRes = await Apis.get(endpoints["course-lessons-public"](id));
                        const lessons = lessonsRes.data.data || [];
                        const chapters = buildChaptersFromLessons(lessons);
                        courseData = {
                            ...found,
                            chapters,
                            totalChapters: chapters.length,
                            totalLessons: lessons.length
                        };
                    }

                    setCourse(courseData);
                    const initExpanded = {};
                    courseData.chapters?.forEach(chapter => { initExpanded[chapter.chapterNum] = false; });
                    setExpandedSections(initExpanded);
                } else setErr("Không tìm thấy khóa học.");
            } catch (ex) { console.error(ex); setErr("Lỗi tải dữ liệu."); } finally { setLoading(false); }
        };
        load();
    }, [id]);

    useEffect(() => {
        const token = cookies.load("token");
        if (!user || !id || !token) {
            setIsEnrolled(false);
            return;
        }

        const checkEnrollment = async () => {
            try {
                const res = await authApis().get(
                    endpoints["my-enrollments"]
                );

                const data = res.data.data || [];

                const enrolled = data.some(e =>
                    e.courseId === parseInt(id)
                );

                setIsEnrolled(enrolled);
            } catch (err) {
                console.error(err);
                setIsEnrolled(false);
            }
        };

        checkEnrollment();
    }, [user, id]);

    const toggleSection = (sid) => setExpandedSections(prev => ({ ...prev, [sid]: !prev[sid] }));

    const handleEnroll = async () => {
        if (!user) {
            nav(`/login?next=/courses/${id}`);
            return;
        }

        if (isEnrolled) {
            nav(`/courses/${id}/learn`);
            return;
        }

        if (course.isPaid) {
            nav(`/payments?courseId=${id}`);
            return;
        }

        try {
            setEnrolling(true);
            setEnrollErr("");
            await authApis().post(
                endpoints["enroll-course"](id)
            );

            setIsEnrolled(true);
            setEnrollSuccess("Đăng ký khóa học thành công! Bạn có thể vào học ngay.");
        } catch (ex) {
            setEnrollErr("Đăng ký thất bại. Vui lòng thử lại.");
        } finally {
            setEnrolling(false);
        }
    };

    if (loading) return <MySpinner />;
    if (err) return <Container className="py-5"><div className="rd-error">{err}</div></Container>;
    if (!course) return null;

    const chapters = course.chapters || [];
    const displayedChapters = showAllSections ? chapters : chapters.slice(0, 3);
    const relatedCourses = [];
    const description = course.description || "Khóa học chưa có mô tả.";
    const subjectName = course.subject?.name || "Chưa phân loại";
    const lecturerName = course.lecturerUser?.fullName || course.lecturerUser?.username || "Chưa cập nhật";
    const lecturerAvatar = lecturerName.trim().charAt(0).toUpperCase();
    const courseInitial = (course.name || "K").trim().charAt(0).toUpperCase();
    const totalChapters = course.totalChapters || chapters.length || 0;
    const totalLessons = course.totalLessons || chapters.reduce((total, chapter) => total + (chapter.lessons?.length || 0), 0);

    const ctaLabel = isEnrolled ? "Vào học ngay →"
        : enrolling ? "Đang xử lý..."
            : course.isPaid ? `Mua khóa học — ${formatPrice(course.price)}`
                : "Học miễn phí";

    return (
        <div className="cd-page">
            <div className="cd-hero">
                <Container>
                    <Row className="align-items-start">
                        <Col lg={8}>
                            <nav className="cd-breadcrumb">
                                <Link to="/">Trang chủ</Link>
                                <span>›</span>
                                <Link to="/courses">Khóa học</Link>
                                <span>›</span>
                                <span>{subjectName}</span>
                            </nav>

                            <div className="d-flex flex-wrap gap-2 mb-3">
                                <Badge className="cd-level-badge" bg={levelVariant(course.targetLevel)}>
                                    {formatLevel(course.targetLevel)}
                                </Badge>
                                {course.subject && (
                                    <span className="cd-subject-pill">{subjectName}</span>
                                )}
                            </div>

                            <h1 className="cd-title">{course.name}</h1>
                            <p className="cd-desc">{description}</p>

                            <div className="cd-meta">
                                <span>{course.enrollmentCount || 0} học viên</span>
                                <span>Bắt đầu: {formatDate(course.startDate)}</span>
                                <span>Kết thúc: {formatDate(course.endDate)}</span>
                            </div>

                            <div className="cd-mobile-cta d-lg-none">
                                {course.isPaid && !isEnrolled && (
                                    <div className="cd-price-row">
                                        <span className="cd-price">{formatPrice(course.price)}</span>
                                    </div>
                                )}
                                {enrollErr && <Alert variant="danger" className="py-2 mb-2" style={{ fontSize: '0.82rem' }}>{enrollErr}</Alert>}
                                {enrollSuccess && <Alert variant="success" className="py-2 mb-2" style={{ fontSize: '0.82rem' }}>{enrollSuccess}</Alert>}
                                <Button className="cd-enroll-btn w-100" onClick={handleEnroll} disabled={enrolling}>
                                    {ctaLabel}
                                </Button>
                            </div>
                        </Col>
                    </Row>
                </Container>
            </div>

            <Container className="py-4">
                <Row className="g-4">
                    <Col lg={8}>

                        <div className="cd-section-card">
                            <h2 className="cd-section-title">Giới thiệu khóa học</h2>
                            <p className="cd-intro-text">{description}</p>
                        </div>

                        <div className="cd-section-card">
                            <div className="cd-curriculum-head">
                                <h2 className="cd-section-title">Nội dung chương trình</h2>
                                <span className="cd-curriculum-meta">
                                    {totalChapters} Chương • {totalLessons} Bài giảng
                                </span>
                            </div>

                            <div className="cd-sections">
                                {displayedChapters.map((chapter, idx) => (
                                    <div key={chapter.chapterNum} className="cd-chapter">
                                        <button
                                            className="cd-chapter-header"
                                            onClick={() => toggleSection(chapter.chapterNum)}
                                        >
                                            <div className="cd-chapter-left">
                                                <span className="cd-chapter-num">{String(idx + 1).padStart(2, '0')}</span>
                                                <span className="cd-chapter-title">{chapter.chapterTitle}</span>
                                            </div>
                                            <div className="cd-chapter-right">
                                                <span className="cd-chapter-meta">{chapter.lessons?.length || 0} bài</span>
                                                <span className="cd-chapter-chevron">{expandedSections[chapter.chapterNum] ? '▲' : '▼'}</span>
                                            </div>
                                        </button>

                                        {expandedSections[chapter.chapterNum] && chapter.lessons?.length > 0 && (
                                            <div className="cd-lessons">
                                                {chapter.lessons.map(item => (
                                                    <div key={item.id} className="cd-lesson-item">
                                                        <div className="cd-lesson-main">
                                                            <div className="cd-lesson-heading">
                                                                <span className="cd-lesson-order">
                                                                    {chapter.chapterNum}.{item.lessonNum}
                                                                </span>
                                                                <span className="cd-lesson-title">
                                                                    {item.title || item.resourceTitle || item.quizTitle}
                                                                </span>
                                                                {item.isFree && <span className="cd-free-tag">Miễn phí</span>}
                                                            </div>

                                                            <div className="cd-lesson-content-list">
                                                                {item.resourceTitle && (
                                                                    <span className="cd-content-chip">
                                                                        <i className={`bi ${item.itemType === 'VIDEO' ? 'bi-play-circle' : 'bi-file-earmark-text'}`} />
                                                                        {item.resourceTitle}
                                                                        {item.format && <small>{item.format}</small>}
                                                                    </span>
                                                                )}
                                                                {item.quizTitle && (
                                                                    <span className="cd-content-chip quiz">
                                                                        <i className="bi bi-pencil-square" />
                                                                        {item.quizTitle}
                                                                        {item.questionCount ? <small>{item.questionCount} câu</small> : null}
                                                                        {item.durationMinutes ? <small>{item.durationMinutes} phút</small> : null}
                                                                    </span>
                                                                )}
                                                                {!item.resourceTitle && !item.quizTitle && (
                                                                    <span className="cd-content-empty">Chưa có nội dung</span>
                                                                )}
                                                            </div>
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                    </div>
                                ))}
                            </div>

                            {chapters.length > 3 && (
                                <button className="cd-show-more" onClick={() => setShowAllSections(!showAllSections)}>
                                    {showAllSections
                                        ? 'Thu gọn'
                                        : `Xem thêm ${chapters.length - 3} chương khác`}
                                </button>
                            )}
                        </div>

                        <div className="cd-tab-bar">
                            {[["intro", "Giới thiệu"], ["instructor", "Giảng viên"]].map(([key, label]) => (
                                <button key={key} className={`cd-tab-btn2 ${activeTab === key ? 'active' : ''}`} onClick={() => setActiveTab(key)}>{label}</button>
                            ))}
                        </div>

                        {activeTab === "intro" && (
                            <div className="cd-section-card">
                                <p className="cd-intro-text">{description}</p>
                            </div>
                        )}

                        {activeTab === "instructor" && (
                            <div className="cd-section-card">
                                <div className="cd-instructor">
                                    <div className="cd-instructor-avatar">{lecturerAvatar}</div>
                                    <div className="cd-instructor-info">
                                        <div className="cd-instructor-name">{lecturerName}</div>
                                        {course.lecturerUser?.email && (
                                            <div className="cd-instructor-title">{course.lecturerUser.email}</div>
                                        )}
                                        {course.lecturerUser?.phone && (
                                            <div className="cd-instructor-exp">{course.lecturerUser.phone}</div>
                                        )}
                                    </div>
                                </div>
                            </div>
                        )}

                        {isEnrolled && (
                            <div className="cd-section-card text-center">
                                <p style={{ fontSize: '0.88rem', color: 'var(--text-secondary)', marginBottom: '12px' }}>
                                    Bạn đã đăng ký. Bấm vào đây để bắt đầu học!
                                </p>
                                <Button
                                    className="cd-enroll-btn"
                                    onClick={() => nav(`/courses/${course.id}/learn`)}
                                >
                                    Vào học ngay →
                                </Button>
                            </div>
                        )}
                    </Col>

                    <Col lg={4} className="d-none d-lg-block">
                        <div className="cd-sidebar">
                            <div className="cd-sidebar-thumb">
                                <div className="cd-sidebar-thumb-placeholder">
                                    <span>{courseInitial}</span>
                                </div>
                            </div>

                            <div className="cd-sidebar-body">
                                {course.isPaid && !isEnrolled && (
                                    <div className="cd-sidebar-price-row">
                                        <span className="cd-price">{formatPrice(course.price)}</span>
                                    </div>
                                )}

                                {enrollErr && <Alert variant="danger" className="py-2 mb-2" style={{ fontSize: '0.82rem' }}>{enrollErr}</Alert>}
                                {enrollSuccess && <Alert variant="success" className="py-2 mb-2" style={{ fontSize: '0.82rem' }}>{enrollSuccess}</Alert>}

                                <Button className="cd-enroll-btn w-100 mb-3" onClick={handleEnroll} disabled={enrolling}>
                                    {ctaLabel}
                                </Button>

                                <div className="cd-includes">
                                    <div className="cd-includes-title">Gói học này bao gồm:</div>
                                    <div className="cd-include-item">
                                        <span>•</span>
                                        <span>{totalChapters} chương học</span>
                                    </div>
                                    <div className="cd-include-item">
                                        <span>•</span>
                                        <span>{totalLessons} bài giảng</span>
                                    </div>
                                    {course.subject && (
                                        <div className="cd-include-item">
                                            <span>•</span>
                                            <span>Môn học: {subjectName}</span>
                                        </div>
                                    )}
                                    <div className="cd-include-item">
                                        <span>•</span>
                                        <span>Trình độ: {formatLevel(course.targetLevel)}</span>
                                    </div>
                                </div>

                                <div className="cd-dm-block">
                                    <div className="cd-dm-instructor">
                                        <div className="cd-instructor-avatar sm">{lecturerAvatar}</div>
                                        <div>
                                            <div className="cd-instructor-name sm">{lecturerName}</div>
                                            {course.lecturerUser?.email && (
                                                <div className="cd-instructor-title sm">{course.lecturerUser.email}</div>
                                            )}
                                        </div>
                                    </div>
                                    <Button
                                        variant="outline-primary"
                                        className="cd-dm-btn w-100"
                                        onClick={() => {
                                            if (!isEnrolled) {
                                                alert(
                                                    "Bạn phải đăng ký khóa học này thì mới có thể thảo luận hoặc nhắn tin với giảng viên."
                                                );
                                                return;
                                            }

                                            nav('/chat?courseId=${course.id}');
                                        }}
                                    >
                                        Nhắn tin với giảng viên
                                    </Button>
                                </div>
                            </div>
                        </div>
                    </Col>
                </Row>

                {relatedCourses.length > 0 && (
                    <div className="rd-related mt-5">
                        <div className="rd-related-head">
                            <span>Khóa học liên quan</span>
                            <Link to="/courses">Xem tất cả →</Link>
                        </div>
                        <Row className="g-3">
                            {relatedCourses.map(c => (
                                <Col key={c.id} xs={12} sm={6} lg={4}>
                                    <CourseCard course={c} />
                                </Col>
                            ))}
                        </Row>
                    </div>
                )}
            </Container>
        </div>
    );
}
export default CourseDetail;

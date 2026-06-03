import { useContext, useEffect, useState } from "react";
import { Alert, Badge, Button, Col, Container, Row } from "react-bootstrap";
import { Link, useNavigate, useParams } from "react-router-dom";

import { MyUserContext } from "../../configs/Context";
import MySpinner from "../../components/common/MySpinner";
import CourseCard from "../../components/common/CourseCard";
import { formatLevel, levelVariant, formatPrice } from "../../configs/MockData";
import Apis, { authApis, endpoints } from "../../configs/Apis";
import cookies from "react-cookies";

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
                    setCourse(found);
                    const initExpanded = {};
                    found.sections?.forEach(s => { initExpanded[s.id] = s.expanded; });
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

    const displayedSections = showAllSections ? course.sections : course.sections?.slice(0, 3);
    const relatedCourses = [];

    const ctaLabel = isEnrolled ? "Vào học ngay →"
        : enrolling ? "Đang xử lý..."
            : course.isPaid ? `Mua khóa học — ${formatPrice(course.price)}`
                : "Học miễn phí";

    return (
        <div className="cd-page">
            {/* ===== HERO BANNER ===== */}
            <div className="cd-hero">
                <Container>
                    <Row className="align-items-start">
                        <Col lg={8}>
                            {/* Breadcrumb */}
                            <nav className="cd-breadcrumb">
                                <Link to="/">Trang chủ</Link>
                                <span>›</span>
                                <Link to="/courses">Khóa học</Link>
                                <span>›</span>
                                <span>{course.subject?.name}</span>
                            </nav>

                            {/* Badges */}
                            <div className="d-flex flex-wrap gap-2 mb-3">
                                <Badge className="cd-level-badge" bg={levelVariant(course.targetLevel)}>
                                    {formatLevel(course.targetLevel)}
                                </Badge>
                                {course.subject && (
                                    <span className="cd-subject-pill">{course.subject.name}</span>
                                )}
                            </div>

                            {/* Title */}
                            <h1 className="cd-title">{course.name}</h1>
                            <p className="cd-desc">{course.longDescription || course.description}</p>

                            {/* Meta */}
                            <div className="cd-meta">
                                <span>{course.enrollmentCount} học viên</span>
                                <span>Cập nhật mới nhất: {course.lastUpdated}</span>
                                <span>{course.language}</span>
                            </div>

                            {/* Mobile CTA */}
                            <div className="cd-mobile-cta d-lg-none">
                                {course.isPaid && !isEnrolled && (
                                    <div className="cd-price-row">
                                        <span className="cd-price">{formatPrice(course.price)}</span>
                                        <span className="cd-original-price">{formatPrice(course.originalPrice)}</span>
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
                    {/* ===== LEFT COLUMN ===== */}
                    <Col lg={8}>

                        {/* Introduction */}
                        <div className="cd-section-card">
                            <h2 className="cd-section-title">Giới thiệu khóa học</h2>
                            <p className="cd-intro-text">{course.longDescription || course.description}</p>

                            {/* Highlights */}
                            {course.highlights && (
                                <div className="cd-highlights">
                                    {course.highlights.map((h, i) => (
                                        <div key={i} className="cd-highlight-item">
                                            <span className="cd-highlight-icon">✓</span>
                                            <span>{h}</span>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>

                        {/* Curriculum */}
                        <div className="cd-section-card">
                            <div className="cd-curriculum-head">
                                <h2 className="cd-section-title">Nội dung chương trình</h2>
                                <span className="cd-curriculum-meta">
                                    {course.totalChapters} Chương • {course.totalLessons} Bài giảng • {course.totalHours} Giờ
                                </span>
                            </div>

                            <div className="cd-sections">
                                {displayedSections?.map((section, idx) => (
                                    <div key={section.id} className="cd-chapter">
                                        <button
                                            className="cd-chapter-header"
                                            onClick={() => toggleSection(section.id)}
                                        >
                                            <div className="cd-chapter-left">
                                                <span className="cd-chapter-num">{String(idx + 1).padStart(2, '0')}</span>
                                                <span className="cd-chapter-title">{section.title}</span>
                                            </div>
                                            <div className="cd-chapter-right">
                                                <span className="cd-chapter-meta">{section.lessons} bài • {section.duration}</span>
                                                <span className="cd-chapter-chevron">{expandedSections[section.id] ? '▲' : '▼'}</span>
                                            </div>
                                        </button>

                                        {expandedSections[section.id] && section.items?.length > 0 && (
                                            <div className="cd-lessons">
                                                {section.items.map(item => (
                                                    <div key={item.id} className="cd-lesson-item">
                                                        <div className="cd-lesson-left">
                                                            <span className="cd-lesson-icon">
                                                                {item.type === 'video' ? '▶' : '📄'}
                                                            </span>
                                                            <span className="cd-lesson-title">{item.title}</span>
                                                        </div>
                                                        <div className="cd-lesson-right">
                                                            {item.isFree && <span className="cd-free-tag">Miễn phí</span>}
                                                            {item.duration && <span className="cd-lesson-dur">{item.duration}</span>}
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                    </div>
                                ))}
                            </div>

                            {course.sections?.length > 3 && (
                                <button className="cd-show-more" onClick={() => setShowAllSections(!showAllSections)}>
                                    {showAllSections
                                        ? 'Thu gọn'
                                        : `Xem thêm ${course.sections.length - 3} chương khác`}
                                </button>
                            )}
                        </div>

                        {/* Tab bar */}
                        <div className="cd-tab-bar">
                            {[["intro", "Giới thiệu"], ["instructor", "Giảng viên"]].map(([key, label]) => (
                                <button key={key} className={`cd-tab-btn2 ${activeTab === key ? 'active' : ''}`} onClick={() => setActiveTab(key)}>{label}</button>
                            ))}
                        </div>

                        {/* Tab: Giới thiệu */}
                        {activeTab === "intro" && (
                            <div className="cd-section-card">
                                <p className="cd-intro-text">{course.longDescription || course.description}</p>
                                {course.highlights && (
                                    <div className="cd-highlights">
                                        {course.highlights.map((h, i) => (
                                            <div key={i} className="cd-highlight-item">
                                                <span className="cd-highlight-icon">✓</span>
                                                <span>{h}</span>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        )}

                        {/* Tab: Giảng viên */}
                        {activeTab === "instructor" && (
                            <div className="cd-section-card">
                                <div className="cd-instructor">
                                    <div className="cd-instructor-avatar">{course.lecturerUser.fullName.charAt(0)}</div>
                                    <div className="cd-instructor-info">
                                        <div className="cd-instructor-name">{course.lecturerUser.fullName}</div>
                                        <div className="cd-instructor-title">{course.lecturerUser.title}</div>
                                        <div className="cd-instructor-exp">{course.lecturerUser.experience}</div>
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* CTA: Vào học (nếu đã đăng ký) */}
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


                    {/* ===== RIGHT SIDEBAR ===== */}
                    <Col lg={4} className="d-none d-lg-block">
                        <div className="cd-sidebar">
                            {/* Course Thumbnail */}
                            <div className="cd-sidebar-thumb">
                                {course.thumbnailUrl ? (
                                    <img src={course.thumbnailUrl} alt={course.name} />
                                ) : (
                                    <div className="cd-sidebar-thumb-placeholder">
                                        <span>▶ Xem video giới thiệu</span>
                                    </div>
                                )}
                                <div className="cd-play-overlay">▶ Xem video giới thiệu</div>
                            </div>

                            {/* Price & CTA */}
                            <div className="cd-sidebar-body">
                                {course.isPaid && !isEnrolled && (
                                    <div className="cd-sidebar-price-row">
                                        <span className="cd-price">{formatPrice(course.price)}</span>
                                        <span className="cd-original-price">{formatPrice(course.originalPrice)}</span>
                                    </div>
                                )}

                                {enrollErr && <Alert variant="danger" className="py-2 mb-2" style={{ fontSize: '0.82rem' }}>{enrollErr}</Alert>}
                                {enrollSuccess && <Alert variant="success" className="py-2 mb-2" style={{ fontSize: '0.82rem' }}>{enrollSuccess}</Alert>}

                                <Button className="cd-enroll-btn w-100 mb-3" onClick={handleEnroll} disabled={enrolling}>
                                    {ctaLabel}
                                </Button>

                                {/* What's included */}
                                <div className="cd-includes">
                                    <div className="cd-includes-title">Gói học này bao gồm:</div>
                                    <div className="cd-include-item">
                                        <span>•</span>
                                        <span>{course.totalHours} giờ video bài giảng HD</span>
                                    </div>
                                    <div className="cd-include-item">
                                        <span>•</span>
                                        <span>{Math.round(course.totalLessons / 7)} bài tập thực hành code</span>
                                    </div>
                                    <div className="cd-include-item">
                                        <span>•</span>
                                        <span>05 dự án thực tế lớn</span>
                                    </div>
                                    <div className="cd-include-item">
                                        <span>•</span>
                                        <span>Hệ thống quiz & Final Exam</span>
                                    </div>
                                    <div className="cd-include-item">
                                        <span>•</span>
                                        <span>Chứng chỉ hoàn thành xác thực</span>
                                    </div>
                                </div>

                                {/* DM Instructor */}
                                <div className="cd-dm-block">
                                    <div className="cd-dm-instructor">
                                        <div className="cd-instructor-avatar sm">{course.lecturerUser.fullName.charAt(0)}</div>
                                        <div>
                                            <div className="cd-instructor-name sm">{course.lecturerUser.fullName}</div>
                                            <div className="cd-instructor-title sm">{course.lecturerUser.title}</div>
                                        </div>
                                    </div>
                                    <Button
                                        variant="outline-primary"
                                        className="cd-dm-btn w-100"
                                        onClick={() => nav('/chat')}
                                    >
                                        Nhắn tin với giảng viên
                                    </Button>
                                </div>
                            </div>
                        </div>
                    </Col>
                </Row>

                {/* Related Courses */}
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

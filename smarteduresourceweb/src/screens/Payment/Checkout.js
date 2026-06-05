import { useContext, useEffect, useState } from "react";
import { Alert, Button, Col, Container, Row } from "react-bootstrap";
import { Link, useNavigate, useParams } from "react-router-dom";

import { MyUserContext } from "../../configs/Context";
import MySpinner from "../../components/common/MySpinner";
import Apis, { authApis, endpoints } from "../../configs/Apis";

const formatPrice = (price) => {
    if (!price) return "0đ";
    return `${Number(price).toLocaleString("vi-VN")}đ`;
};

const Checkout = () => {
    const { courseId } = useParams();
    const [user] = useContext(MyUserContext);
    const [course, setCourse] = useState(null);
    const [loading, setLoading] = useState(true);
    const [paying, setPaying] = useState(false);
    const [err, setErr] = useState("");
    const [selectedMethod, setSelectedMethod] = useState("MOMO");
    const nav = useNavigate();

    useEffect(() => {
        if (!user) {
            nav(`/login?next=/checkout/${courseId}`);
            return;
        }

        const load = async () => {
            try {
                const res = await Apis.get(endpoints["course-detail"](courseId));
                const found = res.data.data;
                if (found) {
                    if (!found.isPaid) {
                        nav(`/courses/${courseId}`);
                        return;
                    }
                    setCourse(found);
                } else {
                    setErr("Không tìm thấy khóa học.");
                }
            } catch (ex) {
                console.error(ex);
                setErr("Lỗi tải thông tin khóa học.");
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [user, courseId, nav]);

    const handlePayment = async () => {
        try {
            localStorage.setItem("payingCourseId", courseId);
            setPaying(true);
            setErr("");
            const res = await authApis().post(
                endpoints["momo-create-payment"](courseId)
            );

            const data = res.data.data;
            if (data && data.payUrl) {
                window.location.href = data.payUrl;
            } else {
                setErr("Không nhận được link thanh toán. Vui lòng thử lại.");
            }
        } catch (ex) {
            console.error(ex);
            const msg = ex.response?.data?.message || "Lỗi tạo thanh toán. Vui lòng thử lại.";
            setErr(msg);
        } finally {
            setPaying(false);
        }
    };

    if (loading) return <MySpinner />;
    if (err && !course) return <Container className="py-5"><div className="rd-error">{err}</div></Container>;
    if (!course) return null;

    const subjectName = course.subject?.name || "Chưa phân loại";
    const lecturerName = course.lecturerUser?.fullName || course.lecturerUser?.username || "Chưa cập nhật";

    return (
        <div className="ck-page">
            <Container className="py-4">
                <nav className="cd-breadcrumb mb-4">
                    <Link to="/">Trang chủ</Link>
                    <span>›</span>
                    <Link to="/courses">Khóa học</Link>
                    <span>›</span>
                    <Link to={`/courses/${courseId}`}>{course.name}</Link>
                    <span>›</span>
                    <span>Thanh toán</span>
                </nav>

                <h1 className="ck-main-title">Thanh toán khóa học</h1>

                <Row className="g-4">
                    {/* Cột trái: Thông tin khóa học */}
                    <Col lg={7}>
                        <div className="ck-card">
                            <div className="ck-card-header">
                                <i className="bi bi-book" />
                                <span>Thông tin khóa học</span>
                            </div>
                            <div className="ck-card-body">
                                <div className="ck-course-info">
                                    <div className="ck-course-thumb">
                                        <span>{(course.name || "K").charAt(0).toUpperCase()}</span>
                                    </div>
                                    <div className="ck-course-detail">
                                        <h3 className="ck-course-name">{course.name}</h3>
                                        <div className="ck-course-meta">
                                            <span><i className="bi bi-person" /> {lecturerName}</span>
                                            <span><i className="bi bi-tag" /> {subjectName}</span>
                                        </div>
                                        {course.description && (
                                            <p className="ck-course-desc">{course.description}</p>
                                        )}
                                    </div>
                                </div>

                                <div className="ck-price-summary">
                                    <div className="ck-price-row">
                                        <span>Giá khóa học</span>
                                        <span>{formatPrice(course.price)}</span>
                                    </div>
                                    <div className="ck-price-row ck-total">
                                        <span>Tổng thanh toán</span>
                                        <span className="ck-total-price">{formatPrice(course.price)}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </Col>

                    {/* Cột phải: Phương thức thanh toán */}
                    <Col lg={5}>
                        <div className="ck-card">
                            <div className="ck-card-header">
                                <i className="bi bi-credit-card-2-front" />
                                <span>Phương thức thanh toán</span>
                            </div>
                            <div className="ck-card-body">
                                <div className="ck-methods">
                                    <label
                                        className={`ck-method-item ${selectedMethod === "MOMO" ? "active" : ""}`}
                                        onClick={() => setSelectedMethod("MOMO")}
                                    >
                                        <div className="ck-method-radio">
                                            <div className={`ck-radio ${selectedMethod === "MOMO" ? "checked" : ""}`} />
                                        </div>
                                        <div className="ck-method-icon momo" style={{ background: 'none', padding: 0 }}>
                                            <img src="/momo.png" alt="MoMo" style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: '10px' }} />
                                        </div>
                                        <div className="ck-method-info">
                                            <div className="ck-method-name">Ví MoMo</div>
                                            <div className="ck-method-desc">Thanh toán qua ví điện tử MoMo</div>
                                        </div>
                                    </label>

                                    <label className="ck-method-item disabled">
                                        <div className="ck-method-radio">
                                            <div className="ck-radio" />
                                        </div>
                                        <div className="ck-method-icon bank">
                                            <i className="bi bi-bank" />
                                        </div>
                                        <div className="ck-method-info">
                                            <div className="ck-method-name">Chuyển khoản ngân hàng</div>
                                            <div className="ck-method-desc">Sắp ra mắt</div>
                                        </div>
                                        <span className="ck-coming-soon">Sắp ra mắt</span>
                                    </label>
                                </div>

                                {err && <Alert variant="danger" className="mt-3 mb-0 py-2" style={{ fontSize: '0.85rem' }}>{err}</Alert>}

                                <Button
                                    className="ck-pay-btn w-100 mt-3"
                                    onClick={handlePayment}
                                    disabled={paying || !selectedMethod}
                                >
                                    {paying ? (
                                        <>
                                            <span className="spinner-border spinner-border-sm me-2" />
                                            Đang xử lý...
                                        </>
                                    ) : (
                                        <>Thanh toán với MoMo — {formatPrice(course.price)}</>
                                    )}
                                </Button>

                                <div className="ck-secure-note">
                                    <i className="bi bi-shield-check" />
                                    <span>Thanh toán an toàn & bảo mật</span>
                                </div>
                            </div>
                        </div>
                    </Col>
                </Row>
            </Container>
        </div>
    );
};
export default Checkout;

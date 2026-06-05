import { useEffect, useState } from "react";
import { Button, Container } from "react-bootstrap";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import Apis, { endpoints } from "../../configs/Apis";

const MoMoResult = () => {
    const [searchParams] = useSearchParams();
    const [status, setStatus] = useState("loading");
    const [syncing, setSyncing] = useState(true);
    const nav = useNavigate();

    const resultCode = searchParams.get("resultCode");
    const orderId = searchParams.get("orderId");
    const message = searchParams.get("message");
    const amount = searchParams.get("amount");
    const courseId = localStorage.getItem("payingCourseId");

    useEffect(() => {
        const syncPayment = async () => {
            if (!resultCode) {
                setStatus("unknown");
                setSyncing(false);
                return;
            }

            try {
                const paramsObj = {};
                for (const [key, value] of searchParams.entries()) {
                    paramsObj[key] = value;
                }

                await Apis.post(endpoints["momo-sync-payment"], paramsObj);

                if (resultCode === "0") {
                    setStatus("success");
                } else {
                    setStatus("failed");
                }
            } catch (err) {
                console.error("Lỗi đồng bộ thanh toán", err);
                if (resultCode === "0") setStatus("success");
                else setStatus("failed");
            } finally {
                setSyncing(false);
            }
        };

        syncPayment();
    }, [resultCode, searchParams]);

    const formatPrice = (price) => {
        if (!price) return "";
        return `${Number(price).toLocaleString("vi-VN")}đ`;
    };

    return (
        <div className="mr-page">
            <Container className="py-5">
                <div className="mr-card">
                    {(status === "loading" || syncing) && (
                        <div className="mr-loading">
                            <div className="spinner-border text-primary" />
                            <p>Đang xử lý kết quả thanh toán...</p>
                        </div>
                    )}

                    {status === "success" && !syncing && (
                        <>
                            <div className="mr-icon success">
                                <i className="bi bi-check-lg" />
                            </div>
                            <h2 className="mr-title success">Thanh toán thành công!</h2>
                            <p className="mr-subtitle">
                                Bạn đã thanh toán thành công. Khóa học đã được kích hoạt.
                            </p>

                            <div className="mr-details">
                                {orderId && (
                                    <div className="mr-detail-row">
                                        <span>Mã giao dịch</span>
                                        <span className="mr-detail-value">{orderId}</span>
                                    </div>
                                )}
                                {amount && (
                                    <div className="mr-detail-row">
                                        <span>Số tiền</span>
                                        <span className="mr-detail-value">{formatPrice(amount)}</span>
                                    </div>
                                )}
                                <div className="mr-detail-row">
                                    <span>Phương thức</span>
                                    <span className="mr-detail-value">
                                        <span className="mr-momo-badge">
                                            <img src="/momo.png" alt="MoMo" style={{ width: '14px', height: '14px', borderRadius: '3px' }} />
                                            MoMo
                                        </span>
                                    </span>
                                </div>
                            </div>

                            <div className="mr-actions">
                                <Button className="mr-btn-primary" onClick={() => nav(courseId ? `/courses/${courseId}/learn` : "/my-courses")}>
                                    <i className="bi bi-play-circle me-2" />
                                    Vào học ngay
                                </Button>
                                <Link to="/courses" className="mr-btn-secondary">
                                    Khám phá thêm khóa học
                                </Link>
                            </div>
                        </>
                    )}

                    {status === "failed" && !syncing && (
                        <>
                            <div className="mr-icon failed">
                                <i className="bi bi-x-lg" />
                            </div>
                            <h2 className="mr-title failed">Thanh toán thất bại</h2>
                            <p className="mr-subtitle">
                                {message || "Giao dịch không thành công. Vui lòng thử lại."}
                            </p>

                            {orderId && (
                                <div className="mr-details">
                                    <div className="mr-detail-row">
                                        <span>Mã giao dịch</span>
                                        <span className="mr-detail-value">{orderId}</span>
                                    </div>
                                </div>
                            )}

                            <div className="mr-actions">
                                <Button className="mr-btn-primary" onClick={() => nav("/courses")}>
                                    <i className="bi bi-search me-2" />
                                    Tìm lại khóa học
                                </Button>
                                <Link to="/payments" className="mr-btn-secondary">
                                    Xem lịch sử giao dịch
                                </Link>
                            </div>
                        </>
                    )}

                    {status === "unknown" && !syncing && (
                        <>
                            <div className="mr-icon unknown">
                                <i className="bi bi-question-lg" />
                            </div>
                            <h2 className="mr-title">Không xác định</h2>
                            <p className="mr-subtitle">
                                Không tìm thấy thông tin thanh toán. Vui lòng kiểm tra lịch sử thanh toán.
                            </p>
                            <div className="mr-actions">
                                <Button className="mr-btn-primary" onClick={() => nav("/payments")}>
                                    Xem lịch sử thanh toán
                                </Button>
                            </div>
                        </>
                    )}
                </div>
            </Container>
        </div>
    );
};
export default MoMoResult;

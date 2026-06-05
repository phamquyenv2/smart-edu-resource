import { useContext, useEffect, useState } from "react";
import { Badge, Container, Table } from "react-bootstrap";
import { useNavigate } from "react-router-dom";

import { MyUserContext } from "../../configs/Context";
import MySpinner from "../../components/common/MySpinner";
import { authApis, endpoints } from "../../configs/Apis";

const PaymentHistory = () => {
    const [user] = useContext(MyUserContext);
    const [payments, setPayments] = useState([]);
    const [loading, setLoading] = useState(true);
    const nav = useNavigate();

    useEffect(() => {
        if (!user) { nav('/login'); return; }
        const loadPayments = async () => {
            try {
                const res = await authApis().get(endpoints["student-payments"]);
                setPayments(Array.isArray(res.data) ? res.data : res.data.data || []);
            } catch (err) {
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        loadPayments();
    }, [user, nav]);

    if (loading) return <MySpinner />;

    const statusBadge = (status) => {
        switch (status) {
            case "SUCCESS": return <Badge bg="success">Thành công</Badge>;
            case "PENDING": return <Badge bg="warning" text="dark">Chờ xử lý</Badge>;
            case "CANCELLED": return <Badge bg="danger">Đã hủy</Badge>;
            case "REFUNDED": return <Badge bg="info">Hoàn tiền</Badge>;
            default: return <Badge bg="secondary">{status}</Badge>;
        }
    };

    return (
        <Container className="py-4">
            <h2 style={{ fontSize: '1.35rem', fontWeight: 700, marginBottom: '20px' }}>Lịch sử thanh toán</h2>
            <div className="panel-card">
                <Table responsive className="payment-table mb-0">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Khóa học</th>
                            <th>Số tiền</th>
                            <th>Phương thức</th>
                            <th>Trạng thái</th>
                            <th>Ngày</th>
                        </tr>
                    </thead>
                    <tbody>
                        {payments.length === 0 ? (
                            <tr>
                                <td colSpan="6" className="text-center py-4">
                                    Chưa có giao dịch thanh toán
                                </td>
                            </tr>
                        ) : (
                            payments.map((p, idx) => (
                                <tr key={p.id}>
                                    <td>{idx + 1}</td>
                                    <td>{p.courseName || p.enrollmentCourseName || "Khóa học"}</td>
                                    <td>{p.amount.toLocaleString('vi-VN')}đ</td>
                                    <td><Badge bg="light" text="dark">{p.paymentMethod || "_"}</Badge></td>
                                    <td>{statusBadge(p.status)}</td>
                                    <td>{p.createdAt || p.paidAt}</td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </Table>
            </div>
        </Container>
    );
}
export default PaymentHistory;

import { useContext, useEffect, useState } from "react";
import { Alert, Button, Col, Container, Form, Row } from "react-bootstrap";
import { useNavigate } from "react-router-dom";

import { MyUserContext } from "../../configs/Context";
import MySpinner from "../../components/common/MySpinner";

const StudentProfile = () => {
    const [user] = useContext(MyUserContext);
    const [loading, setLoading] = useState(true);
    const [formData, setFormData] = useState({});
    const [success, setSuccess] = useState("");
    const nav = useNavigate();

    useEffect(() => {
        if (!user) { nav('/login'); return; }
        setFormData({
            fullName: user.fullName || "",
            email: user.email || "",
            phone: user.phone || "",
            studentCode: user.studentCode || "",
            educationLevel: user.educationLevel || "",
            learningGoal: user.learningGoal || "",
        });
        const t = setTimeout(() => setLoading(false), 400);
        return () => clearTimeout(t);
    }, [user, nav]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSuccess("Cập nhật hồ sơ thành công.");
        setTimeout(() => setSuccess(""), 3000);
    };

    if (loading) return <MySpinner />;

    return (
        <Container className="py-4">
            <h2 style={{ fontSize: '1.35rem', fontWeight: 700, marginBottom: '20px' }}>Hồ sơ cá nhân</h2>

            <Row>
                <Col md={4} className="mb-4">
                    <div className="panel-card" style={{ padding: '24px', textAlign: 'center' }}>
                        <div className="user-avatar-circle mx-auto mb-3" style={{ width: '80px', height: '80px', fontSize: '2rem' }}>
                            {user ? user.fullName.charAt(0) : "U"}
                        </div>
                        <h5 style={{ fontSize: '1.1rem', fontWeight: 600 }}>{user?.fullName}</h5>
                        <p className="text-muted" style={{ fontSize: '0.85rem' }}>{user?.email}</p>
                        <p className="text-muted" style={{ fontSize: '0.82rem' }}>Vai trò: {user?.role}</p>
                    </div>
                </Col>
                <Col md={8}>
                    <div className="panel-card" style={{ padding: '24px' }}>
                        {success && <Alert variant="success">{success}</Alert>}
                        <Form onSubmit={handleSubmit}>
                            <Row>
                                <Col md={6}>
                                    <Form.Group className="mb-3">
                                        <Form.Label style={{ fontSize: '0.85rem', fontWeight: 500 }}>Họ và tên</Form.Label>
                                        <Form.Control value={formData.fullName || ''} onChange={e => setFormData({ ...formData, fullName: e.target.value })} />
                                    </Form.Group>
                                </Col>
                                <Col md={6}>
                                    <Form.Group className="mb-3">
                                        <Form.Label style={{ fontSize: '0.85rem', fontWeight: 500 }}>Email</Form.Label>
                                        <Form.Control type="email" value={formData.email || ''} onChange={e => setFormData({ ...formData, email: e.target.value })} />
                                    </Form.Group>
                                </Col>
                            </Row>
                            <Row>
                                <Col md={6}>
                                    <Form.Group className="mb-3">
                                        <Form.Label style={{ fontSize: '0.85rem', fontWeight: 500 }}>Số điện thoại</Form.Label>
                                        <Form.Control value={formData.phone || ''} onChange={e => setFormData({ ...formData, phone: e.target.value })} />
                                    </Form.Group>
                                </Col>
                                <Col md={6}>
                                    <Form.Group className="mb-3">
                                        <Form.Label style={{ fontSize: '0.85rem', fontWeight: 500 }}>Mã sinh viên</Form.Label>
                                        <Form.Control value={formData.studentCode || ''} onChange={e => setFormData({ ...formData, studentCode: e.target.value })} />
                                    </Form.Group>
                                </Col>
                            </Row>
                            <Form.Group className="mb-3">
                                <Form.Label style={{ fontSize: '0.85rem', fontWeight: 500 }}>Trình độ</Form.Label>
                                <Form.Select value={formData.educationLevel || ''} onChange={e => setFormData({ ...formData, educationLevel: e.target.value })}>
                                    <option value="HIGH_SCHOOL">THPT</option>
                                    <option value="COLLEGE">Cao đẳng</option>
                                    <option value="UNIVERSITY">Đại học</option>
                                    <option value="MASTER">Thạc sĩ</option>
                                </Form.Select>
                            </Form.Group>
                            <Form.Group className="mb-3">
                                <Form.Label style={{ fontSize: '0.85rem', fontWeight: 500 }}>Mục tiêu học tập</Form.Label>
                                <Form.Control as="textarea" rows={3} value={formData.learningGoal || ''} onChange={e => setFormData({ ...formData, learningGoal: e.target.value })} />
                            </Form.Group>
                            <Button type="submit" variant="primary">Cập nhật</Button>
                        </Form>
                    </div>
                </Col>
            </Row>
        </Container>
    );
}
export default StudentProfile;

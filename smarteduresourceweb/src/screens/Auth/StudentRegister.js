import { useState } from "react";
import { Alert, Button, Container, Form } from "react-bootstrap";
import { Link, useNavigate } from "react-router-dom";

import Apis, { endpoints } from "../../configs/Apis";
import MySpinner from "../../components/common/MySpinner";

const StudentRegister = () => {
    const [formData, setFormData] = useState({});
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const nav = useNavigate();

    const fields = [{
        field: "fullName",
        label: "Họ và tên",
        type: "text",
        required: true
    }, {
        field: "username",
        label: "Tên đăng nhập",
        type: "text",
        required: true
    }, {
        field: "email",
        label: "Email",
        type: "email",
        required: true
    }, {
        field: "password",
        label: "Mật khẩu",
        type: "password",
        required: true
    }, {
        field: "confirmPassword",
        label: "Xác nhận mật khẩu",
        type: "password",
        required: true
    }, {
        field: "phone",
        label: "Số điện thoại",
        type: "text",
        required: false
    }, {
        field: "studentCode",
        label: "Mã sinh viên",
        type: "text",
        required: false
    }];

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErr(""); setSuccess("");

        if (formData.password !== formData.confirmPassword) {
            setErr("Mật khẩu xác nhận không khớp.");
            return;
        }

        setLoading(true);
        try {
            const payload = {
                fullName: formData.fullName,
                username: formData.username,
                email: formData.email,
                password: formData.password,
                phone: formData.phone || null,
                studentCode: formData.studentCode || null,
                dob: formData.dob || null,
                gender: (formData.gender !== '' && formData.gender !== undefined) ? Number(formData.gender) : null,
                educationLevel: formData.educationLevel || null,
                learningGoal: formData.learningGoal || null,
            };
            await Apis.post(endpoints['student-register'], payload);
            setSuccess("Đăng ký thành công! Đang chuyển đến trang đăng nhập...");
            setTimeout(() => nav('/login'), 2000);
        } catch (ex) {
            console.error(ex);
            const status = ex.response?.status;
            const raw = ex.response?.data?.message;
            if (status === 400 || status === 409) {
                const msg = Array.isArray(raw)
                    ? raw.join(" | ")
                    : (raw || "Dữ liệu không hợp lệ.");
                setErr(msg);
            } else {
                setErr("Có lỗi xảy ra, vui lòng thử lại.");
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container>
            <div className="auth-wrap wide">
                <div className="auth-card">
                    <h2>Đăng ký sinh viên</h2>
                    <p className="sub">Tạo tài khoản để bắt đầu học tập</p>
                    {err && <Alert variant="danger">{err}</Alert>}
                    {success && <Alert variant="success">{success}</Alert>}
                    <Form onSubmit={handleSubmit}>
                        {fields.map(f => (
                            <Form.Group key={f.field} className="mb-3">
                                <Form.Label>{f.label}</Form.Label>
                                <Form.Control type={f.type} placeholder={f.label} value={formData[f.field] || ''} onChange={e => setFormData({ ...formData, [f.field]: e.target.value })} required={f.required} />
                            </Form.Group>
                        ))}
                        <Form.Group className="mb-3">
                            <Form.Label>Ngày sinh</Form.Label>
                            <Form.Control type="date" value={formData.dob || ''} onChange={e => setFormData({ ...formData, dob: e.target.value })} />
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Giới tính</Form.Label>
                            <Form.Select value={formData.gender || ''} onChange={e => setFormData({ ...formData, gender: e.target.value })}>
                                <option value="">Chọn giới tính</option>
                                <option value="1">Nam</option>
                                <option value="0">Nữ</option>
                            </Form.Select>
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Trình độ học vấn</Form.Label>
                            <Form.Select value={formData.educationLevel || ''} onChange={e => setFormData({ ...formData, educationLevel: e.target.value })}>
                                <option value="">Chọn trình độ</option>
                                <option value="FRESHMAN">Năm 1</option>
                                <option value="SOPHOMORE">Năm 2</option>
                                <option value="JUNIOR">Năm 3</option>
                                <option value="SENIOR">Năm 4</option>
                                <option value="ALUMNI">Đã tốt nghiệp</option>
                            </Form.Select>
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Mục tiêu học tập</Form.Label>
                            <Form.Control as="textarea" rows={2} placeholder="Mô tả mục tiêu của bạn" value={formData.learningGoal || ''} onChange={e => setFormData({ ...formData, learningGoal: e.target.value })} />
                        </Form.Group>
                        <Form.Check type="checkbox" label="Tôi đồng ý với điều khoản sử dụng" className="mb-3" required id="terms" />
                        {loading ? <MySpinner /> : <Button type="submit" className="btn-submit">Đăng ký</Button>}
                    </Form>
                    <div className="text-center mt-3">
                        <p className="mb-1">Bạn là giảng viên? <Link to="/register/lecturer" className="auth-link">Đăng ký giảng viên</Link></p>
                        <p className="mb-0">Đã có tài khoản? <Link to="/login" className="auth-link">Đăng nhập</Link></p>
                    </div>
                </div>
            </div>
        </Container>
    );
}
export default StudentRegister;

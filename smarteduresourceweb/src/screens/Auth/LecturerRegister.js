import { useRef, useState } from "react";
import { Alert, Button, Container, Form } from "react-bootstrap";
import { Link, useNavigate } from "react-router-dom";

import Apis, { endpoints } from "../../configs/Apis";
import MySpinner from "../../components/common/MySpinner";

const LecturerRegister = () => {
    const [formData, setFormData] = useState({});
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const [certificateFile, setCertificateFile] = useState(null);
    const submittingRef = useRef(false);
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
        field: "specialization",
        label: "Chuyên môn",
        type: "text",
        required: true
    }];

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (submittingRef.current) return;
        setErr(""); setSuccess("");

        if (formData.password !== formData.confirmPassword) {
            setErr("Mật khẩu xác nhận không khớp.");
            return;
        }

        if (!certificateFile) {
            setErr("Vui lòng tải lên chứng chỉ PDF.");
            return;
        }

        if (certificateFile.type !== "application/pdf") {
            setErr("Chứng chỉ phải là file PDF.");
            return;
        }

        if (certificateFile.size > 5 * 1024 * 1024) {
            setErr("Chứng chỉ tối đa 5 MB.");
            return;
        }
        
        submittingRef.current = true;
        setLoading(true);
        try {
            const payload = new FormData();
            ["fullName", "username", "email", "password", "phone", "specialization", "degree", "bio"]
                .forEach(field => {
                    if (formData[field] !== undefined && formData[field] !== "") {
                        payload.append(field, formData[field]);
                    }
                });
            payload.append("certificate", certificateFile);
            await Apis.post(endpoints['lecturer-register'], payload);
            setSuccess("Đăng ký thành công! Vui lòng chờ admin duyệt tài khoản.");
            setTimeout(() => nav('/login'), 3000);
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
            submittingRef.current = false;
            setLoading(false);
        }
    };

    return (
        <Container>
            <div className="auth-wrap wide">
                <div className="auth-card">
                    <h2>Đăng ký giảng viên</h2>
                    <p className="sub">Tạo tài khoản để chia sẻ kiến thức</p>
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
                            <Form.Label>Học vị</Form.Label>
                            <Form.Select value={formData.degree || ''} onChange={e => setFormData({ ...formData, degree: e.target.value })} required>
                                <option value="">Chọn học vị</option>
                                <option value="MASTER">Thạc sĩ</option>
                                <option value="PHD">Tiến sĩ</option>
                                <option value="ASSOCPROF">Phó giáo sư</option>
                                <option value="PROFESSOR">Giáo sư</option>
                            </Form.Select>
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Chứng chỉ (PDF)</Form.Label>
                            <Form.Control type="file" accept="application/pdf,.pdf"
                                onChange={e => setCertificateFile(e.target.files?.[0] || null)} required />
                            <Form.Text className="text-muted">Chỉ nhận file PDF, tối đa 5 MB.</Form.Text>
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Giới thiệu bản thân</Form.Label>
                            <Form.Control as="textarea" rows={2} placeholder="Kinh nghiệm giảng dạy, lĩnh vực nghiên cứu..." value={formData.bio || ''} onChange={e => setFormData({ ...formData, bio: e.target.value })} />
                        </Form.Group>
                        <Form.Check type="checkbox" label="Tôi đồng ý với điều khoản sử dụng" className="mb-3" required id="terms" />
                        {loading ? <MySpinner /> : <Button type="submit" className="btn-submit">Đăng ký</Button>}
                    </Form>
                    <div className="text-center mt-3">
                        <p className="mb-1">Bạn là sinh viên? <Link to="/register/student" className="auth-link">Đăng ký sinh viên</Link></p>
                        <p className="mb-0">Đã có tài khoản? <Link to="/login" className="auth-link">Đăng nhập</Link></p>
                    </div>
                </div>
            </div>
        </Container>
    );
}
export default LecturerRegister;

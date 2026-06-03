import { useContext, useState } from "react";
import { Alert, Button, Container, Form } from "react-bootstrap";
import { Link, useNavigate } from "react-router-dom";
import cookies from "react-cookies";

import Apis, { authApis, endpoints } from "../../configs/Apis";
import { MyUserContext } from "../../configs/Context";
import MySpinner from "../../components/common/MySpinner";

const Login = () => {
    const [formData, setFormData] = useState({});
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState("");
    const [, dispatch] = useContext(MyUserContext);
    const nav = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErr("");
        setLoading(true);
        try {
            const res = await Apis.post(endpoints['login'], {
                username: formData.username,
                password: formData.password,
            });
            const token = res.data.data?.accessToken;
            if (!token) {
                setErr("Đăng nhập thất bại, vui lòng thử lại.");
                return;
            }
            cookies.save('token', token);

            const profileRes = await authApis().get(endpoints['profile']);
            const userData = profileRes.data.data;

            cookies.save('user', userData);
            dispatch({ "type": "LOGIN", "payload": userData });
            nav('/');
        } catch (ex) {
            console.error(ex);
            if (ex.response?.status === 401 || ex.response?.status === 403) {
                setErr("Tên đăng nhập hoặc mật khẩu không chính xác.");
            } else {
                setErr("Có lỗi xảy ra, vui lòng thử lại.");
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container>
            <div className="auth-wrap">
                <div className="auth-card">
                    <h2>Đăng nhập</h2>
                    <p className="sub">Chào mừng bạn quay trở lại</p>
                    {err && <Alert variant="danger">{err}</Alert>}
                    <Form onSubmit={handleSubmit}>
                        <Form.Group className="mb-3">
                            <Form.Label>Tên đăng nhập</Form.Label>
                            <Form.Control type="text" placeholder="Nhập tên đăng nhập" value={formData.username || ''} onChange={e => setFormData({ ...formData, username: e.target.value })} required />
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Mật khẩu</Form.Label>
                            <Form.Control type="password" placeholder="Nhập mật khẩu" value={formData.password || ''} onChange={e => setFormData({ ...formData, password: e.target.value })} required />
                        </Form.Group>
                        {loading ? <MySpinner /> : <Button type="submit" className="btn-submit">Đăng nhập</Button>}
                    </Form>
                    <div className="text-center mt-3">
                        <p className="mb-0">Chưa có tài khoản? <Link to="/register/student" className="auth-link">Đăng ký</Link></p>
                    </div>
                </div>
            </div>
        </Container>
    );
}

export default Login;

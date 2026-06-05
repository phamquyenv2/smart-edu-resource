import { Col, Container, Row } from "react-bootstrap";

const Footer = () => {
    return (
        <footer className="site-footer">
            <Container>
                <Row className="gy-4 justify-content-between">
                    <Col md={5}>
                        <h5>SmartEdu Resource</h5>
                        <p style={{ fontSize: '0.85rem', lineHeight: 1.6 }}>
                            Nền tảng quản lý học liệu số, hỗ trợ sinh viên và giảng viên
                            trong quá trình dạy và học trực tuyến.
                        </p>
                    </Col>
                    <Col md={4} lg={3} className="footer-contact">
                        <h6>Liên hệ</h6>
                        <ul>
                            <li>quyen.pa0303@gmail.com</li>
                            <li>0383870916</li>
                            <li>Nhà Bè,Thành phố Hồ Chí Minh</li>
                        </ul>
                    </Col>
                </Row>
                <div className="copyright">
                    &copy; {new Date().getFullYear()} Phạm Anh Quyền vs Đỗ Phú Điền
                </div>
            </Container>
        </footer>
    );
}

export default Footer;

import { useContext, useEffect, useState } from "react";
import { Container, Nav, Navbar, NavDropdown } from "react-bootstrap";
import { Link, useNavigate } from "react-router-dom";

import { authApis, endpoints } from "../../configs/Apis";
import { MyUserContext } from "../../configs/Context";

const Header = () => {
    const [user, dispatch] = useContext(MyUserContext);
    const [notifications, setNotifications] = useState([]);
    const nav = useNavigate();

    useEffect(() => {
        if (user?.role !== "STUDENT") {
            setNotifications([]);
            return undefined;
        }

        const loadNotifications = async () => {
            try {
                const res = await authApis().get(endpoints['student-notifications']);
                setNotifications(res.data.data || []);
            } catch (ex) {
                console.error(ex);
            }
        };

        loadNotifications();
        const timer = setInterval(loadNotifications, 60000);
        return () => clearInterval(timer);
    }, [user?.role]);

    const handleLogout = () => {
        dispatch({ "type": "LOGOUT" });
        nav('/login');
    };

    const markAsRead = async (notification) => {
        if (notification.isRead) return;

        try {
            await authApis().put(endpoints['student-notification-read'](notification.id));
            setNotifications(items => items.map(item =>
                item.id === notification.id ? { ...item, isRead: true } : item
            ));
        } catch (ex) {
            console.error(ex);
        }
    };

    const markAllAsRead = async () => {
        try {
            await authApis().put(endpoints['student-notifications-read-all']);
            setNotifications(items => items.map(item => ({ ...item, isRead: true })));
        } catch (ex) {
            console.error(ex);
        }
    };

    const deleteNotification = async (e, notificationId) => {
        e.preventDefault();
        e.stopPropagation();

        try {
            await authApis().delete(
                endpoints["student-notification-delete"](notificationId)
            );

            setNotifications(items =>
                items.filter(item => item.id !== notificationId)
            );
        } catch (ex) {
            console.error(ex);
            alert("Xóa thông báo thất bại.");
        }
    };

    const handleNotificationClick = async (notification) => {
        await markAsRead(notification);

        if (notification.targetUrl) {
            nav(notification.targetUrl);
            return;
        }

        if (notification.url) {
            nav(notification.url);
            return;
        }

        if (notification.link) {
            nav(notification.link);
        }
    };



    const unreadCount = notifications.filter(notification => !notification.isRead).length;

    return (
        <Navbar expand="lg" className="site-header">
            <Container>
                <Navbar.Brand as={Link} to="/">SmartEdu</Navbar.Brand>
                <Navbar.Toggle aria-controls="main-nav" />
                <Navbar.Collapse id="main-nav">
                    <Nav className="me-auto">
                        <Nav.Link as={Link} to="/">Trang chủ</Nav.Link>
                        <Nav.Link as={Link} to="/resources">Tài liệu</Nav.Link>
                        <Nav.Link as={Link} to="/courses">Khóa học</Nav.Link>
                        {user && <Nav.Link as={Link} to="/forum">Diễn đàn</Nav.Link>}
                    </Nav>
                    <Nav className="align-items-center gap-2">
                        {user === null ? (
                            <>
                                <Nav.Link as={Link} to="/login" className="btn-outline-auth">Đăng nhập</Nav.Link>
                                <Nav.Link as={Link} to="/register/student" className="btn-primary-auth">Đăng ký</Nav.Link>
                            </>
                        ) : (
                            <>
                                {user.role === "STUDENT" && (
                                    <NavDropdown
                                        title={
                                            <span className="notification-bell">
                                                <i className="bi bi-bell"></i>
                                                {unreadCount > 0 && (
                                                    <span className="notification-badge">{unreadCount > 99 ? "99+" : unreadCount}</span>
                                                )}
                                            </span>
                                        }
                                        id="notification-dropdown"
                                        align="end"
                                        className="notification-dropdown"
                                    >
                                        <div className="notification-dropdown-header">
                                            <strong>Thông báo</strong>
                                            {unreadCount > 0 && (
                                                <button type="button" onClick={markAllAsRead}>Đọc tất cả</button>
                                            )}
                                        </div>
                                        {notifications.length === 0 ? (
                                            <div className="notification-empty">Chưa có thông báo</div>
                                        ) : (
                                            notifications.slice(0, 8).map(notification => (
                                                <div
                                                    key={notification.id}
                                                    className={`notification-item dropdown-item ${notification.isRead ? "" : "unread"}`}
                                                    onClick={() => handleNotificationClick(notification)}
                                                    style={{
                                                        display: "flex",
                                                        justifyContent: "space-between",
                                                        gap: "8px",
                                                        alignItems: "flex-start",
                                                        cursor: "pointer"
                                                    }}
                                                >
                                                    <div style={{ flex: 1 }}>
                                                        <strong>{notification.title}</strong>
                                                        <span>{notification.content}</span>
                                                        <small>{notification.createdAt}</small>
                                                    </div>

                                                    <button
                                                        type="button"
                                                        onClick={(e) => deleteNotification(e, notification.id)}
                                                        title="Xóa thông báo"
                                                        style={{
                                                            border: "none",
                                                            background: "transparent",
                                                            color: "#DC2626",
                                                            fontSize: "0.8rem",
                                                            padding: "0 4px"
                                                        }}
                                                    >
                                                        ✕
                                                    </button>
                                                </div>
                                            ))
                                        )}
                                    </NavDropdown>
                                )}
                                <NavDropdown
                                    title={
                                        <span className="d-inline-flex align-items-center gap-2">
                                            <span className="user-avatar-circle">
                                                {user.fullName ? user.fullName.charAt(0) : "U"}
                                            </span>
                                            <span style={{ fontSize: '0.9rem', fontWeight: 500 }}>{user.fullName}</span>
                                        </span>
                                    }
                                    id="user-dropdown"
                                    align="end"
                                >
                                    <NavDropdown.Item as={Link} to="/profile">Hồ sơ cá nhân</NavDropdown.Item>
                                    {user.role === "STUDENT" && (
                                        <>
                                            <NavDropdown.Item as={Link} to="/student/dashboard">Dashboard</NavDropdown.Item>
                                            <NavDropdown.Item as={Link} to="/my-courses">Khóa học của tôi</NavDropdown.Item>
                                            <NavDropdown.Item as={Link} to="/learning-path">Lộ trình học tập</NavDropdown.Item>
                                            <NavDropdown.Item as={Link} to="/chat">Tin nhắn</NavDropdown.Item>
                                            <NavDropdown.Item as={Link} to="/payments">Lịch sử thanh toán</NavDropdown.Item>
                                        </>
                                    )}
                                    {user.role === "LECTURER" && (
                                        <NavDropdown.Item as={Link} to="/lecturer/dashboard">Quản lý Giảng viên</NavDropdown.Item>
                                    )}
                                    {user.role === "ADMIN" && (
                                        <NavDropdown.Item as={Link} to="/admin/dashboard">Trang quản trị</NavDropdown.Item>
                                    )}
                                    <NavDropdown.Divider />
                                    <NavDropdown.Item onClick={handleLogout}>Đăng xuất</NavDropdown.Item>
                                </NavDropdown>
                            </>
                        )}
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
}

export default Header;

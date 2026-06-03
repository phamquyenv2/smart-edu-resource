import { useContext, useEffect, useState } from "react";
import { Button, Container, Form } from "react-bootstrap";
import { useNavigate, useSearchParams } from "react-router-dom";

import { MyUserContext } from "../../configs/Context";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/common/MySpinner";


const Chat = () => {
    const [user] = useContext(MyUserContext);
    const [loading, setLoading] = useState(true);
    const [rooms, setRooms] = useState([]);
    const [messageLoading, setMessageLoading] = useState(false);
    const [activeRoom, setActiveRoom] = useState(null);
    const [messages, setMessages] = useState([]);
    const [msgText, setMsgText] = useState("");

    const nav = useNavigate();
    const [q] = useSearchParams();
    const roomIdParam = q.get("room");

    useEffect(() => {
        if (!user) { nav('/login'); return; }
        loadRooms();
    }, [user, nav]);

    useEffect(() => {
        if (activeRoom) {
            loadMessages(activeRoom.id);
        }
    }, [activeRoom]);

    const normalizeData = (res) => {
        if (Array.isArray(res.data)) return res.data;
        if (Array.isArray(res.data?.data)) return res.data.data;
        if (Array.isArray(res.data?.data?.items)) return res.data.data.items;
        if (Array.isArray(res.data?.items)) return res.data.items;
        return [];
    };

    const loadRooms = async () => {
        setLoading(true);
        try {
            const url =
                user.role === "LECTURER" || user.role === "ADMIN"
                    ? endpoints["lecturer-chat-rooms"]
                    : endpoints["chat-rooms"];

            const res = await authApis().get(url);
            const loadedRooms = normalizeData(res);

            setRooms(loadedRooms);

            if (loadedRooms.length > 0) {
                if (roomIdParam) {
                    const targetRoom = loadedRooms.find(
                        r => r.id?.toString() === roomIdParam
                    );

                    setActiveRoom(targetRoom || loadedRooms[0]);
                } else {
                    setActiveRoom(loadedRooms[0]);
                }
            }
        } catch (err) {
            console.error("Failed to load chat rooms", err);
        } finally {
            setLoading(false);
        }
    };

    const loadMessages = async (roomId) => {
        setMessageLoading(true);

        try {
            const res = await authApis().get(
                endpoints["chat-messages"](roomId)
            );

            const data = normalizeData(res);
            setMessages(data);
        } catch (err) {
            console.error("Failed to load messages", err);
            setMessages([]);
        } finally {
            setMessageLoading(false);
        }
    };

    const handleSend = async (e) => {
        e.preventDefault();

        if (!msgText.trim() || !activeRoom) return;

        try {
            const res = await authApis().post(
                endpoints["chat-send-message"](activeRoom.id),
                {
                    content: msgText
                }
            );

            const newMessage = res.data?.data || res.data;

            setMessages(prev => [...prev, newMessage]);
            setMsgText("");
        } catch (err) {
            console.error("Failed to send message", err);
            alert("Không gửi được tin nhắn.");
        }
    };

    const getRoomName = (room) => {
        return room.name || room.roomName || `Phòng chat #${room.id}`;
    };

    const getRoomSubText = (room) => {
        return room.courseName || room.type || "Phòng chung";
    };

    const getSenderName = (m) => {
        return m.senderName || m.senderFullName || m.username || "Người dùng";
    };

    const isMine = (m) => {
        return m.senderId === user.id || m.userId === user.id;
    };

    const getTime = (m) => {
        const value = m.sentAt || m.createdAt;

        if (!value) return "";

        try {
            return new Date(value).toLocaleTimeString("vi-VN", {
                hour: "2-digit",
                minute: "2-digit"
            });
        } catch {
            return value;
        }
    };

    const getDateKey = (m) => {
        const value = m.sentAt || m.createdAt;

        if (!value) return "unknown";

        return new Date(value).toLocaleDateString("vi-VN");
    };

    const getDateLabel = (m) => {
        const value = m.sentAt || m.createdAt;

        if (!value) return "";

        const date = new Date(value);
        const today = new Date();

        const yesterday = new Date();
        yesterday.setDate(today.getDate() - 1);

        const dateStr = date.toLocaleDateString("vi-VN");
        const todayStr = today.toLocaleDateString("vi-VN");
        const yesterdayStr = yesterday.toLocaleDateString("vi-VN");

        if (dateStr === todayStr) return "Hôm nay";
        if (dateStr === yesterdayStr) return "Hôm qua";

        return dateStr;
    };

    if (loading) return <MySpinner />;

    return (
        <Container fluid className="p-0">
            <h2 style={{ fontSize: "1.35rem", fontWeight: 700, marginBottom: "16px" }}>
                Tin nhắn
            </h2>

            <div className="chat-layout">
                <div className="chat-sidebar">
                    {rooms.length === 0 ? (
                        <div className="text-muted text-center mt-4">
                            Không có phòng chat nào
                        </div>
                    ) : (
                        rooms.map(room => (
                            <div
                                key={room.id}
                                className={`room-item ${activeRoom?.id === room.id ? "active" : ""}`}
                                onClick={() => setActiveRoom(room)}
                            >
                                <div className="room-name">
                                    {getRoomName(room)}
                                </div>
                                <div className="room-last">
                                    {getRoomSubText(room)}
                                </div>
                            </div>
                        ))
                    )}
                </div>

                <div className="chat-main">
                    {activeRoom ? (
                        <>
                            <div
                                style={{
                                    padding: "12px 16px",
                                    borderBottom: "1px solid #E2E8F0",
                                    fontWeight: 600,
                                    fontSize: "0.95rem"
                                }}
                            >
                                {getRoomName(activeRoom)}
                            </div>

                            <div className="chat-messages">
                                {messageLoading ? (
                                    <MySpinner />
                                ) : messages.length === 0 ? (
                                    <div className="text-muted text-center mt-4">
                                        Chưa có tin nhắn nào
                                    </div>
                                ) : (
                                    messages.map((m, index) => {
                                        const currentDate = getDateKey(m);
                                        const previousDate = index > 0 ? getDateKey(messages[index - 1]) : null;
                                        const showDateDivider = currentDate !== previousDate;

                                        return (
                                            <div key={m.id}>
                                                {showDateDivider && (
                                                    <div
                                                        style={{
                                                            textAlign: "center",
                                                            margin: "14px 0",
                                                            color: "#64748B",
                                                            fontSize: "0.75rem",
                                                            fontWeight: 600
                                                        }}
                                                    >
                                                        <span
                                                            style={{
                                                                background: "#F1F5F9",
                                                                padding: "4px 12px",
                                                                borderRadius: "999px"
                                                            }}
                                                        >
                                                            {getDateLabel(m)}
                                                        </span>
                                                    </div>
                                                )}

                                                {!isMine(m) && (
                                                    <div
                                                        style={{
                                                            fontSize: "0.72rem",
                                                            color: "#94A3B8",
                                                            marginBottom: "2px"
                                                        }}
                                                    >
                                                        {getSenderName(m)}
                                                    </div>
                                                )}

                                                <div className={`msg-bubble ${isMine(m) ? "mine" : "other"}`}>
                                                    {m.content}
                                                    <div
                                                        style={{
                                                            fontSize: "0.68rem",
                                                            opacity: 0.7,
                                                            textAlign: "right",
                                                            marginTop: "2px"
                                                        }}
                                                    >
                                                        {getTime(m)}
                                                    </div>
                                                </div>
                                            </div>
                                        );
                                    })
                                )}
                            </div>

                            <Form onSubmit={handleSend} className="chat-input-bar">
                                <Form.Control
                                    type="text"
                                    placeholder="Nhập tin nhắn..."
                                    value={msgText}
                                    onChange={e => setMsgText(e.target.value)}
                                />
                                <Button type="submit" variant="primary" size="sm">
                                    Gửi
                                </Button>
                            </Form>
                        </>
                    ) : (
                        <div className="d-flex align-items-center justify-content-center flex-grow-1 text-muted">
                            Chọn cuộc hội thoại
                        </div>
                    )}
                </div>
            </div>
        </Container>
    );
}
export default Chat;

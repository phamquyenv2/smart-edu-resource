import { useContext, useReducer } from "react";
import { BrowserRouter, Navigate, Route, Routes, useLocation } from "react-router-dom";
import cookies from "react-cookies";

import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-icons/font/bootstrap-icons.css';
import './App.css';

import { MyUserContext } from "./configs/Context";
import MyUserReducer from "./reducers/MyUserReducer";

import Header from "./components/common/Header";
import Footer from "./components/common/Footer";

import Home from "./screens/Home/Home";
import Login from "./screens/Auth/Login";
import StudentRegister from "./screens/Auth/StudentRegister";
import LecturerRegister from "./screens/Auth/LecturerRegister";
import ResourceBrowse from "./screens/Resource/ResourceBrowse";
import ResourceDetail from "./screens/Resource/ResourceDetail";
import CourseBrowse from "./screens/Course/CourseBrowse";
import CourseDetail from "./screens/Course/CourseDetail";
import CourseLearn from "./screens/Course/CourseLearn";
import StudentDashboard from "./screens/Student/StudentDashboard";
import MyCourses from "./screens/Student/MyCourses";
import StudentProfile from "./screens/Student/StudentProfile";
import LearningPath from "./screens/Student/LearningPath";
import QuizList from "./screens/Quiz/QuizList";
import QuizTaking from "./screens/Quiz/QuizTaking";
import QuizResult from "./screens/Quiz/QuizResult";
import Forum from "./screens/Forum/Forum";
import ForumThread from "./screens/Forum/ForumThread";
import NewThread from "./screens/Forum/NewThread";
import Chat from "./screens/Chat/Chat";
import PaymentHistory from "./screens/Payment/PaymentHistory";
import Checkout from "./screens/Payment/Checkout";
import MoMoResult from "./screens/Payment/MoMoResult";

import AdminLayout from "./components/Layouts/AdminLayout";
import AdminDashboard from "./screens/Admin/AdminDashboard";
import AdminUser from "./screens/Admin/AdminUser";
import AdminLecturer from "./screens/Admin/AdminLecturer";
import AdminCategory from "./screens/Admin/AdminCategory";
import AdminPayment from "./screens/Admin/AdminPayment";
import AdminForum from "./screens/Admin/AdminForum";
import LecturerLayout from "./components/Layouts/LecturerLayout";
import LecturerDashboard from "./screens/Lecturer/LecturerDashboard";
import LecturerCourse from "./screens/Lecturer/LecturerCourse";
import LecturerLesson from "./screens/Lecturer/LecturerLesson";
import LecturerResource from "./screens/Lecturer/LecturerResource";
import LecturerResourceForm from "./screens/Lecturer/LecturerResourceForm";
import LecturerQuiz from "./screens/Lecturer/LecturerQuiz";
import LecturerChat from "./screens/Lecturer/LecturerChat";
import LecturerChatParticipants from "./screens/Lecturer/LecturerChatParticipants";
import LecturerResult from "./screens/Lecturer/LecturerResult";

const AppLayout = () => {
    const { pathname } = useLocation();
    const [user] = useContext(MyUserContext);
    const isFullscreen = /^\/courses\/\d+\/learn$/.test(pathname);
    const isDashboard = pathname.startsWith('/admin') || pathname.startsWith('/lecturer');
    const isApprovedLecturer = user?.role === "ADMIN"
        || (user?.role === "LECTURER" && user?.lecturerApproved === true);

    const lecturerPage = (children) => {
        if (!user) {
            return <Navigate to="/login" replace />;
        }

        if (!isApprovedLecturer) {
            return <Navigate to="/" replace />;
        }

        return <LecturerLayout>{children}</LecturerLayout>;
    };

    if (isDashboard) {
        return (
            <Routes>
                <Route path="/admin/dashboard" element={<AdminLayout><AdminDashboard /></AdminLayout>} />
                <Route path="/admin/students" element={<AdminLayout><AdminUser /></AdminLayout>} />
                <Route path="/admin/lecturers" element={<AdminLayout><AdminLecturer /></AdminLayout>} />
                <Route path="/admin/categories" element={<AdminLayout><AdminCategory /></AdminLayout>} />
                <Route path="/admin/payments" element={<AdminLayout><AdminPayment /></AdminLayout>} />
                <Route path="/admin/forum" element={<AdminLayout><AdminForum /></AdminLayout>} />
                <Route path="/admin/reports" element={<Navigate to="/admin/dashboard" replace />} />

                <Route path="/lecturer/dashboard" element={lecturerPage(<LecturerDashboard />)} />
                <Route path="/lecturer/courses" element={lecturerPage(<LecturerCourse />)} />
                <Route path="/lecturer/courses/:id/lessons" element={lecturerPage(<LecturerLesson />)} />
                <Route path="/lecturer/resources" element={lecturerPage(<LecturerResource />)} />
                <Route path="/lecturer/resources/create" element={lecturerPage(<LecturerResourceForm />)} />
                <Route path="/lecturer/resources/:id/edit" element={lecturerPage(<LecturerResourceForm />)} />
                <Route path="/lecturer/quizzes" element={lecturerPage(<LecturerQuiz />)} />
                <Route path="/lecturer/chat" element={lecturerPage(<LecturerChat />)} />
                <Route path="/lecturer/chat/:id/participants" element={lecturerPage(<LecturerChatParticipants />)} />
                <Route path="/lecturer/chat/messages" element={lecturerPage(<Chat />)} />
                <Route path="/lecturer/results" element={lecturerPage(<LecturerResult />)} />
            </Routes>
        );
    }

    return (
        <div className={isFullscreen ? "" : "d-flex flex-column min-vh-100"}>
            {!isFullscreen && <Header />}
            <main className={isFullscreen ? "" : "flex-grow-1"}>
                <Routes>
                    <Route path="/" element={<Home />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register/student" element={<StudentRegister />} />
                    <Route path="/register/lecturer" element={<LecturerRegister />} />
                    <Route path="/resources" element={<ResourceBrowse />} />
                    <Route path="/resources/:id" element={<ResourceDetail />} />
                    <Route path="/courses" element={<CourseBrowse />} />
                    <Route path="/courses/:id" element={<CourseDetail />} />
                    <Route path="/courses/:id/learn" element={<CourseLearn />} />
                    <Route path="/student/dashboard" element={<StudentDashboard />} />
                    <Route path="/my-courses" element={<MyCourses />} />
                    <Route path="/profile" element={<StudentProfile />} />
                    <Route path="/learning-path" element={<LearningPath />} />
                    <Route path="/quizzes" element={<QuizList />} />
                    <Route path="/quizzes/:id/take" element={<QuizTaking />} />
                    <Route path="/quizzes/:id/result" element={<QuizResult />} />
                    <Route path="/forum" element={<Forum />} />
                    <Route path="/forum/threads/:threadId" element={<ForumThread />} />
                    <Route path="/forum/new-thread" element={<NewThread />} />
                    <Route path="/chat" element={<Chat />} />
                    <Route path="/payments" element={<PaymentHistory />} />
                    <Route path="/checkout/:courseId" element={<Checkout />} />
                    <Route path="/payments/momo-result" element={<MoMoResult />} />
                </Routes>
            </main>
            {!isFullscreen && <Footer />}
        </div>
    );
};

const App = () => {
    const [user, dispatch] = useReducer(MyUserReducer, cookies.load('user') || null);

    return (
        <MyUserContext.Provider value={[user, dispatch]}>
            <BrowserRouter>
                <AppLayout />
            </BrowserRouter>
        </MyUserContext.Provider>
    );
}

export default App;

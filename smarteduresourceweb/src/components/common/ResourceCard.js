import { useNavigate } from "react-router-dom";
import { formatViewCount, formatDate, getFormatLabel, getTypeLabel } from "../../configs/MockData";

const getHeaderClass = (format, types) => {
    const typeLabel = getTypeLabel(types);
    if (typeLabel === "Đề thi") return "format-exam";
    switch (format) {
        case "PDF": return "format-pdf";
        case "MP4": return "format-video";
        case "PPTX": return "format-slide";
        default: return "format-default";
    }
};

const getDotClass = (format, types) => {
    const typeLabel = getTypeLabel(types);
    if (typeLabel === "Đề thi") return "dot-exam";
    switch (format) {
        case "PDF": return "dot-pdf";
        case "MP4": return "dot-video";
        case "PPTX": return "dot-slide";
        default: return "dot-default";
    }
};

const getBadgeLabel = (format, types) => {
    const typeLabel = getTypeLabel(types);
    if (typeLabel === "Đề thi") return "Đề thi";
    return getFormatLabel(format);
};

const ResourceCard = ({ resource }) => {
    const nav = useNavigate();
    const r = resource || {};
    const isPaidCourseResource = (r.paidCourses || []).length > 0;
    const isLocked = isPaidCourseResource && r.hasFreePath === false;

    return (
        <div className="res-card" onClick={() => nav(`/resources/${r.id}`)}>
            <div className={`res-card-header ${getHeaderClass(r.format, r.types)}`}>
                {r.thumbnailUrl && (
                    <img src={r.thumbnailUrl} alt={r.title} className="res-thumbnail" />
                )}
                <div className="res-type-badge">
                    <span className={`badge-dot ${getDotClass(r.format, r.types)}`}></span>
                    {getBadgeLabel(r.format, r.types)}
                </div>
                <div className="res-tags-group">
                    <span className="res-subject-tag">{r.subjects?.[0]?.name || "Chưa phân loại"}</span>
                    {isLocked && (
                        <span className="res-subject-tag res-locked-tag">
                            <i className="bi bi-lock-fill"></i> Khóa học trả phí
                        </span>
                    )}
                    {isPaidCourseResource && !isLocked && (
                        <span className="res-subject-tag">Có trong khóa học trả phí</span>
                    )}
                </div>
            </div>
            <div className="res-card-body">
                <div className="title">{r.title}</div>
                <div className="desc">{r.description}</div>
                <div className="res-card-footer">
                    <span className="footer-item">
                        {formatDate(r.createdAt)}
                    </span>
                </div>
            </div>
        </div>
    );
};

export default ResourceCard;

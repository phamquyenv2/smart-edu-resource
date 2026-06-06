/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import com.paq.utils.constant.RoleEnum;

/**
 *
 * @author paqvi
 */
@Entity
@Table(name = "user")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "User.findAll", query = "SELECT u FROM User u"),
    @NamedQuery(name = "User.findById", query = "SELECT u FROM User u WHERE u.id = :id"),
    @NamedQuery(name = "User.findByFullName", query = "SELECT u FROM User u WHERE u.fullName = :fullName"),
    @NamedQuery(name = "User.findByUsername", query = "SELECT u FROM User u WHERE u.username = :username"),
    @NamedQuery(name = "User.findByEmail", query = "SELECT u FROM User u WHERE u.email = :email"),
    @NamedQuery(name = "User.findByPassword", query = "SELECT u FROM User u WHERE u.password = :password"),
    @NamedQuery(name = "User.findByAvatar", query = "SELECT u FROM User u WHERE u.avatar = :avatar"),
    @NamedQuery(name = "User.findByPhone", query = "SELECT u FROM User u WHERE u.phone = :phone"),
    @NamedQuery(name = "User.findByRole", query = "SELECT u FROM User u WHERE u.role = :role"),
    @NamedQuery(name = "User.findByCreatedAt", query = "SELECT u FROM User u WHERE u.createdAt = :createdAt"),
    @NamedQuery(name = "User.findByIsActive", query = "SELECT u FROM User u WHERE u.isActive = :isActive")})
public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;

    @Version
    @Column(name = "version")
    private Long version;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "full_name")
    private String fullName;
    @Size(max = 100)
    @Column(name = "username", unique = true)
    private String username;
    // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "email", unique = true)
    private String email;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @Size(max = 255)
    @Column(name = "avatar")
    private String avatar;
    // @Pattern(regexp="^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$", message="Invalid phone/fax format, should be as xxx-xxx-xxxx")//if the field contains phone or fax number consider using this annotation to enforce field validation
    @Size(max = 20)
    @Column(name = "phone")
    private String phone;
    @Basic(optional = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private RoleEnum role;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "is_active")
    private Boolean isActive;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "userId")
    @JsonIgnore
    private Student student;
    @OneToMany(mappedBy = "createdBy")
    @JsonIgnore
    private Set<Subject> subjectSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "senderId")
    @JsonIgnore
    private Set<ChatMessage> chatMessageSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    @JsonIgnore
    private Set<Notification> notificationSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    @JsonIgnore
    private Set<ChatParticipant> chatParticipantSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "createdBy")
    @JsonIgnore
    private Set<Quiz> quizSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    @JsonIgnore
    private Set<ForumPost> forumPostSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "uploadBy")
    @JsonIgnore
    private Set<Resource> resourceSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    @JsonIgnore
    private Set<InteractionReply> interactionReplySet;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "userId")
    @JsonIgnore
    private Lecturer lecturer;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "createdBy")
    @JsonIgnore
    private Set<ChatRoom> chatRoomSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "createdBy")
    @JsonIgnore
    private Set<ForumThread> forumThreadSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    @JsonIgnore
    private Set<Interaction> interactionSet;
    @OneToMany(mappedBy = "recommendedBy")
    @JsonIgnore
    private Set<LearningPath> learningPathSet;

    public User() {
    }

    public User(Integer id) {
        this.id = id;
    }

    public User(Integer id, String fullName, String email, String password, RoleEnum role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum role) {
        this.role = role;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    @XmlTransient
    public Set<Subject> getSubjectSet() {
        return subjectSet;
    }

    public void setSubjectSet(Set<Subject> subjectSet) {
        this.subjectSet = subjectSet;
    }

    @XmlTransient
    public Set<ChatMessage> getChatMessageSet() {
        return chatMessageSet;
    }

    public void setChatMessageSet(Set<ChatMessage> chatMessageSet) {
        this.chatMessageSet = chatMessageSet;
    }

    @XmlTransient
    public Set<Notification> getNotificationSet() {
        return notificationSet;
    }

    public void setNotificationSet(Set<Notification> notificationSet) {
        this.notificationSet = notificationSet;
    }

    @XmlTransient
    public Set<ChatParticipant> getChatParticipantSet() {
        return chatParticipantSet;
    }

    public void setChatParticipantSet(Set<ChatParticipant> chatParticipantSet) {
        this.chatParticipantSet = chatParticipantSet;
    }

    @XmlTransient
    public Set<Quiz> getQuizSet() {
        return quizSet;
    }

    public void setQuizSet(Set<Quiz> quizSet) {
        this.quizSet = quizSet;
    }

    @XmlTransient
    public Set<ForumPost> getForumPostSet() {
        return forumPostSet;
    }

    public void setForumPostSet(Set<ForumPost> forumPostSet) {
        this.forumPostSet = forumPostSet;
    }

    @XmlTransient
    public Set<Resource> getResourceSet() {
        return resourceSet;
    }

    public void setResourceSet(Set<Resource> resourceSet) {
        this.resourceSet = resourceSet;
    }

    @XmlTransient
    public Set<InteractionReply> getInteractionReplySet() {
        return interactionReplySet;
    }

    public void setInteractionReplySet(Set<InteractionReply> interactionReplySet) {
        this.interactionReplySet = interactionReplySet;
    }

    public Lecturer getLecturer() {
        return lecturer;
    }

    public void setLecturer(Lecturer lecturer) {
        this.lecturer = lecturer;
    }

    @XmlTransient
    public Set<ChatRoom> getChatRoomSet() {
        return chatRoomSet;
    }

    public void setChatRoomSet(Set<ChatRoom> chatRoomSet) {
        this.chatRoomSet = chatRoomSet;
    }

    @XmlTransient
    public Set<ForumThread> getForumThreadSet() {
        return forumThreadSet;
    }

    public void setForumThreadSet(Set<ForumThread> forumThreadSet) {
        this.forumThreadSet = forumThreadSet;
    }

    @XmlTransient
    public Set<Interaction> getInteractionSet() {
        return interactionSet;
    }

    public void setInteractionSet(Set<Interaction> interactionSet) {
        this.interactionSet = interactionSet;
    }

    @XmlTransient
    public Set<LearningPath> getLearningPathSet() {
        return learningPathSet;
    }

    public void setLearningPathSet(Set<LearningPath> learningPathSet) {
        this.learningPathSet = learningPathSet;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof User)) {
            return false;
        }
        User other = (User) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.paq.pojo.User[ id=" + id + " ]";
    }
    
}

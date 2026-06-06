/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
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

/**
 *
 * @author paqvi
 */
@Entity
@Table(name = "forum_thread")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ForumThread.findAll", query = "SELECT f FROM ForumThread f"),
    @NamedQuery(name = "ForumThread.findById", query = "SELECT f FROM ForumThread f WHERE f.id = :id"),
    @NamedQuery(name = "ForumThread.findByTitle", query = "SELECT f FROM ForumThread f WHERE f.title = :title"),
    @NamedQuery(name = "ForumThread.findByIsLock", query = "SELECT f FROM ForumThread f WHERE f.isLock = :isLock"),
    @NamedQuery(name = "ForumThread.findByCreatedAt", query = "SELECT f FROM ForumThread f WHERE f.createdAt = :createdAt"),
    @NamedQuery(name = "ForumThread.findByUpdateAt", query = "SELECT f FROM ForumThread f WHERE f.updateAt = :updateAt")})
public class ForumThread implements Serializable {

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
    @Size(min = 1, max = 255)
    @Column(name = "title")
    private String title;
    @Lob
    @Size(max = 65535)
    @Column(name = "content")
    private String content;
    @Column(name = "is_lock")
    private Boolean isLock;
    @Column(name = "is_deleted")
    private Boolean isDeleted;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "update_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateAt;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "threadId")
    @JsonIgnore
    private Set<ForumPost> forumPostSet;
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private ForumCategory categoryId;
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private User createdBy;

    public ForumThread() {
    }

    public ForumThread(Integer id) {
        this.id = id;
    }

    public ForumThread(Integer id, String title) {
        this.id = id;
        this.title = title;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsLock() {
        return isLock;
    }

    public void setIsLock(Boolean isLock) {
        this.isLock = isLock;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }

    @XmlTransient
    public Set<ForumPost> getForumPostSet() {
        return forumPostSet;
    }

    public void setForumPostSet(Set<ForumPost> forumPostSet) {
        this.forumPostSet = forumPostSet;
    }

    public ForumCategory getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(ForumCategory categoryId) {
        this.categoryId = categoryId;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
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
        if (!(object instanceof ForumThread)) {
            return false;
        }
        ForumThread other = (ForumThread) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.paq.pojo.ForumThread[ id=" + id + " ]";
    }
    
}

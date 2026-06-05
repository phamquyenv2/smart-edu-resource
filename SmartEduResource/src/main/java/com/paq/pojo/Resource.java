/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import com.paq.utils.constant.FormatEnum;
import com.paq.utils.constant.LevelEnum;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
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

/**
 *
 * @author paqvi
 */
@Entity
@Table(name = "resource")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Resource.findAll", query = "SELECT r FROM Resource r"),
    @NamedQuery(name = "Resource.findById", query = "SELECT r FROM Resource r WHERE r.id = :id"),
    @NamedQuery(name = "Resource.findByTitle", query = "SELECT r FROM Resource r WHERE r.title = :title"),
    @NamedQuery(name = "Resource.findByFileUrl", query = "SELECT r FROM Resource r WHERE r.fileUrl = :fileUrl"),
    @NamedQuery(name = "Resource.findByThumbnailUrl", query = "SELECT r FROM Resource r WHERE r.thumbnailUrl = :thumbnailUrl"),
    @NamedQuery(name = "Resource.findByFormat", query = "SELECT r FROM Resource r WHERE r.format = :format"),
    @NamedQuery(name = "Resource.findByFileSize", query = "SELECT r FROM Resource r WHERE r.fileSize = :fileSize"),
    @NamedQuery(name = "Resource.findByLevel", query = "SELECT r FROM Resource r WHERE r.level = :level"),
    @NamedQuery(name = "Resource.findByCreatedAt", query = "SELECT r FROM Resource r WHERE r.createdAt = :createdAt"),
    @NamedQuery(name = "Resource.findByUpdateAt", query = "SELECT r FROM Resource r WHERE r.updateAt = :updateAt"),
    @NamedQuery(name = "Resource.findByPageCount", query = "SELECT r FROM Resource r WHERE r.pageCount = :pageCount")})
public class Resource implements Serializable {

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
    @Column(name = "description")
    private String description;
    @Size(max = 255)
    @Column(name = "file_url")
    private String fileUrl;
    @Size(max = 255)
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    @Enumerated(EnumType.STRING)
    @Column(name = "format")
    private FormatEnum format;
    @Column(name = "file_size")
    private Integer fileSize;
    @Enumerated(EnumType.STRING)
    @Column(name = "level")
    private LevelEnum level;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "update_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateAt;
    @Column(name = "page_count")
    private Integer pageCount;
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @JoinTable(name = "resource_type_map", joinColumns = {
        @JoinColumn(name = "resource_id", referencedColumnName = "id")}, inverseJoinColumns = {
        @JoinColumn(name = "type_id", referencedColumnName = "id")})
    @ManyToMany
    private Set<ResourceType> resourceTypeSet;
    @JoinTable(name = "resource_tag_map", joinColumns = {
        @JoinColumn(name = "resource_id", referencedColumnName = "id")}, inverseJoinColumns = {
        @JoinColumn(name = "tag_id", referencedColumnName = "id")})
    @ManyToMany
    private Set<ResourceTag> resourceTagSet;
    @JoinTable(name = "resource_subject", joinColumns = {
        @JoinColumn(name = "resource_id", referencedColumnName = "id")}, inverseJoinColumns = {
        @JoinColumn(name = "subject_id", referencedColumnName = "id")})
    @ManyToMany
    private Set<Subject> subjectSet;
    @JoinTable(name = "resource_topic", joinColumns = {
        @JoinColumn(name = "resource_id", referencedColumnName = "id")}, inverseJoinColumns = {
        @JoinColumn(name = "topic_id", referencedColumnName = "id")})
    @ManyToMany
    private Set<Topic> topicSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "resourceId")
    @JsonIgnore
    private Set<LearningLog> learningLogSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "relatedId")
    @JsonIgnore
    private Set<ResourceRelation> resourceRelationSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sourceId")
    @JsonIgnore
    private Set<ResourceRelation> resourceRelationSet1;
    @JoinColumn(name = "upload_by", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private User uploadBy;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "resourceId")
    @JsonIgnore
    private Set<Interaction> interactionSet;

    public Resource() {
    }

    public Resource(Integer id) {
        this.id = id;
    }

    public Resource(Integer id, String title) {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public FormatEnum getFormat() {
        return format;
    }

    public void setFormat(FormatEnum format) {
        this.format = format;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    public LevelEnum getLevel() {
        return level;
    }

    public void setLevel(LevelEnum level) {
        this.level = level;
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

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    @XmlTransient
    public Set<ResourceType> getResourceTypeSet() {
        return resourceTypeSet;
    }

    public void setResourceTypeSet(Set<ResourceType> resourceTypeSet) {
        this.resourceTypeSet = resourceTypeSet;
    }

    @XmlTransient
    public Set<ResourceTag> getResourceTagSet() {
        return resourceTagSet;
    }

    public void setResourceTagSet(Set<ResourceTag> resourceTagSet) {
        this.resourceTagSet = resourceTagSet;
    }

    @XmlTransient
    public Set<Subject> getSubjectSet() {
        return subjectSet;
    }

    public void setSubjectSet(Set<Subject> subjectSet) {
        this.subjectSet = subjectSet;
    }

    @XmlTransient
    public Set<Topic> getTopicSet() {
        return topicSet;
    }

    public void setTopicSet(Set<Topic> topicSet) {
        this.topicSet = topicSet;
    }

    @XmlTransient
    public Set<LearningLog> getLearningLogSet() {
        return learningLogSet;
    }

    public void setLearningLogSet(Set<LearningLog> learningLogSet) {
        this.learningLogSet = learningLogSet;
    }

    @XmlTransient
    public Set<ResourceRelation> getResourceRelationSet() {
        return resourceRelationSet;
    }

    public void setResourceRelationSet(Set<ResourceRelation> resourceRelationSet) {
        this.resourceRelationSet = resourceRelationSet;
    }

    @XmlTransient
    public Set<ResourceRelation> getResourceRelationSet1() {
        return resourceRelationSet1;
    }

    public void setResourceRelationSet1(Set<ResourceRelation> resourceRelationSet1) {
        this.resourceRelationSet1 = resourceRelationSet1;
    }

    public User getUploadBy() {
        return uploadBy;
    }

    public void setUploadBy(User uploadBy) {
        this.uploadBy = uploadBy;
    }

    @XmlTransient
    public Set<Interaction> getInteractionSet() {
        return interactionSet;
    }

    public void setInteractionSet(Set<Interaction> interactionSet) {
        this.interactionSet = interactionSet;
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
        if (!(object instanceof Resource)) {
            return false;
        }
        Resource other = (Resource) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.paq.pojo.Resource[ id=" + id + " ]";
    }

    public Object getCreatedDate() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}

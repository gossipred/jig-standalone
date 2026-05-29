package com.jj.jig.jig;

import com.jj.jig.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "jigs")
public class Jig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String classification;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "jig_name", nullable = false, length = 150)
    private String jigName;

    private String customer;

    @Column(name = "jig_no", nullable = false, unique = true, length = 20)
    private String jigNo;

    @Column(name = "jig_base_no", nullable = false, length = 12)
    private String jigBaseNo;

    @Column(name = "set_no", length = 2)
    private String setNo;

    @Column(name = "assembly_line")
    private String assemblyLine;

    @Column(name = "jig_picture_url", length = 500)
    private String jigPictureUrl;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "mro_no")
    private String mroNo;

    @Column(name = "pr_no")
    private String prNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JigStatus status = JigStatus.Normal;

    private String dri;

    @Column(length = 1000)
    private String note;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "due_date_updated_at")
    private LocalDateTime dueDateUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "due_date_updated_by")
    private User dueDateUpdatedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "notion_page_id")
    private String notionPageId;

    @Column(name = "notion_last_edited")
    private LocalDateTime notionLastEdited;

    @Column(name = "last_imported_at")
    private LocalDateTime lastImportedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getJigName() {
        return jigName;
    }

    public void setJigName(String jigName) {
        this.jigName = jigName;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getJigNo() {
        return jigNo;
    }

    public void setJigNo(String jigNo) {
        this.jigNo = jigNo;
    }

    public String getJigBaseNo() {
        return jigBaseNo;
    }

    public void setJigBaseNo(String jigBaseNo) {
        this.jigBaseNo = jigBaseNo;
    }

    public String getSetNo() {
        return setNo;
    }

    public void setSetNo(String setNo) {
        this.setNo = setNo;
    }

    public String getAssemblyLine() {
        return assemblyLine;
    }

    public void setAssemblyLine(String assemblyLine) {
        this.assemblyLine = assemblyLine;
    }

    public String getJigPictureUrl() {
        return jigPictureUrl;
    }

    public void setJigPictureUrl(String jigPictureUrl) {
        this.jigPictureUrl = jigPictureUrl;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getMroNo() {
        return mroNo;
    }

    public void setMroNo(String mroNo) {
        this.mroNo = mroNo;
    }

    public String getPrNo() {
        return prNo;
    }

    public void setPrNo(String prNo) {
        this.prNo = prNo;
    }

    public JigStatus getStatus() {
        return status;
    }

    public void setStatus(JigStatus status) {
        this.status = status;
    }

    public String getDri() {
        return dri;
    }

    public void setDri(String dri) {
        this.dri = dri;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getDueDateUpdatedAt() {
        return dueDateUpdatedAt;
    }

    public void setDueDateUpdatedAt(LocalDateTime dueDateUpdatedAt) {
        this.dueDateUpdatedAt = dueDateUpdatedAt;
    }

    public User getDueDateUpdatedBy() {
        return dueDateUpdatedBy;
    }

    public void setDueDateUpdatedBy(User dueDateUpdatedBy) {
        this.dueDateUpdatedBy = dueDateUpdatedBy;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getNotionPageId() {
        return notionPageId;
    }

    public void setNotionPageId(String notionPageId) {
        this.notionPageId = notionPageId;
    }

    public LocalDateTime getNotionLastEdited() {
        return notionLastEdited;
    }

    public void setNotionLastEdited(LocalDateTime notionLastEdited) {
        this.notionLastEdited = notionLastEdited;
    }

    public LocalDateTime getLastImportedAt() {
        return lastImportedAt;
    }

    public void setLastImportedAt(LocalDateTime lastImportedAt) {
        this.lastImportedAt = lastImportedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

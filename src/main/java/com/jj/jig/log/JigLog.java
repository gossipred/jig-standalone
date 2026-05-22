package com.jj.jig.log;

import com.jj.jig.jig.Jig;
import com.jj.jig.jig.JigStatus;
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

@Entity
@Table(name = "jig_logs")
public class JigLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jig_id", nullable = false)
    private Jig jig;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private JigLogActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 20)
    private JigStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 20)
    private JigStatus newStatus;

    @Column(name = "old_due_date")
    private LocalDate oldDueDate;

    @Column(name = "new_due_date")
    private LocalDate newDueDate;

    @Column(length = 1000)
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public Jig getJig() {
        return jig;
    }

    public void setJig(Jig jig) {
        this.jig = jig;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public JigLogActionType getActionType() {
        return actionType;
    }

    public void setActionType(JigLogActionType actionType) {
        this.actionType = actionType;
    }

    public JigStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(JigStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public JigStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(JigStatus newStatus) {
        this.newStatus = newStatus;
    }

    public LocalDate getOldDueDate() {
        return oldDueDate;
    }

    public void setOldDueDate(LocalDate oldDueDate) {
        this.oldDueDate = oldDueDate;
    }

    public LocalDate getNewDueDate() {
        return newDueDate;
    }

    public void setNewDueDate(LocalDate newDueDate) {
        this.newDueDate = newDueDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

package com.jj.jig.jig;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class JigStatusUpdateForm {

    @NotNull
    private JigStatus status;

    @Size(max = 1000)
    private String note;

    public JigStatus getStatus() {
        return status;
    }

    public void setStatus(JigStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

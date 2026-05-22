package com.jj.jig.jig;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class JigDueDateUpdateForm {

    private LocalDate dueDate;

    @Size(max = 1000)
    private String note;

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

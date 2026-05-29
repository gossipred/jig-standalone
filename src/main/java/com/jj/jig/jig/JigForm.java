package com.jj.jig.jig;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public class JigForm {

    @Size(max = 100)
    private String classification;

    @NotBlank
    @Size(max = 100)
    private String modelName;

    @NotBlank
    @Size(max = 150)
    private String jigName;

    @Size(max = 100)
    private String customer;

    @NotBlank
    @Size(max = 20)
    private String jigNo;

    @Size(max = 100)
    private String assemblyLine;

    @Size(max = 500)
    private String jigPictureUrl;

    private List<MultipartFile> jigFiles = new ArrayList<>();

    @NotNull
    @Min(0)
    private Integer quantity = 1;

    @Size(max = 100)
    private String mroNo;

    @Size(max = 100)
    private String prNo;

    @NotNull
    private JigStatus status = JigStatus.Normal;

    @Size(max = 100)
    private String dri;

    @Size(max = 1000)
    private String note;

    private LocalDate startDate;

    private LocalDate dueDate;

    public static JigForm from(Jig jig) {
        JigForm form = new JigForm();
        form.setClassification(jig.getClassification());
        form.setModelName(jig.getModelName());
        form.setJigName(jig.getJigName());
        form.setCustomer(jig.getCustomer());
        form.setJigNo(jig.getJigNo());
        form.setAssemblyLine(jig.getAssemblyLine());
        form.setJigPictureUrl(jig.getJigPictureUrl());
        form.setQuantity(jig.getQuantity());
        form.setMroNo(jig.getMroNo());
        form.setPrNo(jig.getPrNo());
        form.setStatus(jig.getStatus());
        form.setDri(jig.getDri());
        form.setNote(jig.getNote());
        form.setStartDate(jig.getStartDate());
        form.setDueDate(jig.getDueDate());
        return form;
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

    public List<MultipartFile> getJigFiles() {
        return jigFiles;
    }

    public void setJigFiles(List<MultipartFile> jigFiles) {
        this.jigFiles = jigFiles;
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
}

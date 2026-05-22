package com.jj.jig.jig;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JigCsvExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] export(List<Jig> jigs) {
        StringBuilder csv = new StringBuilder();
        csv.append("Classification,Model Name,Jig Name,Customer,Jig No.,Jig Base No.,Set No.,Ass'y Line,Quantity,MRO No.,PR No.,Status,DRI,Start Date,Due Date,Due Date Updated At,Due Date Updated By\n");

        for (Jig jig : jigs) {
            appendRow(csv, jig);
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void appendRow(StringBuilder csv, Jig jig) {
        append(csv, jig.getClassification());
        append(csv, jig.getModelName());
        append(csv, jig.getJigName());
        append(csv, jig.getCustomer());
        append(csv, jig.getJigNo());
        append(csv, jig.getJigBaseNo());
        append(csv, jig.getSetNo());
        append(csv, jig.getAssemblyLine());
        append(csv, jig.getQuantity());
        append(csv, jig.getMroNo());
        append(csv, jig.getPrNo());
        append(csv, jig.getStatus());
        append(csv, jig.getDri());
        append(csv, formatDate(jig.getStartDate()));
        append(csv, formatDate(jig.getDueDate()));
        append(csv, formatDateTime(jig.getDueDateUpdatedAt()));
        append(csv, jig.getDueDateUpdatedBy() == null ? null : jig.getDueDateUpdatedBy().getUsername(), true);
        csv.append('\n');
    }

    private void append(StringBuilder csv, Object value) {
        append(csv, value, false);
    }

    private void append(StringBuilder csv, Object value, boolean lastColumn) {
        String text = value == null ? "" : value.toString();
        csv.append('"').append(text.replace("\"", "\"\"")).append('"');
        if (!lastColumn) {
            csv.append(',');
        }
    }

    private String formatDate(LocalDate value) {
        return value == null ? null : DATE_FORMAT.format(value);
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : DATE_TIME_FORMAT.format(value);
    }
}

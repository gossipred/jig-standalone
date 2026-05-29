package com.jj.jig.jig;

import com.jj.jig.importlog.ImportLog;
import com.jj.jig.importlog.ImportLogRepository;
import com.jj.jig.importlog.ImportLogStatus;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class JigCsvImportService {

    // Maps lowercase CSV header → internal field key
    private static final Map<String, String> HEADER_ALIASES = new HashMap<>();
    static {
        HEADER_ALIASES.put("jig no.",        "jigNo");
        HEADER_ALIASES.put("jig no",         "jigNo");
        HEADER_ALIASES.put("jig_no",         "jigNo");
        HEADER_ALIASES.put("jig number",     "jigNo");
        HEADER_ALIASES.put("model name",     "modelName");
        HEADER_ALIASES.put("model_name",     "modelName");
        HEADER_ALIASES.put("modelname",      "modelName");
        HEADER_ALIASES.put("jig name",       "jigName");
        HEADER_ALIASES.put("jig_name",       "jigName");
        HEADER_ALIASES.put("jigname",        "jigName");
        HEADER_ALIASES.put("classification", "classification");
        HEADER_ALIASES.put("class",          "classification");
        HEADER_ALIASES.put("customer",       "customer");
        HEADER_ALIASES.put("ass'y line",     "assemblyLine");
        HEADER_ALIASES.put("assembly line",  "assemblyLine");
        HEADER_ALIASES.put("assembly_line",  "assemblyLine");
        HEADER_ALIASES.put("line",           "assemblyLine");
        HEADER_ALIASES.put("quantity",       "quantity");
        HEADER_ALIASES.put("qty",            "quantity");
        HEADER_ALIASES.put("mro no.",        "mroNo");
        HEADER_ALIASES.put("mro no",         "mroNo");
        HEADER_ALIASES.put("mro_no",         "mroNo");
        HEADER_ALIASES.put("mro",            "mroNo");
        HEADER_ALIASES.put("pr no.",         "prNo");
        HEADER_ALIASES.put("pr no",          "prNo");
        HEADER_ALIASES.put("pr_no",          "prNo");
        HEADER_ALIASES.put("pr",             "prNo");
        HEADER_ALIASES.put("status",         "status");
        HEADER_ALIASES.put("dri",            "dri");
        HEADER_ALIASES.put("start date",     "startDate");
        HEADER_ALIASES.put("start_date",     "startDate");
        HEADER_ALIASES.put("due date",       "dueDate");
        HEADER_ALIASES.put("due_date",       "dueDate");
    }

    private static final List<DateTimeFormatter> DATE_PARSERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
    );

    public record ParsedRow(
            int lineNumber,
            String jigNo,
            String modelName,
            String jigName,
            JigForm form,
            boolean isExisting,
            String validationError
    ) {
        public boolean isValid() { return validationError == null && form != null; }
    }

    public record ImportResult(
            int totalRows, int created, int updated, int skipped, int failed,
            List<String> errors
    ) {}

    private final JigRepository jigRepository;
    private final JigService jigService;
    private final ImportLogRepository importLogRepository;

    public JigCsvImportService(JigRepository jigRepository, JigService jigService,
                                ImportLogRepository importLogRepository) {
        this.jigRepository        = jigRepository;
        this.jigService           = jigService;
        this.importLogRepository  = importLogRepository;
    }

    // ===== Preview =====

    public List<ParsedRow> preview(Path csvFile) throws IOException {
        List<List<String>> rawRows = parseCsv(csvFile);
        if (rawRows.isEmpty()) return List.of();

        Map<Integer, String> colMap = buildColumnMap(rawRows.get(0));
        if (!colMap.containsValue("jigNo")) {
            throw new IOException("CSV is missing required column 'Jig No.' / 找不到「Jig No.」欄位，請確認 CSV 格式。");
        }

        List<ParsedRow> result = new ArrayList<>();
        for (int i = 1; i < rawRows.size(); i++) {
            List<String> row = rawRows.get(i);
            if (row.stream().allMatch(String::isBlank)) continue;
            result.add(parseRow(i + 1, row, colMap));
        }
        return result;
    }

    // ===== Execute (no outer @Transactional — each create/update is its own tx) =====

    public ImportResult execute(List<ParsedRow> rows, boolean updateExisting, String username) {
        int created = 0, updated = 0, skipped = 0, failed = 0;
        List<String> errors = new ArrayList<>();

        ImportLog importLog = new ImportLog();
        importLog.setSourceType("CSV");
        importLog.setStartedAt(LocalDateTime.now());
        importLog.setTotalCount(rows.size());

        for (ParsedRow row : rows) {
            if (!row.isValid()) {
                skipped++;
                errors.add("Line " + row.lineNumber() + ": " + row.validationError());
                continue;
            }
            try {
                if (row.isExisting()) {
                    if (updateExisting) {
                        Jig existing = jigRepository.findByJigNo(row.jigNo()).orElse(null);
                        if (existing != null) {
                            jigService.update(existing.getId(), row.form(), username);
                            updated++;
                        } else {
                            skipped++;
                        }
                    } else {
                        skipped++;
                    }
                } else {
                    jigService.create(row.form(), username);
                    created++;
                }
            } catch (Exception e) {
                failed++;
                errors.add("Line " + row.lineNumber()
                        + " (" + row.jigNo() + "): " + e.getMessage());
            }
        }

        importLog.setFinishedAt(LocalDateTime.now());
        importLog.setCreatedCount(created);
        importLog.setUpdatedCount(updated);
        importLog.setSkippedCount(skipped);
        importLog.setFailedCount(failed);
        if (failed > 0 && (created + updated) > 0) {
            importLog.setStatus(ImportLogStatus.PARTIAL);
        } else if (failed > 0) {
            importLog.setStatus(ImportLogStatus.FAILED);
        } else {
            importLog.setStatus(ImportLogStatus.SUCCESS);
        }
        String errorMsg = String.join("\n", errors);
        importLog.setMessage(errorMsg.isEmpty() ? null
                : errorMsg.substring(0, Math.min(2000, errorMsg.length())));
        importLogRepository.save(importLog);

        return new ImportResult(rows.size(), created, updated, skipped, failed, errors);
    }

    // ===== Template =====

    public byte[] generateTemplate() {
        String csv = "Jig No.,Model Name,Jig Name,Classification,Customer,Ass'y Line,Quantity,MRO No.,PR No.,Status,DRI,Start Date,Due Date\n"
                   + "AM-ME-001,iPro4 12P,Main Frame Tray,JIG,APPLE,L01,1,MRO-2024-001,PR-2024-001,Normal,admin,2024-01-01,2025-12-31\n"
                   + "AM-ME-002,iPro4 12P,Back Cover Tray,JIG,APPLE,L01,2,,,,admin,,\n";
        return csv.getBytes(StandardCharsets.UTF_8);
    }

    // ===== Private Helpers =====

    private List<List<String>> parseCsv(Path file) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    line = stripBom(line);
                    firstLine = false;
                }
                if (!line.isBlank()) {
                    rows.add(parseLine(line));
                }
            }
        }
        return rows;
    }

    private String stripBom(String line) {
        return line.startsWith("﻿") ? line.substring(1) : line;
    }

    private List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(field.toString().trim());
                    field.setLength(0);
                } else {
                    field.append(c);
                }
            }
        }
        fields.add(field.toString().trim());
        return fields;
    }

    private Map<Integer, String> buildColumnMap(List<String> headers) {
        Map<Integer, String> colMap = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String normalized = headers.get(i).trim().toLowerCase(Locale.ROOT);
            String fieldKey = HEADER_ALIASES.get(normalized);
            if (fieldKey != null) {
                colMap.put(i, fieldKey);
            }
        }
        return colMap;
    }

    private ParsedRow parseRow(int lineNumber, List<String> cells, Map<Integer, String> colMap) {
        Map<String, String> fields = new HashMap<>();
        for (Map.Entry<Integer, String> entry : colMap.entrySet()) {
            int colIdx = entry.getKey();
            String fieldKey = entry.getValue();
            String value = colIdx < cells.size() ? cells.get(colIdx).trim() : "";
            fields.put(fieldKey, value);
        }

        String jigNo    = fields.getOrDefault("jigNo", "").trim();
        String modelName = fields.getOrDefault("modelName", "").trim();
        String jigName  = fields.getOrDefault("jigName", "").trim();

        // Validate required fields
        if (jigNo.isEmpty()) {
            return new ParsedRow(lineNumber, jigNo, modelName, jigName, null, false,
                    "Jig No. is required / 治具編號為必填");
        }
        if (modelName.isEmpty()) {
            return new ParsedRow(lineNumber, jigNo, modelName, jigName, null, false,
                    "Model Name is required / 型號為必填");
        }
        if (jigName.isEmpty()) {
            return new ParsedRow(lineNumber, jigNo, modelName, jigName, null, false,
                    "Jig Name is required / 治具名稱為必填");
        }

        // Validate Jig No. format (basic check)
        try {
            JigNumber.parse(jigNo);
        } catch (IllegalArgumentException e) {
            return new ParsedRow(lineNumber, jigNo, modelName, jigName, null, false,
                    "Invalid Jig No. format: " + e.getMessage());
        }

        JigForm form = new JigForm();
        form.setJigNo(jigNo);
        form.setModelName(modelName);
        form.setJigName(jigName);
        form.setClassification(emptyToNull(fields.get("classification")));
        form.setCustomer(emptyToNull(fields.get("customer")));
        form.setAssemblyLine(emptyToNull(fields.get("assemblyLine")));
        form.setMroNo(emptyToNull(fields.get("mroNo")));
        form.setPrNo(emptyToNull(fields.get("prNo")));
        form.setDri(emptyToNull(fields.get("dri")));

        String qtyStr = fields.get("quantity");
        if (StringUtils.hasText(qtyStr)) {
            try { form.setQuantity(Integer.parseInt(qtyStr.trim())); }
            catch (NumberFormatException ignored) { form.setQuantity(1); }
        } else {
            form.setQuantity(1);
        }

        form.setStatus(parseStatus(fields.get("status")));
        form.setStartDate(parseDate(fields.get("startDate")));
        form.setDueDate(parseDate(fields.get("dueDate")));

        boolean isExisting = jigRepository.existsByJigNo(
                JigNumber.parse(jigNo).jigNo());

        return new ParsedRow(lineNumber, jigNo, modelName, jigName, form, isExisting, null);
    }

    private JigStatus parseStatus(String value) {
        if (!StringUtils.hasText(value)) return JigStatus.Normal;
        String trimmed = value.trim();
        try { return JigStatus.valueOf(trimmed); } catch (IllegalArgumentException ignored) {}
        for (JigStatus s : JigStatus.values()) {
            if (s.getLabel().equalsIgnoreCase(trimmed) || s.getLabelZh().equals(trimmed)) {
                return s;
            }
        }
        return JigStatus.Normal;
    }

    private LocalDate parseDate(String value) {
        if (!StringUtils.hasText(value)) return null;
        String trimmed = value.trim();
        for (DateTimeFormatter fmt : DATE_PARSERS) {
            try { return LocalDate.parse(trimmed, fmt); } catch (DateTimeParseException ignored) {}
        }
        return null;
    }

    private String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

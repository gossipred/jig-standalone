package com.jj.jig.export;

import com.jj.jig.jig.Jig;
import com.jj.jig.jig.JigStatus;
import com.jj.jig.log.JigLog;
import com.jj.jig.log.JigLogActionType;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class JigPdfReportService {

    private static final DateTimeFormatter DT_FMT   = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Color COLOR_HEADER_BG  = new Color(30, 58, 95);
    private static final Color COLOR_HEADER_FG  = Color.WHITE;
    private static final Color COLOR_SUBHDR_BG  = new Color(226, 232, 240);
    private static final Color COLOR_ROW_ALT    = new Color(248, 250, 252);
    private static final Color COLOR_BORDER     = new Color(203, 213, 225);

    // ── Font loading ──────────────────────────────────────────────────────────

    private BaseFont loadCjkBaseFont() {
        String[] candidates = {
            "/System/Library/Fonts/STHeiti Light.ttc,0",
            "/System/Library/Fonts/Hiragino Sans GB.ttc,0",
            "/Library/Fonts/Arial Unicode.ttf",
            "C:/Windows/Fonts/msyh.ttc,0",
            "C:/Windows/Fonts/simsun.ttc,0"
        };
        for (String path : candidates) {
            try {
                return BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } catch (Exception ignored) {}
        }
        try {
            return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            throw new RuntimeException("Cannot load any font", e);
        }
    }

    private Font font(BaseFont bf, float size, int style, Color color) {
        Font f = new Font(bf, size, style);
        f.setColor(color);
        return f;
    }

    // ── Stats Report (A) ──────────────────────────────────────────────────────

    public void exportStatusReport(Map<JigStatus, Long> stats, long total,
                                    List<Jig> jigs, Path outputPath) throws IOException {
        BaseFont bf = loadCjkBaseFont();
        Font titleFont   = font(bf, 18, Font.BOLD,   COLOR_HEADER_BG);
        Font subtitleFont= font(bf, 10, Font.NORMAL,  Color.GRAY);
        Font secFont     = font(bf, 12, Font.BOLD,    COLOR_HEADER_BG);
        Font headerFont  = font(bf,  9, Font.BOLD,    COLOR_HEADER_FG);
        Font dataFont    = font(bf,  9, Font.NORMAL,  Color.DARK_GRAY);
        Font boldData    = font(bf,  9, Font.BOLD,    Color.DARK_GRAY);

        Document doc = new Document(PageSize.A4, 40, 40, 50, 40);
        try (OutputStream os = Files.newOutputStream(outputPath)) {
            PdfWriter.getInstance(doc, os);
            doc.open();

            // Title
            Paragraph title = new Paragraph("JT & Toolings Status Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            Paragraph sub = new Paragraph("Generated: " + LocalDateTime.now().format(DT_FMT), subtitleFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(16);
            doc.add(sub);

            // Section: Status Summary
            Paragraph sec1 = new Paragraph("Status Summary / 狀態摘要", secFont);
            sec1.setSpacingBefore(4);
            sec1.setSpacingAfter(6);
            doc.add(sec1);

            PdfPTable summaryTable = new PdfPTable(3);
            summaryTable.setWidthPercentage(60);
            summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            summaryTable.setSpacingAfter(20);
            summaryTable.setWidths(new float[]{4f, 1.5f, 1.5f});

            addHeaderCell(summaryTable, "Status / 狀態", headerFont);
            addHeaderCell(summaryTable, "Count / 件數", headerFont);
            addHeaderCell(summaryTable, "Percentage", headerFont);

            // Total row
            PdfPCell totalLabel = styledCell("Total / 總計", boldData, COLOR_SUBHDR_BG);
            PdfPCell totalCount = styledCell(String.valueOf(total), boldData, COLOR_SUBHDR_BG);
            PdfPCell totalPct   = styledCell("100%", boldData, COLOR_SUBHDR_BG);
            totalLabel.setHorizontalAlignment(Element.ALIGN_LEFT);
            summaryTable.addCell(totalLabel);
            summaryTable.addCell(totalCount);
            summaryTable.addCell(totalPct);

            int idx = 0;
            for (JigStatus status : JigStatus.values()) {
                long count = stats.getOrDefault(status, 0L);
                String pct = total > 0 ? String.format("%.1f%%", count * 100.0 / total) : "—";
                Color bg = (idx++ % 2 == 0) ? Color.WHITE : COLOR_ROW_ALT;
                PdfPCell lbl = styledCell(status.getLabel() + " / " + status.getLabelZh(), dataFont, bg);
                lbl.setHorizontalAlignment(Element.ALIGN_LEFT);
                summaryTable.addCell(lbl);
                summaryTable.addCell(styledCell(String.valueOf(count), dataFont, bg));
                summaryTable.addCell(styledCell(pct, dataFont, bg));
            }
            doc.add(summaryTable);

            // Section: JT List
            Paragraph sec2 = new Paragraph("JT List / 治模具清單 (" + jigs.size() + " items)", secFont);
            sec2.setSpacingAfter(6);
            doc.add(sec2);

            PdfPTable jigTable = new PdfPTable(7);
            jigTable.setWidthPercentage(100);
            jigTable.setSpacingAfter(10);
            jigTable.setWidths(new float[]{2f, 1.5f, 2f, 2.5f, 1.8f, 1.5f, 1.5f});

            for (String h : new String[]{"JT No.", "Class.", "Model", "JT Name", "Status", "Due Date", "DRI"}) {
                addHeaderCell(jigTable, h, headerFont);
            }

            idx = 0;
            for (Jig jig : jigs) {
                Color bg = (idx++ % 2 == 0) ? Color.WHITE : COLOR_ROW_ALT;
                addDataCell(jigTable, nvl(jig.getJigNo()),        dataFont, bg, Element.ALIGN_LEFT);
                addDataCell(jigTable, nvl(jig.getClassification()),dataFont, bg, Element.ALIGN_LEFT);
                addDataCell(jigTable, nvl(jig.getModelName()),    dataFont, bg, Element.ALIGN_LEFT);
                addDataCell(jigTable, nvl(jig.getJigName()),      dataFont, bg, Element.ALIGN_LEFT);
                String statusText = jig.getStatus() != null
                        ? jig.getStatus().getLabel() + "\n" + jig.getStatus().getLabelZh() : "—";
                addDataCell(jigTable, statusText, dataFont, bg, Element.ALIGN_CENTER);
                String due = jig.getDueDate() != null ? jig.getDueDate().format(DATE_FMT) : "—";
                addDataCell(jigTable, due,                        dataFont, bg, Element.ALIGN_CENTER);
                addDataCell(jigTable, nvl(jig.getDri()),          dataFont, bg, Element.ALIGN_CENTER);
            }
            doc.add(jigTable);

            doc.close();
        }
    }

    // ── Log Report (B) ───────────────────────────────────────────────────────

    public void exportLogReport(List<JigLog> logs, LocalDate from, LocalDate to,
                                 JigLogActionType actionType, String operator,
                                 Path outputPath) throws IOException {
        BaseFont bf = loadCjkBaseFont();
        Font titleFont  = font(bf, 16, Font.BOLD,   COLOR_HEADER_BG);
        Font subFont    = font(bf,  9, Font.NORMAL,  Color.GRAY);
        Font headerFont = font(bf,  9, Font.BOLD,    COLOR_HEADER_FG);
        Font dataFont   = font(bf,  8, Font.NORMAL,  Color.DARK_GRAY);

        Document doc = new Document(PageSize.A4.rotate(), 30, 30, 40, 30);
        try (OutputStream os = Files.newOutputStream(outputPath)) {
            PdfWriter.getInstance(doc, os);
            doc.open();

            // Title
            Paragraph title = new Paragraph("JT Activity Log Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            // Query info
            String rangeStr = (from != null ? from.format(DATE_FMT) : "—")
                    + "  →  " + (to != null ? to.format(DATE_FMT) : LocalDate.now().format(DATE_FMT));
            String filterStr = buildFilterStr(actionType, operator);
            Paragraph sub = new Paragraph(
                    "Period: " + rangeStr + "   |   " + filterStr
                    + "   |   Records: " + logs.size()
                    + "   |   Generated: " + LocalDateTime.now().format(DT_FMT), subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(12);
            doc.add(sub);

            // Table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.2f, 1.8f, 2.2f, 1.8f, 3.5f, 1.5f});

            for (String h : new String[]{"Date / 日期", "JT No.", "JT Name / 名稱",
                    "Action / 動作", "Details / 詳情", "Operator / 操作者"}) {
                addHeaderCell(table, h, headerFont);
            }

            int idx = 0;
            for (JigLog log : logs) {
                Color bg = (idx++ % 2 == 0) ? Color.WHITE : COLOR_ROW_ALT;
                String dt = log.getCreatedAt() != null ? log.getCreatedAt().format(DT_FMT) : "—";
                addDataCell(table, dt, dataFont, bg, Element.ALIGN_CENTER);
                addDataCell(table, log.getJig() != null ? log.getJig().getJigNo() : "—",  dataFont, bg, Element.ALIGN_LEFT);
                addDataCell(table, log.getJig() != null ? nvl(log.getJig().getJigName()) : "—", dataFont, bg, Element.ALIGN_LEFT);
                addDataCell(table, actionLabel(log.getActionType()), dataFont, bg, Element.ALIGN_CENTER);
                addDataCell(table, formatDetails(log), dataFont, bg, Element.ALIGN_LEFT);
                addDataCell(table, log.getUser() != null ? log.getUser().getUsername() : "—", dataFont, bg, Element.ALIGN_CENTER);
            }
            doc.add(table);
            doc.close();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(COLOR_HEADER_BG);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setBorderColor(COLOR_HEADER_BG);
        table.addCell(cell);
    }

    private PdfPCell styledCell(String text, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4);
        cell.setBorderColor(COLOR_BORDER);
        return cell;
    }

    private void addDataCell(PdfPTable table, String text, Font font, Color bg, int align) {
        PdfPCell cell = styledCell(text == null ? "—" : text, font, bg);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }

    private String nvl(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }

    private String actionLabel(JigLogActionType t) {
        if (t == null) return "—";
        return switch (t) {
            case CREATE          -> "Create / 新增";
            case UPDATE          -> "Update / 編輯";
            case DELETE          -> "Delete / 刪除";
            case STATUS_CHANGE   -> "Status / 狀態";
            case DUE_DATE_CHANGE -> "Due Date";
            case NOTE            -> "Note / 備註";
            case FILE_UPLOAD     -> "Upload";
            case FILE_REPLACE    -> "Replace";
            case FILE_DELETE     -> "File Del";
        };
    }

    private String formatDetails(JigLog log) {
        StringBuilder sb = new StringBuilder();
        if (log.getActionType() == JigLogActionType.STATUS_CHANGE) {
            String o = log.getOldStatus() != null ? log.getOldStatus().getLabel() : "—";
            String n = log.getNewStatus() != null ? log.getNewStatus().getLabel() : "—";
            sb.append(o).append(" → ").append(n);
        } else if (log.getActionType() == JigLogActionType.DUE_DATE_CHANGE) {
            String o = log.getOldDueDate() != null ? log.getOldDueDate().format(DATE_FMT) : "—";
            String n = log.getNewDueDate() != null ? log.getNewDueDate().format(DATE_FMT) : "—";
            sb.append(o).append(" → ").append(n);
        }
        if (log.getNote() != null && !log.getNote().isBlank()) {
            if (!sb.isEmpty()) sb.append("  ");
            sb.append(log.getNote());
        }
        return sb.isEmpty() ? "—" : sb.toString();
    }

    private String buildFilterStr(JigLogActionType actionType, String operator) {
        String a = actionType != null ? actionLabel(actionType) : "All Actions";
        String o = (operator == null || operator.isBlank()) ? "All Operators" : operator;
        return "Action: " + a + "   Operator: " + o;
    }
}

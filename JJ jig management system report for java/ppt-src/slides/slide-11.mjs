import { background, title, footer, tableHeader, tableRow, C } from "./common.mjs";

export async function slide11(presentation, ctx) {
  const slide = presentation.slides.add();
  background(slide, ctx);
  title(slide, ctx, "Change Logs And Traceability", "Knowing what changed matters more than only knowing something changed");
  const x = 95, y = 185;
  const widths = [210, 220, 180, 180, 170, 230];
  tableHeader(slide, ctx, x, y, widths, ["Time", "Action", "Old", "New", "User", "Note"]);
  tableRow(slide, ctx, x, y + 42, widths, ["2026-05-22 16:31", "STATUS_CHANGE", "Normal", "On Process", "jj", "Changed from Edit form"], "#EFF6FF");
  tableRow(slide, ctx, x, y + 85, widths, ["2026-05-22 16:28", "DUE_DATE_CHANGE", "2026-05-22", "2026-06-30", "jj", "Maintenance schedule"], "#F0FDFA");
  tableRow(slide, ctx, x, y + 128, widths, ["2026-05-22 16:20", "FILE_REPLACE", "-", "-", "engineer", "Replaced drawing.pdf"], "#FFFBEB");
  ctx.addShape(slide, { x: 160, y: 430, w: 960, h: 90, fill: "#FFFFFF", line: ctx.line("#E5EAF0", 1) });
  ctx.addText(slide, { x: 190, y: 455, w: 900, h: 38, text: "Traceability answers: who, when, action, old value, new value.", fontSize: 26, bold: true, color: C.ink, align: "center" });
  footer(slide, ctx, 11);
  return slide;
}


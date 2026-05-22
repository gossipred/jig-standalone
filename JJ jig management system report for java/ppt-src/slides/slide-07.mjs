import { background, title, footer, tableHeader, tableRow, C } from "./common.mjs";

export async function slide07(presentation, ctx) {
  const slide = presentation.slides.add();
  background(slide, ctx);
  title(slide, ctx, "Role Permission Design", "Factory work roles translated into system access");
  const x = 78, y = 168;
  const widths = [165, 280, 410, 245];
  tableHeader(slide, ctx, x, y, widths, ["Role", "Best for", "Allowed", "Main restriction"]);
  tableRow(slide, ctx, x, y + 42, widths, ["ADMIN", "System owner", "Manage users, approve scrap, edit all data", "None in first release"], "#FFF7ED");
  tableRow(slide, ctx, x, y + 85, widths, ["SUPERVISOR", "Team lead", "Cross-user edit/delete, status, files, logs", "No user management"], "#FFFBEB");
  tableRow(slide, ctx, x, y + 128, widths, ["ENGINEER", "Maintenance engineer", "Add/edit own jigs, replace own files", "Cannot delete or scrap"], "#EFF6FF");
  tableRow(slide, ctx, x, y + 171, widths, ["OPERATOR", "Production lookup", "Search, view logs, download, export", "Read-only"], "#F8FAFC");
  ctx.addText(slide, { x: 135, y: 450, w: 980, h: 70, text: "Principle: give the smallest role that matches daily work.", fontSize: 30, bold: true, color: C.ink, align: "center" });
  ctx.addText(slide, { x: 190, y: 530, w: 900, h: 40, text: "權限設計的重點是降低誤改與誤刪風險。", fontSize: 19, color: C.muted, align: "center" });
  footer(slide, ctx, 7);
  return slide;
}


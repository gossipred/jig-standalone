import { background, title, footer, card, C } from "./common.mjs";

export async function slide02(presentation, ctx) {
  const slide = presentation.slides.add();
  background(slide, ctx);
  title(slide, ctx, "Project Motivation", "From factory pain points to a Java web system");
  card(slide, ctx, 74, 174, 340, 170, "Scattered Records", "Excel, paper notes, personal folders, and drawing files are hard to search and easy to lose.", C.red);
  card(slide, ctx, 470, 174, 340, 170, "No Traceability", "When a status or due date changes, users need to know who changed it and what changed.", C.yellow);
  card(slide, ctx, 866, 174, 340, 170, "Permission Risk", "Operators, engineers, supervisors, and admins should not share the same system permissions.", C.blue);
  ctx.addText(slide, { x: 120, y: 424, w: 1040, h: 100, text: "Goal: turn real engineering management experience into a usable Java Spring Boot application.", fontSize: 30, bold: true, color: C.ink, align: "center" });
  ctx.addText(slide, { x: 212, y: 540, w: 856, h: 44, text: "目標：把現場治具/模具管理問題，轉成可登入、可查詢、可追蹤、可移交的系統。", fontSize: 20, color: C.muted, align: "center" });
  footer(slide, ctx, 2);
  return slide;
}


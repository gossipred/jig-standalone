import { background, title, footer, node, arrow, C } from "./common.mjs";

export async function slide04(presentation, ctx) {
  const slide = presentation.slides.add();
  background(slide, ctx);
  title(slide, ctx, "System Architecture", "Spring Boot MVC with database-backed security");
  node(slide, ctx, 70, 210, 190, 95, "Browser", "User opens\nlocalhost / intranet IP", C.blue);
  node(slide, ctx, 330, 190, 210, 135, "Controller", "Routes requests\nreturns Thymeleaf pages", C.teal);
  node(slide, ctx, 610, 190, 210, 135, "Service", "Business rules\nlogs, roles, numbering", C.yellow);
  node(slide, ctx, 890, 190, 210, 135, "Repository", "Spring Data JPA\nquery and save", C.red);
  node(slide, ctx, 930, 420, 210, 115, "MySQL", "users, jigs, files, logs", C.green);
  arrow(slide, ctx, 260, 258, 330, 258, C.gray);
  arrow(slide, ctx, 540, 258, 610, 258, C.gray);
  arrow(slide, ctx, 820, 258, 890, 258, C.gray);
  arrow(slide, ctx, 990, 325, 1000, 420, C.gray);
  ctx.addText(slide, { x: 108, y: 430, w: 680, h: 88, text: "Security layer: Spring Security checks login and role-based access before protected operations.", fontSize: 23, bold: true, color: C.ink });
  ctx.addText(slide, { x: 110, y: 530, w: 650, h: 42, text: "畫面、商業邏輯、資料庫分工清楚，比較容易維護與測試。", fontSize: 18, color: C.muted });
  footer(slide, ctx, 4);
  return slide;
}


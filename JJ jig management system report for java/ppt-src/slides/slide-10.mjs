import { background, title, footer, C } from "./common.mjs";

export async function slide10(presentation, ctx) {
  const slide = presentation.slides.add();
  background(slide, ctx);
  title(slide, ctx, "Iterative Development", "User testing changed the system design");
  const items = [
    ["1", "Jig only", "Jig & Toolings"],
    ["2", "URL field", "Real file upload"],
    ["3", "Simple roles", "4-role RBAC"],
    ["4", "Manual DRI", "Login auto-fill"],
    ["5", "Basic status", "Defined colored states"],
    ["6", "Loose update log", "Old/New traceability"],
  ];
  items.forEach((item, i) => {
    const x = i < 3 ? 110 : 690;
    const y = 160 + (i % 3) * 145;
    ctx.addShape(slide, { x, y, w: 64, h: 64, geometry: "ellipse", fill: C.blue, line: ctx.line("transparent", 0) });
    ctx.addText(slide, { x, y: y + 16, w: 64, h: 28, text: item[0], fontSize: 22, bold: true, color: "#FFFFFF", align: "center" });
    ctx.addText(slide, { x: x + 84, y, w: 230, h: 28, text: item[1], fontSize: 16, color: C.muted });
    ctx.addText(slide, { x: x + 84, y: y + 34, w: 360, h: 32, text: item[2], fontSize: 20, bold: true, color: C.ink });
  });
  ctx.addText(slide, { x: 205, y: 610, w: 870, h: 32, text: "Real projects grow through questions, tests, and corrections.", fontSize: 22, bold: true, color: C.ink, align: "center" });
  footer(slide, ctx, 10);
  return slide;
}


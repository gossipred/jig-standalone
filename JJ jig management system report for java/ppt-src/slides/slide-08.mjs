import { background, title, footer, node, arrow, C } from "./common.mjs";

export async function slide08(presentation, ctx) {
  const slide = presentation.slides.add();
  background(slide, ctx);
  title(slide, ctx, "Daily Workflow", "How users work with the system");
  const y = 235;
  node(slide, ctx, 78, y, 165, 95, "Login", "Spring Security", C.blue);
  node(slide, ctx, 282, y, 165, 95, "Search", "keyword list", C.teal);
  node(slide, ctx, 486, y, 165, 95, "Detail", "files + logs", C.yellow);
  node(slide, ctx, 690, y, 165, 95, "Update", "status / due date", C.red);
  node(slide, ctx, 894, y, 165, 95, "Trace", "old/new logs", C.green);
  arrow(slide, ctx, 243, y + 48, 282, y + 48, C.gray);
  arrow(slide, ctx, 447, y + 48, 486, y + 48, C.gray);
  arrow(slide, ctx, 651, y + 48, 690, y + 48, C.gray);
  arrow(slide, ctx, 855, y + 48, 894, y + 48, C.gray);
  ctx.addShape(slide, { x: 190, y: 426, w: 900, h: 92, fill: "#FFFFFF", line: ctx.line("#E5EAF0", 1) });
  ctx.addText(slide, { x: 230, y: 450, w: 820, h: 42, text: "Operators focus on lookup; engineers maintain owned data; supervisors/admins control exceptions.", fontSize: 22, bold: true, color: C.ink, align: "center" });
  footer(slide, ctx, 8);
  return slide;
}


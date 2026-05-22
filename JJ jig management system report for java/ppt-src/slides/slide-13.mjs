import { background, title, footer, node, arrow, C } from "./common.mjs";

export async function slide13(presentation, ctx) {
  const slide = presentation.slides.add();
  background(slide, ctx);
  title(slide, ctx, "Deployment And Handoff Plan", "One server PC, many browser users");
  node(slide, ctx, 95, 250, 230, 115, "Server PC", "Java 17\nMySQL\nSpring Boot JAR", C.blue);
  node(slide, ctx, 525, 190, 230, 95, "Office PC", "Browser only", C.teal);
  node(slide, ctx, 525, 330, 230, 95, "Line PC", "Browser only", C.yellow);
  node(slide, ctx, 900, 260, 230, 95, "URL", "http://192.168.1.50:8080", C.green);
  arrow(slide, ctx, 325, 290, 525, 235, C.gray);
  arrow(slide, ctx, 325, 325, 525, 374, C.gray);
  arrow(slide, ctx, 755, 235, 900, 305, C.gray);
  arrow(slide, ctx, 755, 374, 900, 305, C.gray);
  ctx.addText(slide, { x: 145, y: 505, w: 990, h: 45, text: "Customer does not need to install the system on every computer.", fontSize: 25, bold: true, color: C.ink, align: "center" });
  footer(slide, ctx, 13);
  return slide;
}


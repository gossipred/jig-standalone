import { background, coverFooter, C } from "./common.mjs";

export async function slide01(presentation, ctx) {
  const slide = presentation.slides.add();
  background(slide, ctx, { dark: true });
  ctx.addShape(slide, { x: 72, y: 72, w: 88, h: 8, fill: C.blue });
  ctx.addText(slide, { x: 72, y: 128, w: 970, h: 70, text: "Jig & Toolings\nManagement System", fontSize: 48, bold: true, color: "#FFFFFF", typeface: ctx.fonts.title });
  ctx.addText(slide, { x: 74, y: 258, w: 760, h: 44, text: "治具及模具管理系統", fontSize: 28, bold: true, color: "#D8E5F2" });
  ctx.addText(slide, { x: 74, y: 332, w: 770, h: 70, text: "A Java Spring Boot graduation project for managing fixture/tooling master data, files, permissions, and change records.", fontSize: 22, color: "#B9C6D3" });
  ctx.addShape(slide, { x: 895, y: 140, w: 250, h: 250, fill: "#1F6FEB22", line: ctx.line("#4F8FF7", 2) });
  ctx.addShape(slide, { x: 930, y: 175, w: 180, h: 22, fill: "#4F8FF7" });
  ctx.addShape(slide, { x: 930, y: 220, w: 180, h: 22, fill: "#4F8FF7" });
  ctx.addShape(slide, { x: 930, y: 265, w: 180, h: 22, fill: "#4F8FF7" });
  ctx.addText(slide, { x: 910, y: 432, w: 250, h: 46, text: "Java 17\nSpring Boot + MySQL", fontSize: 21, bold: true, color: "#FFFFFF", align: "center" });
  coverFooter(slide, ctx);
  return slide;
}


import { background, title, footer, bullet, C } from "./common.mjs";

export async function slide14(presentation, ctx) {
  const slide = presentation.slides.add();
  background(slide, ctx);
  title(slide, ctx, "Conclusion And Next Steps", "From Java learning to an operational factory tool");
  bullet(slide, ctx, 110, 175, "Built a complete Java Spring Boot web system, not only static pages.", C.blue, 980);
  bullet(slide, ctx, 110, 238, "Connected engineering management needs with database design and permissions.", C.teal, 980);
  bullet(slide, ctx, 110, 301, "Implemented traceability: who changed what, when, old value, new value.", C.red, 980);
  bullet(slide, ctx, 110, 364, "Verified system behavior through 47 automated tests.", C.green, 980);
  ctx.addShape(slide, { x: 180, y: 480, w: 920, h: 92, fill: C.dark, line: ctx.line("transparent", 0) });
  ctx.addText(slide, { x: 210, y: 506, w: 860, h: 36, text: "Future: deletion audit, one-click handoff, backup/restore, Excel import, QR code lookup.", fontSize: 22, bold: true, color: "#FFFFFF", align: "center" });
  footer(slide, ctx, 14);
  return slide;
}


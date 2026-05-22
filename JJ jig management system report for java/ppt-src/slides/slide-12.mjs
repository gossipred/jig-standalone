import { background, title, footer, card, C } from "./common.mjs";

export async function slide12(presentation, ctx) {
  const slide = presentation.slides.add();
  background(slide, ctx);
  title(slide, ctx, "Testing And Verification", "Automated tests reduce regression risk");
  ctx.addText(slide, { x: 82, y: 170, w: 460, h: 90, text: "47", fontSize: 84, bold: true, color: C.blue, typeface: ctx.fonts.title });
  ctx.addText(slide, { x: 210, y: 197, w: 320, h: 45, text: "tests passed", fontSize: 30, bold: true, color: C.ink });
  ctx.addText(slide, { x: 84, y: 270, w: 480, h: 36, text: "mvn test | Failures: 0 | Errors: 0", fontSize: 18, color: C.muted });
  card(slide, ctx, 600, 160, 250, 130, "Authorization", "ADMIN, SUPERVISOR, ENGINEER, OPERATOR rules", C.red);
  card(slide, ctx, 890, 160, 250, 130, "Jig Numbering", "Semi-auto prefix and duplicate checks", C.blue);
  card(slide, ctx, 600, 340, 250, 130, "Logs", "Status / due date old-new records", C.yellow);
  card(slide, ctx, 890, 340, 250, 130, "Accounts", "Password change and user audit logs", C.green);
  footer(slide, ctx, 12);
  return slide;
}


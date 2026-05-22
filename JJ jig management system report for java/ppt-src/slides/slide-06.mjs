import { background, title, footer, node, arrow, C } from "./common.mjs";

export async function slide06(presentation, ctx) {
  const slide = presentation.slides.add();
  background(slide, ctx);
  title(slide, ctx, "Database Design", "Core tables and traceability relationships");
  node(slide, ctx, 86, 160, 190, 95, "users", "account, role, enabled", C.blue);
  node(slide, ctx, 86, 320, 190, 95, "user_logs", "password, role, enabled\nchange history", C.teal);
  node(slide, ctx, 405, 220, 210, 130, "jigs", "classification, model,\njig no., status, owner", C.red);
  node(slide, ctx, 760, 140, 210, 105, "jig_files", "uploaded drawings\nphotos, PDF, CAD", C.green);
  node(slide, ctx, 760, 330, 210, 105, "jig_logs", "status, due date,\nfile, master changes", C.yellow);
  node(slide, ctx, 1010, 245, 165, 95, "import_logs", "future import\ntracking", C.gray);
  arrow(slide, ctx, 276, 204, 405, 258, C.gray);
  arrow(slide, ctx, 276, 360, 405, 300, C.gray);
  arrow(slide, ctx, 615, 250, 760, 188, C.gray);
  arrow(slide, ctx, 615, 300, 760, 380, C.gray);
  arrow(slide, ctx, 970, 382, 1010, 292, C.gray);
  ctx.addText(slide, { x: 150, y: 520, w: 980, h: 54, text: "Design focus: current state is stored in master tables; accountability is stored in log tables.", fontSize: 24, bold: true, color: C.ink, align: "center" });
  footer(slide, ctx, 6);
  return slide;
}


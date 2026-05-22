import { background, title, footer, bullet, pill, C } from "./common.mjs";

export async function slide03(presentation, ctx) {
  const slide = presentation.slides.add();
  background(slide, ctx);
  title(slide, ctx, "Project Goals", "What the system needs to solve");
  pill(slide, ctx, 78, 164, 170, "Master Data", C.blue);
  pill(slide, ctx, 292, 164, 170, "Files", C.teal);
  pill(slide, ctx, 506, 164, 170, "Roles", C.yellow);
  pill(slide, ctx, 720, 164, 170, "Logs", C.red);
  pill(slide, ctx, 934, 164, 170, "Handoff", C.green);
  bullet(slide, ctx, 118, 260, "Manage jig and tooling master data in one system.", C.blue, 920);
  bullet(slide, ctx, 118, 318, "Upload and download photos, drawings, PDFs, and CAD files.", C.teal, 920);
  bullet(slide, ctx, 118, 376, "Separate permissions for ADMIN, SUPERVISOR, ENGINEER, and OPERATOR.", C.yellow, 920);
  bullet(slide, ctx, 118, 434, "Record who changed what, when, including old and new values.", C.red, 920);
  bullet(slide, ctx, 118, 492, "Plan for simple internal-network deployment and customer handoff.", C.green, 920);
  footer(slide, ctx, 3);
  return slide;
}


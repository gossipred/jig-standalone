import { background, title, footer, C } from "./common.mjs";

export async function slide09(presentation, ctx) {
  const slide = presentation.slides.add();
  background(slide, ctx);
  title(slide, ctx, "Key UI Features", "English-first interface with Chinese helper text");
  ctx.addShape(slide, { x: 75, y: 155, w: 1130, h: 395, fill: "#FFFFFF", line: ctx.line("#D8DEE6", 1) });
  ctx.addShape(slide, { x: 75, y: 155, w: 1130, h: 54, fill: C.dark });
  ctx.addText(slide, { x: 98, y: 170, w: 300, h: 24, text: "Jig / Tooling List", fontSize: 19, bold: true, color: "#FFFFFF" });
  const cols = [["Classification", 95], ["Jig No.", 245], ["Model", 390], ["Ass'y Line", 535], ["Status", 700], ["Owner", 870], ["Actions", 1010]];
  cols.forEach(([label, x]) => ctx.addText(slide, { x, y: 235, w: 130, h: 25, text: label, fontSize: 13, bold: true, color: C.ink }));
  const rows = [
    ["Jig", "AM-ME-001", "Mouton", "C542", "On Process", "admin"],
    ["Tooling", "TM-ME-001", "TV-55", "Line 7", "Maintain", "jj"],
    ["Jig", "PM-ME-001", "Palau", "901", "Repair", "ha"],
  ];
  rows.forEach((row, r) => {
    const y = 278 + r * 58;
    ctx.addShape(slide, { x: 88, y: y - 12, w: 1088, h: 46, fill: r % 2 ? "#F8FAFC" : "#FFFFFF", line: ctx.line("#EEF2F6", 1) });
    [95, 245, 390, 535, 700, 870].forEach((x, i) => ctx.addText(slide, { x, y, w: 140, h: 22, text: row[i], fontSize: 13, color: C.ink }));
    const statusColor = row[4] === "Repair" ? "#F8D7DA" : row[4] === "Maintain" ? "#CFF4FC" : "#CFE2FF";
    ctx.addShape(slide, { x: 700, y: y - 5, w: 112, h: 28, fill: statusColor, line: ctx.line("#AAC4E8", 1) });
    ctx.addText(slide, { x: 708, y: y + 2, w: 96, h: 16, text: row[4], fontSize: 11, bold: true, color: C.ink, align: "center" });
  });
  ctx.addText(slide, { x: 105, y: 485, w: 1040, h: 34, text: "Classification first, color-coded status, searchable visible columns.", fontSize: 24, bold: true, color: C.ink, align: "center" });
  footer(slide, ctx, 9);
  return slide;
}


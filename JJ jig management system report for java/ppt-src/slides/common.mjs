export const C = {
  ink: "#17202A",
  muted: "#667085",
  line: "#D8DEE6",
  bg: "#F4F7FA",
  panel: "#FFFFFF",
  dark: "#101820",
  blue: "#1F6FEB",
  teal: "#0F766E",
  green: "#16A34A",
  yellow: "#F59E0B",
  red: "#DC2626",
  gray: "#6B7280",
  cyan: "#0891B2",
};

export function background(slide, ctx, opts = {}) {
  ctx.addShape(slide, { x: 0, y: 0, w: 1280, h: 720, fill: opts.dark ? C.dark : C.bg });
  const line = opts.dark ? "#25313D" : "#E7ECF2";
  for (let x = -80; x < 1400; x += 120) {
    ctx.addShape(slide, { x, y: 0, w: 1, h: 720, fill: line });
  }
  for (let y = 40; y < 760; y += 100) {
    ctx.addShape(slide, { x: 0, y, w: 1280, h: 1, fill: line });
  }
  ctx.addShape(slide, { x: 980, y: -120, w: 420, h: 420, geometry: "ellipse", fill: opts.dark ? "#17212C" : "#FFFFFF66", line: ctx.line("transparent", 0) });
  ctx.addShape(slide, { x: -150, y: 470, w: 360, h: 360, geometry: "ellipse", fill: opts.dark ? "#17212C" : "#FFFFFF66", line: ctx.line("transparent", 0) });
}

export function title(slide, ctx, text, subtitle = "") {
  ctx.addText(slide, { x: 64, y: 38, w: 860, h: 52, text, fontSize: 31, bold: true, color: C.ink, typeface: ctx.fonts.title });
  if (subtitle) ctx.addText(slide, { x: 66, y: 86, w: 860, h: 28, text: subtitle, fontSize: 15, color: C.muted });
  ctx.addShape(slide, { x: 64, y: 124, w: 86, h: 5, fill: C.blue });
}

export function footer(slide, ctx, n) {
  ctx.addText(slide, { x: 64, y: 676, w: 760, h: 24, text: "Java Programming Graduation Project | Jig & Toolings Management System", fontSize: 11, color: "#7B8794" });
  ctx.addText(slide, { x: 1160, y: 676, w: 60, h: 24, text: String(n).padStart(2, "0"), fontSize: 12, bold: true, color: "#7B8794", align: "right" });
}

export function coverFooter(slide, ctx) {
  ctx.addText(slide, { x: 70, y: 650, w: 760, h: 28, text: "Course: Java 程式設計  |  Author: JJ", fontSize: 17, color: "#D0DAE5" });
}

export function card(slide, ctx, x, y, w, h, heading, body, color = C.blue) {
  ctx.addShape(slide, { x, y, w, h, fill: C.panel, line: ctx.line("#E5EAF0", 1) });
  ctx.addShape(slide, { x, y, w: 6, h, fill: color });
  ctx.addText(slide, { x: x + 22, y: y + 18, w: w - 36, h: 30, text: heading, fontSize: 18, bold: true, color: C.ink });
  ctx.addText(slide, { x: x + 22, y: y + 58, w: w - 36, h: h - 72, text: body, fontSize: 14, color: "#344054" });
}

export function pill(slide, ctx, x, y, w, label, color = C.blue) {
  ctx.addShape(slide, { x, y, w, h: 34, fill: color, line: ctx.line("transparent", 0) });
  ctx.addText(slide, { x: x + 12, y: y + 7, w: w - 24, h: 20, text: label, fontSize: 13, bold: true, color: "#FFFFFF", align: "center" });
}

export function bullet(slide, ctx, x, y, text, color = C.blue, width = 500) {
  ctx.addShape(slide, { x, y: y + 8, w: 9, h: 9, geometry: "ellipse", fill: color, line: ctx.line("transparent", 0) });
  ctx.addText(slide, { x: x + 20, y, w: width, h: 40, text, fontSize: 16, color: C.ink });
}

export function node(slide, ctx, x, y, w, h, label, sub, color = C.blue) {
  ctx.addShape(slide, { x, y, w, h, fill: "#FFFFFF", line: ctx.line(color, 2) });
  ctx.addText(slide, { x: x + 12, y: y + 12, w: w - 24, h: 24, text: label, fontSize: 17, bold: true, color });
  if (sub) ctx.addText(slide, { x: x + 12, y: y + 42, w: w - 24, h: h - 50, text: sub, fontSize: 12, color: C.muted });
}

export function arrow(slide, ctx, x1, y1, x2, y2, color = C.gray) {
  const dx = x2 - x1;
  const dy = y2 - y1;
  const len = Math.sqrt(dx * dx + dy * dy);
  const angle = Math.atan2(dy, dx) * 180 / Math.PI;
  const line = ctx.addShape(slide, { x: x1, y: y1, w: len, h: 2, fill: color });
  line.rotation = angle;
  ctx.addShape(slide, { x: x2 - 8, y: y2 - 5, w: 12, h: 10, geometry: "triangle", fill: color, line: ctx.line("transparent", 0) }).rotation = angle + 90;
}

export function tableHeader(slide, ctx, x, y, widths, labels) {
  let left = x;
  labels.forEach((label, i) => {
    ctx.addShape(slide, { x: left, y, w: widths[i], h: 42, fill: C.dark, line: ctx.line("#FFFFFF", 1) });
    ctx.addText(slide, { x: left + 8, y: y + 11, w: widths[i] - 16, h: 20, text: label, fontSize: 13, bold: true, color: "#FFFFFF" });
    left += widths[i];
  });
}

export function tableRow(slide, ctx, x, y, widths, labels, fill = "#FFFFFF") {
  let left = x;
  labels.forEach((label, i) => {
    ctx.addShape(slide, { x: left, y, w: widths[i], h: 43, fill, line: ctx.line("#E6EAF0", 1) });
    ctx.addText(slide, { x: left + 8, y: y + 10, w: widths[i] - 16, h: 22, text: label, fontSize: 12, color: C.ink });
    left += widths[i];
  });
}


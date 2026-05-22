import { background, title, footer, card, C } from "./common.mjs";

export async function slide05(presentation, ctx) {
  const slide = presentation.slides.add();
  background(slide, ctx);
  title(slide, ctx, "Java Technology Stack", "Course learning applied to a complete web application");
  const items = [
    ["Java 17", "Main programming language", C.blue],
    ["Spring Boot", "Web application framework", C.teal],
    ["Maven", "Build and dependency management", C.yellow],
    ["MySQL 8.4", "Production relational database", C.green],
    ["Spring Data JPA", "Entity and repository data access", C.red],
    ["Spring Security", "Login and role access control", C.gray],
    ["Thymeleaf", "Server-side page templates", C.cyan],
    ["Bootstrap 5", "Responsive UI components", C.blue],
  ];
  items.forEach((it, idx) => {
    const x = idx % 4 === 0 ? 70 : 70 + (idx % 4) * 300;
    const y = idx < 4 ? 170 : 380;
    card(slide, ctx, x, y, 250, 145, it[0], it[1], it[2]);
  });
  footer(slide, ctx, 5);
  return slide;
}


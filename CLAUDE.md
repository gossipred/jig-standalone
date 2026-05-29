# jig-standalone — CLAUDE.md

治具與模具資產管理桌面應用程式。不需要 Server、不需要網路，單機安裝即用。

---

## 常用指令

```bash
# 開發模式執行
mvn javafx:run

# 打包成 fat JAR（跳過測試）
mvn clean package -DskipTests

# 重置資料庫
rm ~/.jig-standalone/data/jigdb.mv.db

# ⚠️ 刪除 Java 原始檔或移除 Maven 依賴後，必須用 clean
mvn clean javafx:run
```

---

## 技術架構

```
UI 層     JavaFX 21（FXML + CSS）
邏輯層    Spring Boot 3.5（DI / Service / Repository）
資料層    H2 Database file mode → ~/.jig-standalone/data/jigdb.mv.db
附件存放  ~/.jig-standalone/uploads/jigs/
更新機制  GitHub raw version.json + GitHub Releases
```

**版本：** 1.1.0  
**GitHub：** https://github.com/gossipred/jig-standalone  
**JAR 大小：** ~60MB（fat JAR，含所有依賴）

---

## 專案結構

```
src/main/java/com/jj/jig/
  JigStandaloneApp.java       ← 入口點（Spring + JavaFX 整合）
  JigFxApp.java               ← JavaFX Application 類別
  JigSpringApplication.java   ← Spring Boot 啟動類別

  auth/          UserSession（singleton）、AuthService、DbEnumMigration
  backup/        BackupRestoreService、AutoBackupService、AutoBackupSettings
  importlog/     ImportLog、ImportLogRepository、ImportLogStatus
  jig/           Jig、JigFile、JigService、JigCsvImportService、JigCsvExportService
                 JigFileStorageService、JigStatus、JigNumber、JigForm
  log/           JigLog、JigLogActionType、JigLogRepository
  security/      DatabaseUserDetailsService、SecurityConfig
  uninstall/     UninstallService
  update/        UpdateService、UpdateCheckResult
  user/          User、UserRole（ADMIN/SUPERVISOR/ENGINEER/VIEWER）
                 UserService、UserLog
  ui/
    SpringFxmlLoader           ← FXMLLoader + Spring context 整合
    StageHolder                ← 主視窗單例
    admin/   AdminManagementController
    jig/     JigListController、JigDetailController、JigFormDialogController
             JigImportDialogController、JigStatusDialogController
    log/     LogViewController
    login/   LoginController
    main/    MainController
    stats/   StatsViewController
    uninstall/ UninstallDialogController

src/main/resources/
  application.properties
  fxml/          login.fxml、main.fxml、jig-list.fxml、jig-detail.fxml
                 jig-form-dialog.fxml、jig-status-dialog.fxml
                 jig-import-dialog.fxml、log-view.fxml、stats-view.fxml
                 admin-management.fxml、uninstall-dialog.fxml
  css/           app.css

version.json     ← GitHub 版本檢查用（repo 根目錄）
docs/PLAN.md     ← 功能規劃書
docs/MILESTONE.md
```

---

## 重要設計規則

### Spring + JavaFX 整合
- `LoginController`、`MainController` 都是 `@Scope("prototype")`
- FXML controller 透過 `SpringFxmlLoader`（`context::getBean`）建立，不由 FXMLLoader 直接 new
- `setupSelectionListener()` lambda 首行加 null guard，防止 logout 後 NPE

### 非同步規則
- `Desktop.open()`（開啟附件）必須在獨立 Thread 執行，避免 macOS AppKit 衝突
- Dialog Stage 顯示動態內容後需呼叫 `sizeToScene()`
- Main view 以 `MainController.switchView()` 動態 load FXML（三頁 nav）

### 資料庫
- H2 `ddl-auto=update`（只加欄位，不刪）
- `DbEnumMigration` @Order(0) 在啟動時修復 ACTION_TYPE CHECK constraint
- 同一時間只能一個 app 實例開啟（H2 file lock）
- `JigLogActionType` 值：`CREATE, UPDATE, DELETE, STATUS_CHANGE, DUE_DATE_CHANGE, NOTE, FILE_UPLOAD, FILE_REPLACE, FILE_DELETE`

### 術語對照
| 英文（程式碼） | UI 顯示 |
|---|---|
| Jig / jig | JT / 治模具 |
| JigNo | JT No. |

---

## 列印功能實作規則（重要）

JavaFX + Spring Boot 環境下，以下兩種列印方式都**不能用**：

```java
// ❌ FXMLLoader.getClassLoader() = null → NPE
job.showPrintDialog(stage);

// ❌ JavaFX 佔用 AppKit → AWT HeadlessException
java.awt.print.PrinterJob.getPrinterJob().printDialog();
```

**正確作法：** 自建 `Dialog<Printer>` + `ComboBox<Printer>` 讓使用者選印表機，再直接 `printPage()`：

```java
// ✅ 正確模式
List<Printer> printers = new ArrayList<>(Printer.getAllPrinters());
// 自建 Dialog<Printer> 顯示選單
Printer chosen = showPrinterChooser(printers);
PrinterJob job = PrinterJob.createPrinterJob(chosen);
WritableImage img = node.snapshot(new SnapshotParameters(), null);
ImageView iv = new ImageView(img);
// 縮放到符合頁面大小
job.printPage(layout, iv);
job.endJob();
```

---

## 版本更新 SOP（Admin → Update）

1. 備份（Admin → Backup）
2. 在 Update 頁面確認新版本
3. 下載新 JAR（開啟 GitHub Releases）
4. 關閉 app
5. 替換 JAR 檔
6. 重新啟動

版本檢查 URL：`https://raw.githubusercontent.com/gossipred/jig-standalone/main/version.json`

---

## 踩坑紀錄

### 刪除 Java 原始檔後必須 `mvn clean`
`mvn compile` 不會刪除已刪原始碼對應的 `.class`。若同時移除 Maven 依賴，殭屍 `.class` 會讓 Spring 掃描時崩潰（NoClassDefFoundError），導致部分 FXML view 無法載入。**解法：刪檔或移除依賴後一律跑 `mvn clean compile` 或 `mvn clean javafx:run`。**

### OpenPDF 版本問題
OpenPDF **2.x** 在 macOS 上用 TTC 字型（STHeiti / Hiragino）搭配 `IDENTITY_H` 編碼時，cmap 解析失敗，PDF 文字全空白。若未來要加 PDF 功能，改用 OpenPDF **1.3.x**。

### H2 lock
app 已在執行時，`mvn javafx:run` 啟動第二個實例會因 H2 file lock 報錯。關掉舊 app 再啟動。

---

## 預設帳號

| 帳號 | 密碼 | 角色 |
|------|------|------|
| admin | 123456 | ADMIN |

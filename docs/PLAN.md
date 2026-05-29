# Jig & Toolings Management System — Standalone Version
# 專案計畫書

**版本：** 1.0.0  
**更新：** 2026-05-29  
**架構：** JavaFX 21 + Spring Boot 3.5 + H2 (file mode)

---

## 專案目標

提供一個**不需要 Server、不需要網路、單機安裝即用**的治具與模具管理系統。
資料儲存在本機 H2 資料庫，支援備份/還原，並可透過 GitHub 版本更新。

---

## 技術架構

```
UI 層          JavaFX 21 (FXML + CSS)
邏輯層         Spring Boot 3.5 (DI / Service / Repository)
資料層         H2 Database (file mode) at ~/.jig-standalone/data/
檔案儲存       ~/.jig-standalone/uploads/jigs/
跨平台         macOS / Windows (同一份 Java 程式碼)
更新機制       GitHub raw file (version.json) + GitHub Releases
```

---

## 功能規劃

### ✅ Phase 1 — 基礎框架與登入（完成）

- [x] Spring Boot + JavaFX 整合架構
- [x] H2 file-mode 資料庫，資料存於 `~/.jig-standalone/`
- [x] 登入畫面（左右兩欄，雙語設計）
- [x] 密碼欄位眼睛切換顯示/隱藏
- [x] Spring Security 帳號驗證
- [x] 角色系統：ADMIN / SUPERVISOR / ENGINEER / OPERATOR
- [x] 登入後切換至主畫面

### ✅ Phase 2 — Admin Management（完成）

- [x] Admin Management 獨立視窗（TabPane 四分頁）
- [x] **Users 帳號管理**
  - [x] 帳號列表（TableView）
  - [x] 新增帳號（username / password / role / enabled）
  - [x] 編輯帳號（role / enabled）
  - [x] 重設密碼
  - [x] 刪除帳號（防止刪除最後一個 admin）
  - [x] 操作紀錄（user_logs）
- [x] **Update 版本更新**
  - [x] 讀取 GitHub version.json 比對版本
  - [x] 顯示更新通知與說明
  - [x] 開啟 GitHub Release 下載頁
- [x] **Backup & Restore 備份還原**
  - [x] 手動備份（H2 BACKUP SQL + uploads 資料夾）
  - [x] 自動備份設定（daily / weekly / monthly）
  - [x] 備份清單瀏覽與選擇
  - [x] 排程還原（下次啟動前執行，避免 DB 鎖定問題）
- [x] **Uninstall 卸載**
  - [x] Admin 二次驗證確認對話框
  - [x] 刪除 `~/.jig-standalone/` 目錄
  - [x] 跨平台（macOS / Windows）

### ✅ Phase 3 — Jig 治具管理（完成）

- [x] 治具列表（搜尋、狀態篩選、狀態 badge）
- [x] 新增治具（Form Dialog，含自動產生 Jig No.）
- [x] 編輯治具（Form Dialog）
- [x] 刪除治具（ADMIN 限定，含子資料 jig_files / jig_logs 清除）
- [x] 到期日顏色警示（逾期/30天內/正常）
- [x] 治具詳細資料頁（info grid + Files 表格 + Change Logs 表格）
- [x] 狀態快速切換 Quick Status Change（含備註，狀態不變時存 NOTE log）
- [x] 圖紙附件上傳、開啟（背景 Thread）、刪除
- [x] 匯入 CSV（預覽對話框、New/Update/Error badge、下載範本）
- [x] 匯出 CSV（全部或選取，FileChooser 存檔）

### 🔄 Phase 4 — 報表與日誌（進行中）

- [x] Nav Bar — JTs / Logs / Stats 三頁切換（main.fxml + MainController 動態 load）
- [x] 跨 JT 異動紀錄查詢（日期範圍、操作者、動作類型，預設最近 7 天）
- [x] Log 頁 Export CSV
- [x] 狀態統計圖表（JavaFX PieChart + 各狀態件數/百分比卡片）
- [x] 備份資料夾加入 HHmm 時間戳記
- [x] UpdateService URL 修正（新 repo：gossipred/jig-standalone）
- [ ] PDF 報表匯出（規劃中，不做 Excel，已有 CSV）
- [ ] 列印報表

### 📦 Phase 5 — 打包與發布

- [ ] macOS `.dmg` / `.pkg`（jpackage）
- [ ] Windows `.msi` / `.exe`（jpackage + WiX）
- [ ] 自動化 build pipeline

---

## 資料庫 Schema 概覽

```
users          帳號資料（username, password, role, enabled）
user_logs      帳號操作紀錄
jigs           治具主資料
jig_files      圖紙附件（外鍵 jig_id → jigs）
jig_logs       治具異動紀錄（外鍵 jig_id → jigs，action_type: CREATE/UPDATE/DELETE/STATUS_CHANGE/DUE_DATE_CHANGE/NOTE/FILE_UPLOAD/FILE_REPLACE/FILE_DELETE）
```

---

## 重要路徑

| 項目 | 路徑 |
|------|------|
| 資料庫 | `~/.jig-standalone/data/jigdb.mv.db` |
| 上傳附件 | `~/.jig-standalone/uploads/jigs/` |
| 自動備份設定 | `~/.jig-standalone/auto-backup.properties` |
| 待還原標記 | `{系統 temp}/jig-pending-restore.txt` |
| GitHub 版本檔 | `standalone/version.json` (in repo) |

---

## 已知限制

- H2 嵌入式資料庫：同一時間只能一個 app 實例開啟
- 附件儲存在本機：多台電腦使用需手動同步或使用共用磁碟路徑
- 更新為手動下載：不支援自動覆蓋安裝（需使用者手動替換 JAR）

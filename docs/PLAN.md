# Jig & Toolings Management System — Standalone Version
# 專案計畫書

**版本：** 1.0.0  
**更新：** 2026-05-28  
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

### 🔄 Phase 3 — Jig 治具管理（進行中）

- [ ] 治具列表（搜尋、篩選、排序）
- [ ] 治具詳細資料頁
- [ ] 新增 / 編輯治具
- [ ] 狀態管理（使用中 / 維修中 / 報廢）
- [ ] 圖紙附件上傳與預覽
- [ ] 到期日提醒
- [ ] 匯入 CSV

### 📋 Phase 4 — 報表與日誌

- [ ] 異動紀錄查詢
- [ ] 狀態統計圖表
- [ ] CSV 匯出
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
jigs           治具主資料（Phase 3）
jig_files      圖紙附件（Phase 3）
import_logs    CSV 匯入紀錄（Phase 3）
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

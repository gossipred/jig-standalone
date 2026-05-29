# Milestone 進度追蹤

**更新：** 2026-05-29

---

## 目前狀態：Phase 3 完成，Phase 4 待開始

```
Phase 1  [██████████] 100%  ✅ 完成
Phase 2  [██████████] 100%  ✅ 完成
Phase 3  [██████████] 100%  ✅ 完成
Phase 4  [░░░░░░░░░░]   0%  📋 規劃中
Phase 5  [░░░░░░░░░░]   0%  📋 規劃中
```

---

## Phase 1 — 基礎框架與登入 ✅

| 項目 | 狀態 | 完成日期 |
|------|------|----------|
| Spring Boot + JavaFX 整合 | ✅ | 2026-05-28 |
| H2 file-mode 資料庫 | ✅ | 2026-05-28 |
| 登入畫面雙語設計 | ✅ | 2026-05-28 |
| 密碼眼睛切換 | ✅ | 2026-05-28 |
| Spring Security 驗證 | ✅ | 2026-05-28 |
| 角色系統 (ADMIN/SUPERVISOR/ENGINEER/OPERATOR) | ✅ | 2026-05-28 |

---

## Phase 2 — Admin Management ✅

| 項目 | 狀態 | 完成日期 |
|------|------|----------|
| Admin Management 視窗 (TabPane) | ✅ | 2026-05-28 |
| **Users Tab** | | |
| — 帳號列表 TableView | ✅ | 2026-05-28 |
| — 新增帳號 | ✅ | 2026-05-28 |
| — 編輯帳號 (role / enabled) | ✅ | 2026-05-28 |
| — 重設密碼 | ✅ | 2026-05-28 |
| — 刪除帳號（防刪最後 admin） | ✅ | 2026-05-28 |
| — 操作紀錄顯示 | ✅ | 2026-05-28 |
| **Update Tab** | | |
| — GitHub version.json 比對 | ✅ | 2026-05-28 |
| — 更新通知與說明 | ✅ | 2026-05-28 |
| — 開啟 GitHub Release 頁面 | ✅ | 2026-05-28 |
| **Backup & Restore Tab** | | |
| — 手動備份 (H2 BACKUP + uploads) | ✅ | 2026-05-28 |
| — 自動備份設定 (daily/weekly/monthly) | ✅ | 2026-05-28 |
| — 備份清單瀏覽 | ✅ | 2026-05-28 |
| — 排程還原（下次啟動前執行） | ✅ | 2026-05-28 |
| **Uninstall Tab** | | |
| — Admin 二次驗證對話框 | ✅ | 2026-05-28 |
| — 刪除 ~/.jig-standalone/ | ✅ | 2026-05-28 |

---

## Phase 3 — Jig 治具管理 ✅ 完成

| 項目 | 狀態 | 完成日期 |
|------|------|----------|
| 治具列表頁（搜尋/篩選/狀態 badge） | ✅ | 2026-05-29 |
| 新增 / 編輯治具（完整 form dialog） | ✅ | 2026-05-29 |
| 刪除治具（ADMIN 限定，含子資料清除） | ✅ | 2026-05-29 |
| 到期日顏色警示（逾期/30天內） | ✅ | 2026-05-29 |
| 治具詳細資料頁（info grid + 異動紀錄） | ✅ | 2026-05-29 |
| 狀態快速切換（Change Status + Note） | ✅ | 2026-05-29 |
| 圖紙附件上傳與預覽（FileChooser + Desktop.open） | ✅ | 2026-05-29 |
| 匯入 CSV（預覽 + 批次匯入 + 下載範本） | ✅ | 2026-05-29 |
| 匯出 CSV（Export all / selected jigs） | ✅ | 2026-05-29 |

**Phase 3 修復 Bug（共 9 個，2026-05-29 全數修完）**

| Bug | 修正說明 |
|-----|---------|
| 附件刪除失敗 | 路徑解析改為 `uploadDir.resolve(storedPath)` |
| Detail view 刪檔後不刷新 | 加 try-catch 確保 `loadFiles()` 執行 |
| Logout NPE cascade | Selection listener 加 null guard |
| LoginController 二次 load 失敗 | 改 `@Scope("prototype")` |
| Upload log H2 constraint 錯誤 | DbEnumMigration 修復 CHECK constraint |
| File Open macOS 衝突 | `Desktop.open()` 移至獨立 Thread |
| Import CSV 底部按鈕消失 | `loadPreview()` 末尾加 `sizeToScene()` |
| Delete Jig 無反應 | 先刪 jig_files + jig_logs 再刪 jig |
| Change Status note 不見 | 狀態不變時若有 note 改存 `NOTE` log |

---

## Phase 4 — 報表與日誌 🔄 進行中

| 項目 | 狀態 | 完成日期 |
|------|------|----------|
| Nav Bar（JTs / Logs / Stats 三頁切換） | ✅ | 2026-05-29 |
| 跨 JT 異動紀錄查詢（日期/操作者/動作類型） | ✅ | 2026-05-29 |
| 無篩選條件時預設顯示最近 7 天 | ✅ | 2026-05-29 |
| Log 頁 Export CSV | ✅ | 2026-05-29 |
| 狀態統計圖表（PieChart + 各狀態件數/百分比） | ✅ | 2026-05-29 |
| 備份資料夾加入時間戳記 | ✅ | 2026-05-29 |
| UpdateService URL 修正至新 repo | ✅ | 2026-05-29 |
| PDF 報表匯出 | 🔄 | 規劃中 |
| 列印報表 | 📋 | — |

---

## 已知 Bug / 待修

| 編號 | 描述 | 優先度 |
|------|------|--------|
| - | 目前無已知 Bug | - |

---

## 下一步行動

1. 開始 Phase 4：跨 Jig 日誌查詢頁面
2. 在 GitHub repo 建立 `standalone/version.json`
3. 建立第一個 GitHub Release `standalone-v1.0.0`

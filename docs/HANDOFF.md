# Handoff Guide — Jig & Toolings Management System (Standalone)
# 安裝移交說明文件

**版本：** 1.0.0  
**更新：** 2026-05-28  
**適用：** 系統管理員 / IT 人員

---

## 系統需求

| 項目 | 最低要求 |
|------|----------|
| 作業系統 | macOS 12+ 或 Windows 10/11 (64-bit) |
| Java | JDK 17 或以上（需安裝） |
| 磁碟空間 | 安裝檔 ~50MB，資料庫隨資料成長 |
| 記憶體 | 512 MB RAM（建議 1GB） |
| 網路 | 不需要（僅版本更新檢查時需要） |

---

## 安裝步驟

### macOS

1. 確認已安裝 Java 17+
   ```bash
   java -version
   ```
2. 下載 `jig-standalone-x.x.x.jar`（從 GitHub Releases）
3. 執行：
   ```bash
   java -jar jig-standalone-x.x.x.jar
   ```
4. 或建立一個 shell script `run-jig.sh`：
   ```bash
   #!/bin/bash
   java -jar /Applications/jig-standalone-1.0.0.jar
   ```

### Windows

1. 確認已安裝 Java 17+（建議 [Adoptium](https://adoptium.net/)）
2. 下載 `jig-standalone-x.x.x.jar`
3. 建立 `run-jig.bat`：
   ```bat
   @echo off
   java -jar jig-standalone-1.0.0.jar
   pause
   ```
4. 雙擊執行 `run-jig.bat`

---

## 首次啟動與初始帳號

首次啟動時系統會自動建立預設管理者帳號：

| 帳號 | 密碼 |
|------|------|
| `admin` | `admin123` |

> ⚠️ **請立即修改預設密碼！**  
> 登入後 → Admin Management → Users → 選擇 admin → Reset Password

---

## 資料儲存位置

系統所有資料存放於使用者家目錄：

```
macOS：  /Users/{username}/.jig-standalone/
Windows： C:\Users\{username}\.jig-standalone\

目錄結構：
~/.jig-standalone/
├── data/
│   └── jigdb.mv.db          ← H2 資料庫主檔
├── uploads/
│   └── jigs/                ← 圖紙附件
└── auto-backup.properties   ← 自動備份設定
```

> 注意：`.jig-standalone` 是隱藏資料夾，macOS 需按 `Cmd+Shift+.` 顯示。

---

## 備份與還原

### 手動備份

1. 登入系統（需 ADMIN 角色）
2. 主畫面 → Admin Management → **Backup & Restore** 頁籤
3. 選擇備份目的地資料夾
4. 點「Backup Now」
5. 系統會建立：`{目的地}/jig-backup-YYYYMMDD/`
   - `jigdb-backup.zip`（資料庫）
   - `uploads/`（附件）

### 自動備份設定

同上頁面 → Auto-Backup Settings：
- 勾選「Enable auto-backup」
- 選擇頻率（每天 / 每週 / 每月）
- 選擇備份目的地
- 點「Save Settings」

### 還原資料

1. 主畫面 → Admin Management → **Backup & Restore**
2. 瀏覽選擇存放備份的資料夾
3. 從清單選擇要還原的備份版本
4. 點「Schedule Restore」→ 確認
5. App 結束，**下次啟動時自動還原**

---

## 卸載（移除所有資料）

> ⚠️ **此操作無法復原，請先備份！**

1. 登入系統（需 ADMIN 角色）
2. Admin Management → **Uninstall** 頁籤
3. 點「Uninstall Application」
4. 在確認對話框輸入管理者帳號密碼
5. 點「Confirm Uninstall」
6. 系統自動刪除 `~/.jig-standalone/` 並結束

刪除 JAR 檔本身：手動刪除即完成卸載。

---

## 軟體更新

### 檢查更新

1. 主畫面 → Admin Management → **Update** 頁籤
2. 點「Check for Update」
3. 如有新版本會顯示版本號與說明
4. 點「Open Download Page」→ 瀏覽器開啟 GitHub Releases 頁面

### 更新步驟

1. 在 GitHub Releases 頁面下載新版 JAR
2. 停止目前的 App
3. 用新版 JAR 取代舊版（資料不受影響）
4. 重新啟動

> 資料庫 schema 會在啟動時自動升級（Spring JPA `ddl-auto=update`）

---

## 角色權限說明

| 角色 | 說明 | 可使用功能 |
|------|------|------------|
| ADMIN | 系統管理員 | 全部功能 + Admin Management |
| SUPERVISOR | 主管 | 治具管理、報表查詢 |
| ENGINEER | 工程師 | 治具新增/編輯 |
| OPERATOR | 操作員 | 查詢、狀態更新 |

---

## 常見問題

**Q: App 無法啟動，出現「Database may be already in use」**  
A: 有另一個 App 實例正在運行。關閉其他視窗或重新開機後再試。

**Q: 忘記 admin 密碼怎麼辦？**  
A: 直接刪除 `~/.jig-standalone/data/jigdb.mv.db`（資料會清空），重新啟動會產生預設帳號 `admin / admin123`。或從備份還原。

**Q: 如何在多台電腦共用資料？**  
A: 將 `~/.jig-standalone/` 目錄指向共用磁碟（NAS），或定期用備份同步。目前版本不支援即時多人同時存取。

**Q: 備份檔可以跨電腦還原嗎？**  
A: 可以，H2 的 `.mv.db` 格式可跨平台。

---

## 技術聯絡

- **GitHub:** https://github.com/gossipred/jig-and-toolings-management-system
- **Issues:** https://github.com/gossipred/jig-and-toolings-management-system/issues

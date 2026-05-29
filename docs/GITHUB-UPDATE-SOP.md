# GitHub Update SOP — Jig & Toolings Management System
# GitHub 版本更新標準作業程序

**Repository:** https://github.com/gossipred/jig-and-toolings-management-system  
**Last Updated:** 2026-05-29

---

## 概述 Overview

App 透過讀取 GitHub 上的 `version.json` 來判斷是否有新版本。
Admin 開啟 **Admin Management → Update** 頁面，點「Check for Update」即可檢查。

```
App → 讀取 GitHub raw file → 比對版本號 → 有新版 → 顯示通知 → 開啟下載頁
```

---

## GitHub Repo 版本檔案位置

| 版本 | 路徑 | Raw URL |
|------|------|---------|
| Standalone | `standalone/version.json` | `https://raw.githubusercontent.com/gossipred/jig-and-toolings-management-system/main/standalone/version.json` |
| Web | `web/version.json` | `https://raw.githubusercontent.com/gossipred/jig-and-toolings-management-system/main/web/version.json` |

---

## version.json 格式

```json
{
  "version": "1.0.0",
  "releaseDate": "2026-05-28",
  "downloadUrl": "https://github.com/gossipred/jig-and-toolings-management-system/releases/tag/standalone-v1.0.0",
  "releaseNotes": "Bug fixes and new features.",
  "releaseNotesZh": "修復問題與新功能。"
}
```

| 欄位 | 說明 |
|------|------|
| `version` | 語意版號，格式 `主.次.修` (e.g. `1.2.0`) |
| `releaseDate` | 發布日期，格式 `YYYY-MM-DD` |
| `downloadUrl` | GitHub Release 頁面網址（不是直接下載連結） |
| `releaseNotes` | 英文更新說明 |
| `releaseNotesZh` | 中文更新說明 |

---

## 發布新版本 SOP（每次發版照這個流程）

### Step 1 — 更新程式碼版本號

編輯 `src/main/resources/application.properties`：

```properties
app.version=1.1.0   ← 改這裡
```

### Step 2 — Build 打包

```bash
cd /Users/chienchungwu/Documents/jig-standalone
mvn clean package -DskipTests
```

產出檔案：`target/jig-standalone-1.0.0.jar`

### Step 3 — 在 GitHub 建立 Release

1. 進入 https://github.com/gossipred/jig-and-toolings-management-system
2. 右側點 **「Releases」→「Create a new release」**
3. 填寫：
   - **Tag version:** `standalone-v1.1.0`（standalone 用）或 `web-v1.1.0`（web 用）
   - **Release title:** `Standalone v1.1.0` 或 `Web v1.1.0`
   - **Description:** 更新說明（中英文）
4. 上傳檔案：把 `target/jig-standalone-1.1.0.jar` 拖曳上傳
5. 點 **「Publish release」**
6. 複製這個 Release 頁面的網址，例如：
   `https://github.com/gossipred/jig-and-toolings-management-system/releases/tag/standalone-v1.1.0`

### Step 4 — 更新 GitHub 上的 version.json

進入 GitHub repo → `standalone/version.json` → 點鉛筆圖示編輯：

```json
{
  "version": "1.1.0",
  "releaseDate": "2026-06-01",
  "downloadUrl": "https://github.com/gossipred/jig-and-toolings-management-system/releases/tag/standalone-v1.1.0",
  "releaseNotes": "New features: ...",
  "releaseNotesZh": "新功能：..."
}
```

Commit message 建議：`chore: bump standalone version to 1.1.0`

### Step 5 — 驗證

1. 開啟 App → 登入 → Admin Management → Update 頁面
2. 點「Check for Update」
3. 確認顯示新版本通知
4. 點「Open Download Page」確認能開啟正確的 Release 頁面

---

## Web 版本更新（同上，Tag 和路徑不同）

- Tag 格式：`web-v1.1.0`
- 更新檔案：`web/version.json`
- 其餘步驟相同

---

## Standalone Tag 命名規則

```
standalone-v{major}.{minor}.{patch}
web-v{major}.{minor}.{patch}

範例：
standalone-v1.0.0  ← 初始版本
standalone-v1.1.0  ← 小功能新增
standalone-v1.1.1  ← Bug fix
standalone-v2.0.0  ← 重大版本更新
```

---

## 版本號規則（語意版控 Semantic Versioning）

| 類型 | 何時遞增 | 範例 |
|------|----------|------|
| Major（主） | 重大架構變更、不相容的 DB schema 更新 | `1.x.x → 2.0.0` |
| Minor（次） | 新功能、新頁面、不影響舊資料 | `1.0.x → 1.1.0` |
| Patch（修） | Bug fix、UI 微調、效能改善 | `1.1.0 → 1.1.1` |

---

*文件維護：每次發版後同步更新本文件的 Last Updated 日期。*

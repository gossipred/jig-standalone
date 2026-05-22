# V6.2 計劃：滑鼠生產治具管理與維護紀錄系統

## Summary

建立一套 Spring Boot + MySQL 的 Web 治具管理系統，將目前 Notion Jig 管理資料轉成正式資料庫系統。第一版以「可展示、可查詢、可控權限、可追蹤異動」為目標，並預留未來 Notion API 自動匯入能力。

目前環境已確認：

- Maven 3.9.16 已安裝
- MySQL 8.4.9 已安裝
- 工作目錄目前尚無專案檔案，適合從零建立 Spring Boot 專案

## Key Changes

- 技術架構：
  - Java 17 相容設定
  - Spring Boot 3.x
  - Maven
  - MySQL 8.4 LTS
  - Spring Data JPA
  - Spring Security Form Login
  - Thymeleaf
  - Bootstrap 5

- 核心資料表：
  - `users`：登入帳號、密碼、角色
  - `jigs`：治具主資料，依 Notion 欄位設計
  - `jig_logs`：狀態、Due Date、維護與異動紀錄
  - `import_logs`：預留 Notion 匯入紀錄

- 角色權限：
  - `ADMIN`：新增、修改治具、更新狀態、修改 Due Date、匯出 CSV
  - `ENGINEER`：查詢、更新狀態、修改 Due Date、新增維護紀錄、匯出 CSV
  - `OPERATOR`：查詢、查看紀錄、匯出 CSV

- Jig No. 規則：
  - 支援 `AM-ME-001`
  - 支援 `AM-ME-001-02`
  - `AM-ME-001-01` 自動轉成 `AM-ME-001`
  - 驗證規則：`^[A-Z]{2}-[A-Z]{2}-[0-9]{3}(-[0-9]{2})?$`
  - 資料庫同時保存 `jig_no`、`jig_base_no`、`set_no`

- Due Date 追蹤：
  - `jigs` 保存目前最新 Due Date
  - `jig_logs` 保存修改歷史
  - 每次修改記錄修改者、修改時間、舊日期、新日期、原因

- Notion 自動匯入準備：
  - `jigs` 預留 `notion_page_id`、`notion_last_edited`、`last_imported_at`
  - 新增 `import_logs`
  - 預留設定：
    - `notion.api-token`
    - `notion.database-id`
    - `notion.api-version`

## Implementation Plan

1. Phase 1：Database Setup
   - 建立 database：`web_fixture_management`
   - 建立 `users`
   - 建立 `jigs`
   - 建立 `jig_logs`
   - 建立 `import_logs`
   - 建立 index、unique constraint、foreign key
   - 建立三組預設帳號：`admin`、`engineer`、`operator`
   - 建立 Notion 畫面中的範例治具資料

2. Phase 2：Spring Boot Project Setup
   - 建立 Maven Spring Boot 專案
   - 設定 `pom.xml`
   - 設定 `application.properties`
   - 建立 Entity：
     - `User`
     - `Jig`
     - `JigLog`
     - `ImportLog`
   - 建立 Repository：
     - `UserRepository`
     - `JigRepository`
     - `JigLogRepository`
     - `ImportLogRepository`

3. Phase 3：Login and RBAC
   - 使用 Spring Security Form Login
   - 設定登入、登出
   - 使用 BCrypt 儲存密碼
   - 後端限制權限：
     - `ADMIN` 才能新增 / 修改治具
     - `ADMIN`、`ENGINEER` 才能更新狀態與 Due Date
   - Thymeleaf 畫面依角色隱藏或顯示按鈕

4. Phase 4：Jig Management
   - 治具列表
   - 單一搜尋框，搜尋：
     - `model_name`
     - `jig_no`
     - `customer`
   - 新增治具
   - 修改治具
   - Jig No. 格式驗證與標準化
   - 顯示欄位依 Notion 模板設計：
     - Classification
     - Model Name
     - Jig Name
     - Customer
     - Jig No.
     - Ass'y Line
     - Jig Picture URL
     - Quantity
     - MRO No.
     - PR No.
     - Status
     - DRI
     - Start Date
     - Due Date
     - Due Date Updated At
     - Due Date Updated By

5. Phase 5：Status, Due Date and Logs
   - 狀態支援：
     - `Normal`
     - `Repair`
     - `Scrap`
     - `Hold`
   - 更新狀態時寫入 `jig_logs`
   - 修改 Due Date 時寫入：
     - `old_due_date`
     - `new_due_date`
     - `user_id`
     - `note`
     - `created_at`
   - 支援查看單一 Jig 的異動紀錄

6. Phase 6：CSV Export
   - 匯出全部治具
   - 匯出搜尋後結果
   - CSV 欄位使用 `jigs` 主要欄位，不匯出密碼、系統內部 token 或敏感設定

7. Phase 7：Notion Import Preparation
   - 第一版只完成資料庫與設定預留
   - 第二版加入手動觸發 Notion import
   - 第三版加入排程自動同步

## Database Design

### `users`

```text
id
username
password
role
enabled
created_at
```

### `jigs`

```text
id
classification
model_name
jig_name
customer
jig_no
jig_base_no
set_no
assembly_line
jig_picture_url
quantity
mro_no
pr_no
status
dri
start_date
due_date
due_date_updated_at
due_date_updated_by
notion_page_id
notion_last_edited
last_imported_at
created_at
updated_at
```

### `jig_logs`

```text
id
jig_id
user_id
action_type
old_status
new_status
old_due_date
new_due_date
note
created_at
```

### `import_logs`

```text
id
source_type
started_at
finished_at
status
total_count
created_count
updated_count
skipped_count
failed_count
message
```

## Test Plan

- 登入測試：
  - `admin / 123456`
  - `engineer / 123456`
  - `operator / 123456`

- 權限測試：
  - `OPERATOR` 看不到新增、修改、更新狀態、修改 Due Date 按鈕
  - `OPERATOR` 直接輸入管理網址也不能執行
  - `ENGINEER` 可更新狀態與 Due Date，但不能新增治具
  - `ADMIN` 可執行全部管理功能

- Jig No. 測試：
  - `AM-ME-001` 可新增
  - `AM-ME-001-01` 自動轉 `AM-ME-001`
  - `AM-ME-001-02` 可新增
  - 重複 Jig No. 不可新增
  - 錯誤格式不可新增

- Due Date 測試：
  - 修改 Due Date 後，`jigs` 更新最新日期
  - `jig_logs` 留下舊日期、新日期、修改者、修改時間、原因

- 搜尋測試：
  - 依 `Model Name` 搜尋
  - 依 `Jig No.` 搜尋
  - 依 `Customer` 搜尋

- CSV 測試：
  - 可匯出全部資料
  - 可匯出搜尋結果
  - 匯出內容不包含密碼或敏感資訊

## Assumptions

- `Ass'y Line` 視為組裝線別，存入 `assembly_line`
- `Q’y` 視為數量，存入 `quantity`
- `MRO NO.` 與 `PR NO.` 第一版先當文字欄位
- `Jig Picture` 第一版只存 URL 或檔案路徑，不做圖片上傳
- Notion API 自動匯入第一版只做資料表與設定預留，不實作同步流程
- 密碼使用 BCrypt，資料庫不保存明碼密碼
- 第一版不做 Dashboard、圖表、列印格式、使用者管理與圖片上傳

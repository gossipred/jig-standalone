# Jig & Toolings Management System

治具及模具管理系統。

滑鼠生產治具、模具管理與維護紀錄系統。

## 專案資料位置

所有與本專案相關的計劃、進度、資料庫檔案與程式碼，都集中放在這個資料夾：

```text
/Users/chienchungwu/Documents/jig management system
```

## 資料夾說明

```text
docs/       專案計劃、審查紀錄、設計文件
progress/   開發進度、待辦事項、決策紀錄
data/       資料庫 schema、測試資料、匯入資料
src/        Spring Boot 程式碼
```

## 目前狀態

- 已收納原始計劃檔：`docs/PLAN-jig.md`
- 已建立計劃審查：`docs/PLAN-review.md`
- 已建立進度追蹤：`progress/STATUS.md`
- 已建立變更紀錄：`progress/CHANGELOG.md`
- 已建立決策紀錄：`progress/DECISIONS.md`
- 已將系統顯示名稱調整為 `Jig & Toolings Management System / 治具及模具管理系統`
- 已建立 Phase 1 資料庫草稿：
  - `data/schema.sql`
  - `data/seed.sql`
- 已建立 Spring Boot Maven 骨架
- 已建立 Entity、Repository、基本登入設定、首頁與登入頁
- 已建立治具列表與搜尋頁
- 已建立新增與修改治具功能
- 已建立 Jig No. 格式驗證與標準化
- 已建立 Jig No. 半自動給號：預設 `AM-ME`，可調整前綴並取得下一個流水號
- 已將 DRI / 負責人改為登入帳號自動帶入，不需手動填寫
- 已重新定義 Status：On Process 在線使用中、Normal 正常存放、Scrap 進行報廢、Hold 保留、Repair 維修中、Maintain 保養
- 已在 Jig List 最前面加入 Classification / 產品類別欄位，方便區分 Jig 與 Tooling
- 已在 Jig List 加入 Ass'y Line / 組裝線別欄位，放在 Model 後面
- 已擴充 Jig List Keyword 搜尋：可查 Classification, Jig No., Model, Ass'y Line, Jig Name, Customer, Status, Owner
- 已優化 Login / Home 視覺：加入工業圖紙風格背景圖
- 已將主要頁面背景統一套用淡工業圖紙風格
- 已修復按鈕 hover 時中文輔助文字顏色不清楚的問題
- 已將 Home 的 Phase 1~3 開發進度改成使用者操作說明與角色說明
- 已建立治具圖片/圖紙檔案上傳與下載，檔案預設存到 `uploads/jigs/`
- 每個 Jig 初步限制最多 5 個上傳檔案
- 已建立 Status / Due Date 更新與異動紀錄
- 已修正 Edit Jig 內變更 Status / Due Date 時，會記錄 Old / New 異動值
- 已建立 CSV 匯出功能
- 已建立 MySQL 實機資料庫並完成登入測試
- 已建立 ADMIN 使用者管理頁
- 已建立個人 Change Password 功能：使用者可自行修改密碼，但不可修改帳號
- 已建立帳號異動紀錄：admin 建帳號/改角色/停用啟用/重設密碼、個人改密碼都會寫入 `user_logs`
- 已在 User Management 顯示 Account Change Logs
- 已建立角色權限說明：
  - `ADMIN`：使用者管理與報廢核准
  - `SUPERVISOR`：可跨使用者修改與刪除治具
  - `ENGINEER`：可新增/修改自己建立的治具內容，但不能刪除或報廢
  - `OPERATOR`：唯讀查詢、下載與匯出
- 已建立角色權限自動測試
- 已驗證 `ENGINEER` 可修改自己建立的 Jig，`OPERATOR` 無法修改 Jig
- 已修正 Jig Edit 日期欄位，修改時會顯示原本 Start Date / Due Date
- 已加入附件權限：`ADMIN` 可刪除、`ENGINEER` 可替換、`OPERATOR` 可下載
- 已加入 `SUPERVISOR` 角色，可跨使用者修改與刪除 Jig
- 已加入 Jig 建立者/修改者追蹤，`ENGINEER` 只能修改自己建立的 Jig
- 已加入 Jig 新增、修改、狀態、Due Date、附件替換/刪除異動紀錄
- 已新增客戶移交文件：`docs/CUSTOMER-HANDOFF.md`
- 已通過 `mvn test`

## 常用指令

使用 Java 17 執行測試：

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home mvn test
```

連接 MySQL 後啟動專案：

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home mvn spring-boot:run
```

本機網址：

```text
http://localhost:8080
```

開發測試帳號：

```text
admin / 123456
supervisor / 123456
engineer / 123456
operator / 123456
```

## 角色權限

| Role | Main Use | Allowed Actions |
| --- | --- | --- |
| `ADMIN` | System owner / user administrator | User management, scrap approval, add/edit jigs, update status/due date, upload/delete files, download files, export CSV |
| `SUPERVISOR` | Team lead / line supervisor | Add/edit/delete jigs across users, update status/due date, replace/delete files, view logs, export CSV |
| `ENGINEER` | Maintenance / process engineer | Add jigs, edit own jigs, update own jig status/due date, view logs, replace own jig files, export CSV |
| `OPERATOR` | Production line / read-only user | Search jigs, view logs, download files, export CSV |

## 帳號與密碼管理

- Admin creates the account and provides the initial password.
- Users can change their own password from `Change Password`.
- Username cannot be changed after account creation.
- User Management records account changes in `Account Change Logs`.
- Passwords are stored with BCrypt encryption.

## Jig No. 給號方式

- Default prefix is `AM-ME`.
- `AM` 可代表組裝機構類治具；`ME` 可代表機構部門單位。
- 新增 Jig 時系統會先帶出下一個流水號，例如 `AM-ME-005`。
- 使用者可以把前綴改成其他兩碼加兩碼格式，例如 `BM-QA`，再按 `Get Next` 取得下一號。
- 使用者仍可手動調整完整 Jig No.；儲存時系統會再次檢查，不允許重號。

## 技術方向

- Java 17
- Spring Boot 3.x
- Maven
- MySQL 8.4 LTS
- Spring Data JPA
- Spring Security
- Thymeleaf
- Bootstrap 5

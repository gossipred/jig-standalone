# 專案進度追蹤

更新日期：2026-05-22

## 目前完成

- [x] 建立 Git 專案資料夾
- [x] 將 `Downloads/PLAN-jig.md` 複製到 `docs/PLAN-jig.md`
- [x] 建立專案說明 `README.md`
- [x] 建立計劃審查 `docs/PLAN-review.md`
- [x] 建立資料庫 schema 草稿 `data/schema.sql`
- [x] 建立測試資料草稿 `data/seed.sql`
- [x] 建立變更紀錄 `progress/CHANGELOG.md`
- [x] 建立決策紀錄 `progress/DECISIONS.md`
- [x] 建立 Spring Boot Maven 專案
- [x] 系統顯示名稱調整為 `Jig & Toolings Management System / 治具及模具管理系統`
- [x] 設定 `pom.xml`
- [x] 設定 `application.properties`
- [x] 建立 Entity：
  - `User`
  - `Jig`
  - `JigLog`
  - `ImportLog`
- [x] 建立 Repository：
  - `UserRepository`
  - `JigRepository`
  - `JigLogRepository`
  - `ImportLogRepository`
- [x] 建立基本登入設定
- [x] 建立首頁與登入頁
- [x] 建立治具列表頁
- [x] 建立治具搜尋功能
- [x] 建立新增與修改治具功能
- [x] 建立 Jig No. 格式驗證與標準化：
  - `AM-ME-001-01` 自動轉成 `AM-ME-001`
  - `AM-ME-001-02` 保留為第二套
- [x] 建立 Jig No. 半自動給號：
  - 新增 Jig 預設帶出 `AM-ME` 下一個流水號
  - 使用者可調整前綴，例如 `BM-QA`
  - 儲存時仍會檢查不可重號
- [x] DRI / 負責人由登入帳號自動帶入，畫面唯讀，儲存時由後端再次套用登入帳號
- [x] 重新定義 Status：On Process 在線使用中、Normal 正常存放、Scrap 進行報廢、Hold 保留、Repair 維修中、Maintain 保養
- [x] Jig List 最前面加入 Classification / 產品類別欄位，方便區分 Jig 與 Tooling
- [x] Jig List 加入 Ass'y Line / 組裝線別欄位，放在 Model 後面
- [x] Jig List Keyword 搜尋擴充為可查 Classification, Jig No., Model, Ass'y Line, Jig Name, Customer, Status, Owner
- [x] 優化 Login / Home 視覺，加入工業圖紙風格背景
- [x] 主要頁面套用淡工業圖紙風格背景
- [x] 修復按鈕 hover 時中文輔助文字顏色不清楚的問題
- [x] Home 移除 Phase 1~3 開發進度，改為 Daily Workflow 與 Role Guide
- [x] 將治具圖片/圖紙欄位改為檔案上傳，檔案存放於 `uploads/jigs/`
- [x] 支援每個 Jig 最多 5 個上傳檔案
- [x] 支援 Jig 檔案下載
- [x] 建立狀態與 Due Date 更新功能
- [x] 建立異動紀錄頁
- [x] 更新狀態與 Due Date 時寫入 `jig_logs`
- [x] 修正 Edit Jig 內變更 Status / Due Date 時，會額外寫入 Old / New 的專用異動紀錄
- [x] 建立 CSV 匯出功能
- [x] 支援匯出全部或搜尋後的治具資料
- [x] 執行 `data/schema.sql` 建立 MySQL 資料庫與資料表
- [x] 執行 `data/seed.sql` 建立預設帳號與範例治具
- [x] 連接 MySQL 後啟動 Spring Boot
- [x] 使用 `admin / 123456` 完成登入與 Jig List 實機驗證
- [x] 建立 ADMIN 使用者管理頁
- [x] 支援新增使用者、修改角色、啟用/停用帳號、重設密碼
- [x] 建立個人 Change Password 功能，使用者可自行修改密碼但不可修改帳號
- [x] 建立帳號異動紀錄 `user_logs`，記錄帳號建立、角色變更、啟用狀態變更、admin 重設密碼、個人修改密碼
- [x] 在 ADMIN User Management 顯示 Account Change Logs
- [x] 套用本機 MySQL migration：`data/migration-2026-05-22-user-logs.sql`
- [x] 在 User Management 加入角色權限矩陣與角色說明
- [x] 調整角色權限：OPERATOR 唯讀、ENGINEER 可新增/修改自己建立的 Jig、SUPERVISOR 可跨人修改/刪除、ADMIN 最大權限
- [x] 建立角色權限自動測試
- [x] 驗證 ENGINEER 可開啟並送出自己建立的 Jig Edit，OPERATOR 無法開啟或送出 Jig Edit
- [x] 將 Jig、JigLog、User 的建立/更新時間改為由 Hibernate 自動填入
- [x] 修正 Jig Edit 的 Start Date / Due Date 顯示格式，編輯時會顯示原本日期
- [x] 加入 ADMIN 檔案刪除功能
- [x] 加入 ENGINEER 檔案替換功能
- [x] 確認 OPERATOR 只能下載檔案，不能刪除或替換
- [x] 新增 `SUPERVISOR` 角色
- [x] 新增 Jig `created_by` / `updated_by` 追蹤
- [x] 限制 ENGINEER 只能修改自己建立的 Jig
- [x] 允許 SUPERVISOR / ADMIN 修改與刪除所有 Jig
- [x] 在 Jig List 加入 Owner 欄位與 SUPERVISOR / ADMIN Delete 按鈕
- [x] 新增 Jig 新增、修改、附件替換/刪除異動紀錄
- [x] 套用本機 MySQL migration：`data/migration-2026-05-21-ownership.sql`
- [x] 建立客戶移交文件 `docs/CUSTOMER-HANDOFF.md`
- [x] 加入測試用 H2 設定
- [x] 通過 `mvn test`

## 下一步

- [ ] 建立正式客戶交付用 start script 與 README-CUSTOMER.md
- [ ] 建立全域刪除稽核表，保留已刪除 Jig 的刪除紀錄
- [ ] 建立 Notion 匯入準備設定頁或設定文件

## 專案決策

- 第一版不做 Notion 自動同步，只保留資料表欄位與設定。
- 第一版支援治具圖片/圖紙檔案上傳，每個 Jig 初步限制最多 5 個檔案。
- 使用者介面顯示名稱採 `Jig & Toolings Management System / 治具及模具管理系統`，但第一版後端資料模型仍沿用 `Jig` 命名，避免高風險大改。
- 上傳檔案使用 `jig_files` 表保存，`jig_picture_url` 暫時保留作為相容欄位。
- 第一版預設角色為 `ADMIN`、`SUPERVISOR`、`ENGINEER`、`OPERATOR`。
- 第一版帳號名稱建立後不可修改；admin 可提供初始密碼與重設密碼，使用者可自行修改個人密碼。
- 第一版角色權限：
  - `ADMIN`：帳號管理、報廢核准、治具新增/修改、狀態與 Due Date 更新、上傳/刪除檔案、下載、匯出
  - `SUPERVISOR`：跨使用者新增/修改/刪除治具、狀態與 Due Date 更新、替換/刪除檔案、查看紀錄、匯出
  - `ENGINEER`：治具查詢、新增治具、修改自己建立的治具、非報廢狀態與 Due Date 更新、替換自己治具的檔案、匯出
  - `OPERATOR`：唯讀查詢、查看紀錄、下載檔案、匯出
- 第一版預設治具/模具狀態為 `OnProcess`、`Normal`、`Scrap`、`Hold`、`Repair`、`Maintain`。
- 第一版 Jig No. 採半自動給號：預設 `AM-ME`，可調整前綴，系統提供下一個三碼流水號，儲存時檢查不可重號。
- 第一版 DRI / 負責人不由使用者手動輸入，改用目前登入帳號自動寫入。
- 第一版 Status 在列表與明細頁使用顏色辨識：On Process 藍、Normal 淡綠、Scrap 灰、Hold 黃、Repair 紅、Maintain 淺藍。
- Jig List 顯示組裝線別，避免使用者為了查線別進入 Edit Jig。
- Jig List Keyword 搜尋涵蓋主要列表欄位，包含產品類別、狀態與建立者。
- Login / Home 使用工業圖紙風格背景，並把首頁內容改成使用者能理解的操作說明。
- 按鈕內的中文輔助文字使用共用 CSS 控制 hover 顏色，避免文字在灰色按鈕背景上消失。
- 介面文字以英文為主；空間足夠時中英對照，中文作為較小的輔助文字。

詳細決策紀錄請看 `progress/DECISIONS.md`。

## 最新驗證

- 日期：2026-05-22
- H2 測試指令：

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home mvn test
```

- H2 測試結果：

```text
BUILD SUCCESS
Tests run: 47, Failures: 0, Errors: 0
```

- MySQL 實機驗證：
  - MySQL 版本：8.4.9
  - 資料庫：`web_fixture_management`
  - 已建立資料表：`users`, `user_logs`, `jigs`, `jig_files`, `jig_logs`, `import_logs`
  - 已匯入預設帳號：`admin`, `supervisor`, `engineer`, `operator`
  - 已匯入範例治具：`AM-ME-001`, `AM-ME-001-02`, `BM-ME-002`
  - Spring Boot 已在 `http://localhost:8080` 啟動
  - `admin / 123456` 登入成功
  - `/jigs` 顯示範例治具成功
  - `engineer / 123456` 可開啟 `/jigs/1/edit`
  - `operator / 123456` 開啟 `/jigs/1/edit` 會被拒絕，回應 `403`
  - `/jigs/1/edit` 的 Start Date / Due Date 會以 `yyyy-MM-dd` 顯示原日期
  - `engineer / 123456` 開啟 admin 建立的 `/jigs/1/edit` 會被拒絕，回應 `403`
  - `supervisor / 123456` 可在 Jig List 看到跨資料 Edit / Delete
  - `/account/password` 可修改個人密碼，帳號欄位唯讀
  - `/admin/users` 可查看 Account Change Logs

## 備註

這份檔案之後可以當作施工日誌。每完成一個功能，就在這裡打勾，避免做到一半忘記自己剛剛在忙什麼。

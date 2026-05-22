# Java 程式設計結訓專題報告

## 專題名稱

**Jig & Toolings Management System**  
**治具及模具管理系統**

課程：Java 程式設計  
作者：JJ  
開發技術：Java 17、Spring Boot、Maven、MySQL、Spring Security、Thymeleaf、Bootstrap

---

## 1. 專題動機

在工廠與工程管理現場，治具與模具是生產、維修、保養中非常重要的資產。過去這類資料常見的管理方式包含 Excel、紙本紀錄、個人資料夾、或口頭交接。這些方式在資料量小時可以使用，但當治具數量增加、使用者變多、狀態變更頻繁時，就容易發生以下問題：

- 不容易快速查詢治具或模具位置與狀態。
- 圖片、圖紙、PDF、CAD 檔案分散存放，不容易追蹤。
- 不知道是誰修改資料、何時修改、改了什麼。
- 權限沒有分級時，容易發生誤改、誤刪。
- 工程人員、產線人員、管理者需要的功能不同，但傳統表格很難控管。

因此，本專題希望建立一套以 Java Web 技術開發的治具及模具管理系統，讓使用者可以透過瀏覽器完成查詢、新增、修改、附件管理、狀態追蹤與權限管理。

---

## 2. 專題目標

本系統的目標不是只做一個靜態網頁，而是完成一個具備後端邏輯、資料庫、登入權限與異動紀錄的完整 Java Web 專題。

主要目標如下：

1. 建立治具與模具主資料管理功能。
2. 支援產品類別、機種、組裝線別、治具編號、客戶、狀態等欄位查詢。
3. 支援圖片、圖紙、PDF、CAD 等檔案上傳與下載。
4. 建立角色權限，避免不同使用者做超出職責的操作。
5. 記錄資料異動，包含使用者、時間、動作、舊值與新值。
6. 建立帳號管理與個人密碼修改功能。
7. 提供 CSV 匯出，方便管理者進一步分析。
8. 規劃客戶移交方式，讓系統可以部署在公司內部網路使用。

---

## 3. 系統使用者與權限設計

系統目前設計四種角色：

| Role | 使用情境 | 主要權限 |
| --- | --- | --- |
| ADMIN | 系統管理者 | 管理使用者、核准報廢、修改所有資料、刪除資料、匯出 |
| SUPERVISOR | 主管或線長 | 跨使用者修改與刪除 Jig / Tooling，查看紀錄與匯出 |
| ENGINEER | 工程或維修人員 | 新增資料、修改自己建立的資料、替換檔案、更新非報廢狀態 |
| OPERATOR | 產線或查詢人員 | 查詢、查看紀錄、下載檔案、匯出 CSV |

此設計的核心概念是：**不是每個人都應該擁有最大權限**。  
在實際工廠環境中，權限分級可以降低誤刪、誤改與資料混亂的風險。

---

## 4. 系統架構

本系統採用 Spring Boot MVC 架構，主要分成：

- Controller：處理瀏覽器請求與頁面路由。
- Service：處理商業邏輯，例如權限、編號、異動紀錄。
- Repository：使用 Spring Data JPA 存取資料庫。
- Entity：對應 MySQL 資料表。
- Templates：使用 Thymeleaf 產生網頁畫面。
- Security：使用 Spring Security 管理登入與角色權限。

技術堆疊：

| Layer | Technology |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot |
| Build Tool | Maven |
| Database | MySQL 8.4 |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security |
| View | Thymeleaf |
| UI | Bootstrap 5 |
| Test | JUnit, Spring Boot Test, H2 |

---

## 5. 資料庫設計

主要資料表如下：

- `users`：登入帳號、角色、啟用狀態。
- `user_logs`：帳號異動紀錄，例如改密碼、角色變更。
- `jigs`：治具/模具主資料。
- `jig_files`：附件檔案資訊。
- `jig_logs`：治具/模具異動紀錄。
- `import_logs`：預留匯入紀錄。

其中 `jigs` 是核心資料表，記錄治具與模具主資料，例如：

- Classification / 產品類別
- Model / 機種
- Ass'y Line / 組裝線別
- Jig No. / 治具編號
- Customer / 客戶
- Status / 狀態
- Due Date / 到期日
- Created By / 建立者
- Updated By / 修改者

`jig_logs` 則記錄異動歷程，例如 Status 從 Normal 改成 Repair，會記錄 old status 與 new status。

---

## 6. 主要功能

### 6.1 登入與首頁

使用者必須登入後才能進入系統。首頁顯示目前登入帳號、角色、日常操作流程與角色說明。

### 6.2 Jig / Tooling List

列表顯示治具與模具的主要資訊，並將 `Classification / 產品類別` 放在第一欄，方便區分治具與模具。

關鍵字搜尋支援：

- Classification
- Jig No.
- Model
- Ass'y Line
- Jig Name
- Customer
- Status
- Owner

### 6.3 新增與修改資料

ENGINEER、SUPERVISOR、ADMIN 可以新增資料。ENGINEER 只能修改自己建立的資料，SUPERVISOR 與 ADMIN 可以跨使用者修改。

### 6.4 Jig No. 半自動給號

新增資料時，系統可以依照前綴自動提供下一個流水號。例如：

- 預設前綴：`AM-ME`
- 自動產生：`AM-ME-001`, `AM-ME-002`
- 使用者可調整前綴，例如 `BM-QA`
- 儲存時仍會檢查不可重號

這樣保留了現場彈性，也降低手動輸入錯誤。

### 6.5 檔案上傳與下載

系統支援每筆資料最多 5 個附件，可上傳治具照片、圖紙、PDF 或 CAD 檔案。OPERATOR 可以下載，ENGINEER 可替換自己資料的檔案，ADMIN / SUPERVISOR 可刪除檔案。

### 6.6 狀態管理

目前狀態包含：

- On Process / 在線使用中
- Normal / 正常存放
- Scrap / 進行報廢
- Hold / 保留
- Repair / 維修中
- Maintain / 保養

列表與明細頁使用顏色標籤協助辨識狀態。

### 6.7 異動紀錄

系統會記錄重要異動：

- 建立資料
- 修改主資料
- 狀態變更
- Due Date 變更
- 檔案替換
- 檔案刪除
- 帳號異動
- 個人修改密碼

例如狀態變更時，會記錄：

- 何時變更
- 誰變更
- 原本狀態
- 新狀態
- 備註

### 6.8 帳號與密碼管理

ADMIN 可以建立帳號與提供初始密碼。使用者登入後可以自行修改個人密碼，但不能修改帳號名稱。帳號名稱固定可以確保異動紀錄與責任追蹤不會中斷。

---

## 7. 開發歷程與需求修正

本專題開發過程不是一次完成，而是依照使用情境逐步修正。

重要調整包含：

1. 系統原本以 Jig 為主，後來擴充為 Jig & Toolings，讓模具也可以管理。
2. 圖片欄位原本可能是 URL，後來改為實際檔案上傳。
3. 權限從簡單角色，擴充為 ADMIN、SUPERVISOR、ENGINEER、OPERATOR。
4. ENGINEER 從可修改資料，調整為只能修改自己建立的資料。
5. 新增 SUPERVISOR 角色，用來跨人修改與刪除。
6. DRI 欄位改成登入帳號自動帶入，避免手動填錯。
7. Status 重新定義並加入顏色辨識。
8. Jig List 增加 Classification 與 Ass'y Line，讓查詢更直覺。
9. 帳號密碼管理加入個人改密碼與帳號異動紀錄。
10. 異動紀錄補強 old/new 欄位，避免只知道有改，卻不知道改了什麼。

這些修改反映了實務系統開發的特性：需求會隨使用者測試逐步清楚，程式也要跟著調整。

---

## 8. 測試與驗證

本專題使用 Spring Boot Test 與 H2 測試資料庫建立自動化測試。

目前測試重點包含：

- 角色權限測試
- ENGINEER / OPERATOR 存取限制
- SUPERVISOR 跨資料權限
- Jig No. 自動給號
- DRI 自動帶入
- Status 顯示與搜尋
- Classification 搜尋
- 檔案權限
- 帳號改密碼
- 帳號異動紀錄
- Edit Jig 時 Status / Due Date old/new 紀錄

最新測試結果：

```text
mvn test
BUILD SUCCESS
Tests run: 47, Failures: 0, Errors: 0
```

---

## 9. 客戶移交與內網部署規劃

未來如果交給客戶使用，最簡單的方式是：

1. 指定一台電腦作為伺服器。
2. 在伺服器安裝 Java 17、MySQL、系統 JAR。
3. 將資料庫 schema、seed data、uploads 資料夾一起交付。
4. 由該電腦啟動系統。
5. 其他使用者在同一個內部網路，用瀏覽器連線。

例如伺服器 IP 是：

```text
192.168.1.50
```

使用者只要在瀏覽器輸入：

```text
http://192.168.1.50:8080
```

就可以使用，不需要每台電腦都安裝系統。

後續可再補強：

- 一鍵啟動腳本
- 一鍵備份
- 一鍵還原
- 刪除紀錄稽核表
- 客戶操作手冊

---

## 10. 學習成果

透過本專題，我練習到的不只是 Java 語法，也包含完整系統開發流程：

- Java 類別與物件設計
- Spring Boot MVC 架構
- Controller / Service / Repository 分工
- MySQL 資料表設計
- JPA Entity 關聯
- Spring Security 權限控管
- Thymeleaf 前後端資料整合
- 檔案上傳與下載
- 自動化測試
- 系統移交思維

這個專題也把我過去工程管理與機械設計背景，轉換成可以落地的 Java Web 系統。

---

## 11. 結論

Jig & Toolings Management System 是一套以 Java Spring Boot 開發的治具及模具管理系統。它從實際工廠管理需求出發，完成登入、權限、主資料管理、附件、異動紀錄、搜尋、匯出與測試。

本專題最大的價值在於，它不是單純展示畫面，而是將資料正確性、權限控管、維護紀錄與未來移交都納入考量。對 Java 程式設計課程而言，這是一個結合實務需求與程式技術的完整專題。


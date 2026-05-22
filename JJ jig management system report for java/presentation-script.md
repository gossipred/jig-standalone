# 專題口頭報告文字稿

## Slide 1 - Title

各位老師、同學大家好，我是 JJ。  
今天我要報告的專題是 **Jig & Toolings Management System**，中文名稱是 **治具及模具管理系統**。這是我在 Java 程式設計課程中的結訓專題。

這個題目跟我的工程背景有關。過去在工廠或工程現場，治具、模具、圖紙、維修紀錄常常分散在 Excel、紙本或個人資料夾裡。當資料量變多、使用者變多，就會很難追蹤誰改了什麼、哪一個治具目前是正常、維修、保養或報廢。

所以我希望用 Java Web 技術，做一套可以實際用在工廠內部的管理系統。

## Slide 2 - Project Motivation

這個專題的出發點是現場管理問題。治具與模具本身不是單純的資料，它們會跟生產線、機種、客戶、維修狀態、圖紙檔案、負責人都有關係。

如果只用 Excel 管理，雖然一開始很方便，但比較難做到權限控管，也很難知道資料是誰修改的。這次專題就是希望把這些現場需求轉成系統功能。

## Slide 3 - Project Goals

我的系統目標有幾個重點。第一是能管理治具與模具主資料。第二是能上傳圖片、圖紙、PDF 或 CAD 檔案。第三是建立角色權限，讓不同使用者做不同事情。第四是保留異動紀錄，例如狀態從 Normal 改成 Repair，要記錄舊值、新值、修改人與時間。

最後，我也規劃未來可以部署在客戶內部網路，一台電腦當伺服器，其他人用瀏覽器使用。

## Slide 4 - System Architecture

這張圖是系統架構。瀏覽器送出請求到 Spring Boot，Controller 負責接收請求，Service 負責商業邏輯，Repository 透過 JPA 存取 MySQL。

畫面使用 Thymeleaf 與 Bootstrap，登入與角色權限使用 Spring Security。這個架構讓前端畫面、後端邏輯與資料庫分工比較清楚。

## Slide 5 - Java Technology Stack

這次使用的主要技術是 Java 17、Spring Boot、Maven、MySQL、Spring Data JPA、Spring Security、Thymeleaf 與 Bootstrap。

我覺得這個專題很適合 Java 課程，因為它不是只寫單一 class，而是包含 Entity、Controller、Service、Repository、Security、Test 等完整結構。

## Slide 6 - Database Design

資料庫主要有六張表。`users` 管理帳號，`user_logs` 記錄帳號異動，`jigs` 是治具與模具主資料，`jig_files` 管理附件，`jig_logs` 記錄治具異動，`import_logs` 預留未來匯入紀錄。

在這裡最重要的是 `jigs` 與 `jig_logs`。主資料負責保存目前狀態，異動紀錄負責回答「誰在什麼時間改了什麼」。

## Slide 7 - Role Permission Design

系統設計四種角色：ADMIN、SUPERVISOR、ENGINEER、OPERATOR。

ADMIN 是最大權限，可以管理帳號、核准報廢與修改所有資料。SUPERVISOR 可以跨使用者修改與刪除資料。ENGINEER 可以新增與修改自己建立的資料。OPERATOR 只能查詢、看紀錄、下載與匯出。

這樣做的目的是降低誤刪與誤改風險，讓權限符合現場工作分工。

## Slide 8 - Main Workflow

使用流程是：先登入，進入列表查詢，再看明細或新增資料。工程人員可以上傳附件、更新狀態或到期日。系統會把這些動作寫入異動紀錄。

這個流程對產線人員來說比較簡單，主要是查詢與下載；對工程人員來說可以維護資料；對管理者來說可以管理帳號與查看紀錄。

## Slide 9 - Key UI Features

畫面上我使用英文為主，中文做較小的輔助文字，因為這套系統未來可能有外國使用者。

Jig List 第一欄是 Classification，用來區分 Jig 或 Tooling。Status 用顏色標籤顯示，例如 On Process 是藍色、Normal 是淡綠色、Repair 是紅色。這樣使用者掃描列表時會比較快。

## Slide 10 - Iterative Development

這張是開發過程中的需求修正。專題不是一次寫完，而是依照使用情境逐步調整。

例如原本只管理 Jig，後來改成 Jig & Toolings。原本圖片可能用 URL，後來改成檔案上傳。權限也從簡單角色，擴充到 ADMIN、SUPERVISOR、ENGINEER、OPERATOR。

這些調整讓我理解到，真正的系統開發不是只把程式寫出來，而是要根據使用者回饋修正。

## Slide 11 - Change Logs And Traceability

異動紀錄是這個系統的重要功能。只知道資料被改過是不夠的，還要知道誰改、何時改、舊值是什麼、新值是什麼。

例如 Status 從 Normal 改成 On Process，系統會寫入 `STATUS_CHANGE`，並記錄 old status 與 new status。這對工廠管理很重要，因為可以追溯責任與維護歷程。

## Slide 12 - Testing And Verification

我使用 `mvn test` 執行自動化測試，目前有 47 個測試通過，沒有失敗。

測試包含角色權限、Jig No. 給號、DRI 自動帶入、檔案權限、帳號改密碼、分類搜尋、Status old/new 紀錄等。這讓我比較有信心，修改新功能時不會不小心破壞原本功能。

## Slide 13 - Deployment And Handoff

未來客戶移交時，最簡單的方式是一台電腦作為伺服器，安裝 Java、MySQL 與系統程式。其他使用者在同一個內部網路，用瀏覽器連線。

例如伺服器 IP 是 `192.168.1.50`，其他電腦就可以開 `http://192.168.1.50:8080` 使用。這樣不需要每台電腦都安裝系統，比較容易維護。

## Slide 14 - Conclusion

總結來說，這次專題讓我把 Java 程式設計、資料庫、網頁、登入權限、檔案管理、測試與部署概念串起來。

對我來說，最大的收穫是把工程管理經驗轉成可以運作的 Java Web 系統。後續還可以補強刪除稽核、一鍵安裝、備份還原與 Excel 匯入。

以上是我的專題報告，謝謝大家。


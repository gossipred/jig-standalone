# Change Log

## 2026-05-22

### Windows Customer Handoff Package

- Built the Spring Boot executable JAR for customer delivery:
  - `target/jig-management-system-0.0.1-SNAPSHOT.jar`
- Created a Windows handoff package:
  - `JJ jig toolings customer handoff package/`
- Added customer-facing files:
  - `app/jig-toolings-management.jar`
  - `app/application.properties`
  - `app/start-system.bat`
  - `app/stop-system.bat`
  - `database/install-database.bat`
  - `database/default-users.sql`
  - `database/sample-data.sql`
  - `scripts/backup-now.bat`
  - `scripts/open-firewall-8080.bat`
  - `documents/windows-installation-guide.md`
  - `documents/admin-operation-guide.md`
  - `documents/user-quick-guide.md`
  - `documents/handoff-checklist.md`
- Updated `docs/CUSTOMER-HANDOFF.md` with the Windows deployment flow.
- Added a separate Chinese customer handoff guide outside the zip package:
  - `JJ jig toolings Chinese handoff guide/治具及模具管理系統_中文移交安裝說明.docx`
  - `JJ jig toolings Chinese handoff guide/中文移交安裝說明.md`

### Verification

- Latest package build result:

```text
BUILD SUCCESS
Tests run: 47, Failures: 0, Errors: 0, Skipped: 0
```

### Jig List Classification

- Added `Classification / 產品類別` as the first column on Jig / Tooling List.
- Updated the list helper text and keyword placeholder to include product category examples.
- Expanded keyword search to include `classification`, so users can search Jig, Tooling, Mouse, mold categories, or other product/category labels.
- Added an automated test for classification search.

### Verification

- Latest test result:

```text
BUILD SUCCESS
Tests run: 45, Failures: 0, Errors: 0
```

### Jig Change Log Accuracy

- Fixed Edit Jig saves so status changes also create a `STATUS_CHANGE` log with old/new status values.
- Fixed Edit Jig saves so due date changes also create a `DUE_DATE_CHANGE` log with old/new due date values.
- Kept the general `UPDATE` log for master-data saves.
- Added tests for old/new status and due date logs from the Edit Jig form.

### Verification

- Latest test result:

```text
BUILD SUCCESS
Tests run: 47, Failures: 0, Errors: 0
```

### Account Password Management

- Added personal password change flow:
  - Any logged-in user can change their own password.
  - Current password is required.
  - New password and confirmation must match.
  - Username is shown as read-only and cannot be changed from the personal account page.
- Added account audit records:
  - `CREATE`
  - `ROLE_CHANGE`
  - `ENABLED_CHANGE`
  - `PASSWORD_RESET`
  - `PASSWORD_CHANGE`
- Added `user_logs` entity, repository, schema, and MySQL migration:
  - `data/migration-2026-05-22-user-logs.sql`
- Updated ADMIN User Management:
  - Shows latest 50 Account Change Logs.
  - Edit user form keeps username read-only after creation.
  - Admin can still reset passwords and update role/enabled status.
- Added Password links to the main navigation on home, jig pages, and admin user pages.

### Verification

- Applied MySQL migration for `user_logs`.
- Restarted Spring Boot on `http://localhost:8080`.
- Browser-verified:
  - Home shows Change Password.
  - `/account/password` shows read-only username.
  - `/admin/users` shows Account Change Logs.
- Latest test result:

```text
BUILD SUCCESS
Tests run: 44, Failures: 0, Errors: 0
```

## 2026-05-21

### Project Setup

- Created the project folder structure under `/Users/chienchungwu/Documents/jig management system`.
- Moved the original project plan into `docs/PLAN-jig.md`.
- Added project overview and working notes:
  - `README.md`
  - `docs/PLAN-review.md`
  - `progress/STATUS.md`

### Environment

- Confirmed Java 17 and Maven workflow.
- Added VS Code project settings:
  - `.vscode/settings.json`
  - `.vscode/extensions.json`

### Database

- Added first database schema draft in `data/schema.sql`.
- Added seed data draft in `data/seed.sql`.
- Added these tables:
  - `users`
  - `jigs`
  - `jig_files`
  - `jig_logs`
  - `import_logs`

### Spring Boot Foundation

- Added Maven Spring Boot project through `pom.xml`.
- Added the main application class:
  - `src/main/java/com/jj/jig/JigManagementSystemApplication.java`
- Added application configuration:
  - `src/main/resources/application.properties`
- Added H2 test configuration:
  - `src/test/resources/application-test.properties`

### Login And Roles

- Added database-backed login service:
  - `DatabaseUserDetailsService`
- Added Spring Security configuration:
  - `SecurityConfig`
- Added basic login and home pages:
  - `templates/login.html`
  - `templates/home.html`
- Added ADMIN-only User Management:
  - List users.
  - Add users.
  - Edit role.
  - Enable or disable accounts.
  - Reset password with BCrypt encryption.
- Added visible role permission guidance in User Management:
  - `ADMIN`: user management and scrap approval.
  - `ENGINEER`: add/edit jig content and non-scrap maintenance updates.
  - `OPERATOR`: read-only view, download, and export access.
- Added automated authorization tests for the first-release role rules:
  - ADMIN can open User Management.
  - ENGINEER cannot open User Management.
  - ENGINEER can open Add Jig.
  - ENGINEER can open and submit Jig Edit.
  - ENGINEER cannot set a Jig to Scrap.
  - OPERATOR cannot open Add Jig.
  - OPERATOR cannot open or submit Jig Edit.
  - OPERATOR can open Jig List.
  - Anonymous users are redirected to Login.
- Changed `Jig`, `JigLog`, and `User` timestamps to be filled by Hibernate so tests and runtime do not depend only on database default timestamps.
- Fixed Jig Edit date fields:
  - Start Date and Due Date now render as `yyyy-MM-dd`.
  - Existing dates remain visible when opening the edit form.
  - Added an authorization/form rendering test for existing edit dates.

### File Permissions

- Added ADMIN-only file delete flow.
- Added ADMIN/ENGINEER file replace flow.
- Kept OPERATOR file access read-only with download access only.
- Added tests for ADMIN delete, ENGINEER replace, and OPERATOR restrictions.

### Customer Handoff

- Added first-release customer handoff guide:
  - `docs/CUSTOMER-HANDOFF.md`

### Ownership And Supervisor Role

- Added `SUPERVISOR` role.
- Added Jig ownership fields:
  - `created_by`
  - `updated_by`
- Added migration:
  - `data/migration-2026-05-21-ownership.sql`
- Engineers can edit only Jigs they created.
- Supervisors and admins can edit/delete all Jigs.
- Jig List now shows Owner and displays Delete only for Supervisor/Admin.
- Added change logs for create, master-data update, file replace, and file delete.
- Added tests for owner-based access and supervisor delete permission.

### Jig And Tooling Management

- Added Jig list page and keyword search by:
  - Model Name
  - Jig No.
  - Customer
- Added create and edit Jig flow:
  - `JigController`
  - `JigService`
  - `JigForm`
- Added Jig No. validation and normalization:
  - `AM-ME-001` is valid.
  - `AM-ME-001-01` is saved as `AM-ME-001`.
  - `AM-ME-001-02` is saved as `AM-ME-001-02`.
- Added semi-automatic Jig No. assignment:
  - New Jig forms default to the next `AM-ME` number.
  - Users can change the prefix, such as `BM-QA`, and click `Get Next`.
  - The final save still blocks duplicate Jig No. values.
  - Invalid prefixes return a clear validation message.
- Changed DRI / responsible person handling:
  - The form auto-fills DRI from the current login account.
  - The DRI field is read-only in the UI.
  - The backend overwrites DRI with the login account during create/update, so submitted spoofed values are ignored.
- Improved status badges on Jig List and Jig Detail:
  - `OnProcess`: On Process / 在線使用中, blue.
  - `Normal`: Normal / 正常存放, light green.
  - `Scrap`: Scrap / 進行報廢, gray.
  - `Hold`: Hold / 保留, yellow.
  - `Repair`: Repair / 維修中, red.
  - `Maintain`: Maintain / 保養, light cyan.
  - Added MySQL migration `data/migration-2026-05-22-statuses.sql`.
- Improved Jig List scanning:
  - Added `Ass'y Line` after `Model`.
  - Empty assembly line values show `-`.
- Expanded Jig List keyword search:
  - Search now includes Jig No., Model, Ass'y Line, Jig Name, Customer, Status, and Owner.
  - Added tests for assembly line, jig name, status, and owner search.
- Improved Login and Home screens:
  - Added an industrial blueprint-style background image.
  - Replaced developer-facing `Phase 1`, `Phase 2`, and `Phase 3` cards with user-facing `Daily Workflow`.
  - Added `Role Guide` so users can understand what each role can do.
- Added shared UI stylesheet:
  - Main work pages now use a subtle industrial blueprint background.
  - Button hover states keep Chinese helper text visible.
  - Fixed the `Export CSV` hover state where helper text could disappear against the gray button background.
- Added Jig detail page with:
  - Master data summary.
  - File download list.
  - Status update form.
  - Due Date update form.
  - Change log table.
- Added Status and Due Date updates with history logs in `jig_logs`.
- Added CSV export:
  - Exports all Jig records when no keyword is provided.
  - Exports filtered Jig records when the list search keyword is active.
  - Excludes passwords, tokens, and sensitive system settings.

### File Uploads

- Changed the Jig picture/drawing field from URL input to file upload.
- Added multiple file upload support.
- Initial limit: up to 5 files per Jig.
- Added file download support through `/jigs/files/{fileId}/download`.
- Added file metadata storage:
  - `JigFile`
  - `JigFileRepository`
  - `JigFileStorageService`
- Uploaded files are stored under `uploads/jigs/`.

### Interface Language

- UI text is English-first for international users.
- Chinese is used as smaller helper text when there is enough room.
- Updated user-facing system name to `Jig & Toolings Management System / 治具及模具管理系統`.
- Kept backend Java classes, routes, and database table names using `Jig` for first-release stability.

### Verification

- Last verified command:

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home mvn test
```

- Result:

```text
BUILD SUCCESS
Tests run: 44, Failures: 0, Errors: 0
```

### MySQL Runtime Verification

- Created MySQL database from `data/schema.sql`.
- Loaded seed data from `data/seed.sql`.
- Confirmed MySQL tables:
  - `users`
  - `jigs`
  - `jig_files`
  - `jig_logs`
  - `import_logs`
- Confirmed default users:
  - `admin`
  - `engineer`
  - `operator`
- Confirmed sample Jigs:
  - `AM-ME-001`
  - `AM-ME-001-02`
  - `BM-ME-002`
- Started Spring Boot on `http://localhost:8080`.
- Verified login with `admin / 123456`.
- Verified `/jigs` displays the sample Jig records.

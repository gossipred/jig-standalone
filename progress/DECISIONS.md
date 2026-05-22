# Project Decisions

## 2026-05-22

### Classification On Jig List

- Decision: Show `Classification / 產品類別` as the first column on Jig / Tooling List.
- Reason: The system now manages both jigs and toolings/molds, so users need to identify the product/category before reading jig number or model details.
- Implementation:
  - The first list column is Classification.
  - Empty classification values show `-`.
  - Keyword search includes `classification`.

### Edit Jig Change Logs

- Decision: Edit Jig saves should still write a general `UPDATE` log, but status and due date changes must also write dedicated logs with Old/New values.
- Reason: Users may change status or due date from the full edit form, not only from the detail-page quick update panels.
- Implementation:
  - Status changes from Edit Jig create `STATUS_CHANGE` with `old_status` and `new_status`.
  - Due date changes from Edit Jig create `DUE_DATE_CHANGE` with `old_due_date` and `new_due_date`.

### Account Password Management

- Decision: Users may change only their own password, not their username.
- Reason: Username is the traceability key for ownership and audit records.
- Implementation:
  - `/account/password` is available to all logged-in users.
  - The password form requires current password, new password, and confirmation.
  - Username is displayed as read-only.
  - Admin still creates accounts, provides the initial password, and can reset passwords from User Management.

### Account Change Logs

- Decision: Account management changes must be recorded in `user_logs`.
- Reason: Password and role changes affect system access and must be traceable.
- Implementation:
  - `CREATE` records admin-created accounts.
  - `ROLE_CHANGE` records role updates.
  - `ENABLED_CHANGE` records enable/disable changes.
  - `PASSWORD_RESET` records admin password resets.
  - `PASSWORD_CHANGE` records users changing their own passwords.
  - ADMIN User Management shows the latest 50 account logs.

## 2026-05-21

### System Name

- Decision: Use `Jig & Toolings Management System / 治具及模具管理系統` as the user-facing system name.
- Reason: The system can manage both jigs and mold/tooling records.
- Implementation:
  - Update page titles, navbar brand, login, home, and major action labels.
  - Keep backend class names, routes, and table names using `Jig` in the first release to avoid unnecessary migration risk.

### Interface Language

- Decision: Use English as the primary UI language.
- Reason: Foreign users will use this system.
- Implementation: Chinese text may be shown as smaller helper text when space allows.

### Jig File Handling

- Decision: Jig pictures and drawings are uploaded as files, not entered as URL text.
- Reason: Real jig management usually stores actual photos, drawings, PDFs, and CAD files.
- Implementation:
  - Files are saved under `uploads/jigs/`.
  - File metadata is saved in `jig_files`.
  - Each Jig is initially limited to 5 files.
  - Files can be downloaded from the UI.

### `jig_picture_url` Compatibility

- Decision: Keep the existing `jig_picture_url` column for now.
- Reason: Avoid a larger database migration while the project is still in early development.
- Implementation: New file records use `jig_files`; `jig_picture_url` remains as a compatibility field.

### Jig No. Normalization

- Decision: Normalize `AM-ME-001-01` to `AM-ME-001`.
- Reason: The first set should be treated as the base Jig number.
- Implementation:
  - `AM-ME-001` is saved as base set.
  - `AM-ME-001-01` is saved as `AM-ME-001`.
  - `AM-ME-001-02` is saved as an additional set.

### Jig No. Semi-Automatic Assignment

- Decision: Use semi-automatic Jig No. assignment instead of fully locking the number field.
- Reason: Factory numbering often needs human judgment, but the system should still prevent accidental duplicate numbers.
- Implementation:
  - New Jig forms default to the next `AM-ME` number.
  - Prefix format is two letters, dash, two letters, such as `AM-ME` or `BM-QA`.
  - Users can change the prefix and click `Get Next` to receive the next three-digit sequence.
  - Users can still manually adjust the full Jig No.
  - Saving the Jig remains the final duplicate-number check.

### DRI Auto-Fill

- Decision: Fill DRI / responsible person from the current login account.
- Reason: Users should not need to type their own account name, and the responsible account should not be manually spoofed.
- Implementation:
  - The form shows the login username in the DRI field.
  - The DRI field is read-only in the UI.
  - Create and update actions overwrite DRI with the authenticated username on the backend.

### Status Color Badges

- Decision: Use color-coded status badges and clear bilingual labels on Jig List and Jig Detail.
- Reason: Users can identify jig condition faster when scanning many rows.
- Implementation:
  - `OnProcess`: On Process / 在線使用中, blue.
  - `Normal`: Normal / 正常存放, light green.
  - `Scrap`: Scrap / 進行報廢, gray.
  - `Hold`: Hold / 保留, yellow.
  - `Repair`: Repair / 維修中, red.
  - `Maintain`: Maintain / 保養, light cyan.
  - Internally store On Process as `OnProcess` so Java enum and database values remain simple.

### Jig List Columns

- Decision: Show Ass'y Line directly on Jig List, after Model.
- Reason: Users need the assembly line when scanning or searching jigs, and should not need to open Edit Jig just to check it.
- Implementation:
  - Jig List column order starts with Jig No., Model, Ass'y Line.
  - Empty assembly line values show `-`.

### Jig List Keyword Search

- Decision: Keyword search should cover the main list columns, not only the original three fields.
- Reason: Users naturally expect the search box above the list to search what they can see in the list.
- Implementation:
  - Database-backed search covers Jig No., Model, Ass'y Line, Jig Name, Customer, and Owner.
  - Status search is matched in application code for database compatibility.
  - Due Date is not included in keyword search yet because date filtering should be a separate control later.

### Home And Login Content

- Decision: Use Login and Home for user orientation, not developer progress tracking.
- Reason: Users need to know how to operate the system; `Phase 1`, `Phase 2`, and `Phase 3` are internal project status and are not useful during daily work.
- Implementation:
  - Login uses an industrial blueprint-style background.
  - Home uses the same visual style in the hero area.
  - Home shows `Daily Workflow` and `Role Guide`.

### Shared UI Styling

- Decision: Use one shared stylesheet for common visual fixes and page background.
- Reason: Button hover behavior and page background should stay consistent across user-facing screens.
- Implementation:
  - `src/main/resources/static/css/app.css` controls shared button hover behavior.
  - Main work pages use the shared `app-industrial-bg` class.
  - Button Chinese helper text uses `currentColor` during normal and hover states, so it remains readable.

### Testing Database

- Decision: Use H2 for automated Spring Boot context tests.
- Reason: Tests should run without requiring MySQL to be configured first.
- Implementation: `src/test/resources/application-test.properties`.

### Status And Due Date Logs

- Decision: Status and Due Date changes must be written to `jig_logs`.
- Reason: Maintenance history needs traceability for review and accountability.
- Implementation:
  - Status changes use `STATUS_CHANGE`.
  - Due Date changes use `DUE_DATE_CHANGE`.
  - Logs store old value, new value, user, note, and created time.

### CSV Export Scope

- Decision: CSV export includes Jig master data only.
- Reason: Exported files may be shared outside the system, so sensitive fields must be excluded.
- Implementation:
  - Export includes classification, model, Jig No., status, due dates, and related Jig fields.
  - Export excludes passwords, tokens, internal import settings, and uploaded file paths.

### User Management

- Decision: User Management is available only to `ADMIN`.
- Reason: Account creation, role changes, and password reset are sensitive operations.
- Implementation:
  - `/admin/**` requires role `ADMIN`.
  - Passwords are stored with BCrypt.
  - Editing a user keeps the existing password when the password field is blank.

### Role Permissions

- Decision: Use four first-release roles: `ADMIN`, `SUPERVISOR`, `ENGINEER`, and `OPERATOR`.
- Reason: The system needs a simple permission model that matches real factory work without making account setup too complicated.
- Implementation:
  - `ADMIN` can manage users, approve Jig scrap, add/edit/delete all Jig data, update status/due date, upload/delete/download files, and export CSV.
  - `SUPERVISOR` can add/edit/delete Jigs across users, update status/due date, replace/delete uploaded files, view logs, and export CSV.
  - `ENGINEER` can view Jigs, add Jigs, edit only Jigs they created, update non-scrap status/due date for owned Jigs, replace owned uploaded files, view logs, and export CSV.
  - `OPERATOR` can only read Jig data, view logs, download files, and export CSV.
  - Existing Jigs are assigned to `admin` during the ownership migration unless ownership is later adjusted.
  - User Management now shows a role permission matrix so admins can choose roles correctly.
  - Authorization tests cover the first-release page access rules.

### Change Logs And Ownership

- Decision: Jig create, update, status change, due date change, file replace, and file delete actions write to `jig_logs`.
- Reason: Users need to know who changed what and when.
- Implementation:
  - `created_by` tracks the original creator of a Jig.
  - `updated_by` tracks the last user to update the Jig.
  - Change logs show action type, user, note, and created time.
  - Hard-deleting a Jig removes the row from the main list; a future global audit table is recommended if deleted-row history must remain searchable after deletion.

### Customer Handoff

- Decision: Use Java 17 + MySQL + application JAR as the simplest first-release customer handoff.
- Reason: It is easier for a small factory or department to operate than a full container/server platform at this stage.
- Implementation:
  - Deliver an application JAR.
  - Deliver a MySQL schema/data dump.
  - Deliver the `uploads/jigs/` folder.
  - Add a simple customer handoff guide in `docs/CUSTOMER-HANDOFF.md`.

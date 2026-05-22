CREATE DATABASE IF NOT EXISTS web_fixture_management
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE web_fixture_management;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(100) NOT NULL,
  role VARCHAR(20) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username),
  CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'SUPERVISOR', 'ENGINEER', 'OPERATOR'))
);

CREATE TABLE IF NOT EXISTS user_logs (
  id BIGINT NOT NULL AUTO_INCREMENT,
  target_user_id BIGINT,
  actor_user_id BIGINT,
  action_type VARCHAR(30) NOT NULL,
  old_value VARCHAR(100),
  new_value VARCHAR(100),
  note VARCHAR(1000),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_user_logs_target_user_id (target_user_id),
  KEY idx_user_logs_actor_user_id (actor_user_id),
  KEY idx_user_logs_action_type (action_type),
  KEY idx_user_logs_created_at (created_at),
  CONSTRAINT fk_user_logs_target_user_id
    FOREIGN KEY (target_user_id) REFERENCES users (id),
  CONSTRAINT fk_user_logs_actor_user_id
    FOREIGN KEY (actor_user_id) REFERENCES users (id),
  CONSTRAINT chk_user_logs_action_type CHECK (
    action_type IN ('CREATE', 'ROLE_CHANGE', 'ENABLED_CHANGE', 'PASSWORD_RESET', 'PASSWORD_CHANGE')
  )
);

CREATE TABLE IF NOT EXISTS jigs (
  id BIGINT NOT NULL AUTO_INCREMENT,
  classification VARCHAR(100),
  model_name VARCHAR(100) NOT NULL,
  jig_name VARCHAR(150) NOT NULL,
  customer VARCHAR(100),
  jig_no VARCHAR(20) NOT NULL,
  jig_base_no VARCHAR(12) NOT NULL,
  set_no VARCHAR(2),
  assembly_line VARCHAR(100),
  jig_picture_url VARCHAR(500),
  quantity INT NOT NULL DEFAULT 1,
  mro_no VARCHAR(100),
  pr_no VARCHAR(100),
  status VARCHAR(20) NOT NULL DEFAULT 'Normal',
  dri VARCHAR(100),
  start_date DATE,
  due_date DATE,
  due_date_updated_at DATETIME,
  due_date_updated_by BIGINT,
  created_by BIGINT,
  updated_by BIGINT,
  notion_page_id VARCHAR(100),
  notion_last_edited DATETIME,
  last_imported_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_jigs_jig_no (jig_no),
  KEY idx_jigs_model_name (model_name),
  KEY idx_jigs_customer (customer),
  KEY idx_jigs_status (status),
  KEY idx_jigs_due_date (due_date),
  KEY idx_jigs_notion_page_id (notion_page_id),
  CONSTRAINT fk_jigs_due_date_updated_by
    FOREIGN KEY (due_date_updated_by) REFERENCES users (id),
  CONSTRAINT fk_jigs_created_by
    FOREIGN KEY (created_by) REFERENCES users (id),
  CONSTRAINT fk_jigs_updated_by
    FOREIGN KEY (updated_by) REFERENCES users (id),
  CONSTRAINT chk_jigs_status CHECK (status IN ('OnProcess', 'Normal', 'Scrap', 'Hold', 'Repair', 'Maintain')),
  CONSTRAINT chk_jigs_quantity CHECK (quantity >= 0)
);

CREATE TABLE IF NOT EXISTS jig_files (
  id BIGINT NOT NULL AUTO_INCREMENT,
  jig_id BIGINT NOT NULL,
  original_filename VARCHAR(255) NOT NULL,
  stored_path VARCHAR(500) NOT NULL,
  content_type VARCHAR(100),
  file_size BIGINT NOT NULL,
  uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_jig_files_jig_id (jig_id),
  KEY idx_jig_files_uploaded_at (uploaded_at),
  CONSTRAINT fk_jig_files_jig_id
    FOREIGN KEY (jig_id) REFERENCES jigs (id)
    ON DELETE CASCADE,
  CONSTRAINT chk_jig_files_file_size CHECK (file_size >= 0)
);

CREATE TABLE IF NOT EXISTS jig_logs (
  id BIGINT NOT NULL AUTO_INCREMENT,
  jig_id BIGINT NOT NULL,
  user_id BIGINT,
  action_type VARCHAR(30) NOT NULL,
  old_status VARCHAR(20),
  new_status VARCHAR(20),
  old_due_date DATE,
  new_due_date DATE,
  note VARCHAR(1000),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_jig_logs_jig_id (jig_id),
  KEY idx_jig_logs_user_id (user_id),
  KEY idx_jig_logs_action_type (action_type),
  KEY idx_jig_logs_created_at (created_at),
  CONSTRAINT fk_jig_logs_jig_id
    FOREIGN KEY (jig_id) REFERENCES jigs (id)
    ON DELETE CASCADE,
  CONSTRAINT fk_jig_logs_user_id
    FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT chk_jig_logs_action_type CHECK (
    action_type IN ('CREATE', 'UPDATE', 'DELETE', 'FILE_REPLACE', 'FILE_DELETE', 'STATUS_CHANGE', 'DUE_DATE_CHANGE', 'NOTE')
  )
);

CREATE TABLE IF NOT EXISTS import_logs (
  id BIGINT NOT NULL AUTO_INCREMENT,
  source_type VARCHAR(30) NOT NULL,
  started_at DATETIME NOT NULL,
  finished_at DATETIME,
  status VARCHAR(20) NOT NULL,
  total_count INT NOT NULL DEFAULT 0,
  created_count INT NOT NULL DEFAULT 0,
  updated_count INT NOT NULL DEFAULT 0,
  skipped_count INT NOT NULL DEFAULT 0,
  failed_count INT NOT NULL DEFAULT 0,
  message VARCHAR(2000),
  PRIMARY KEY (id),
  KEY idx_import_logs_source_type (source_type),
  KEY idx_import_logs_started_at (started_at),
  CONSTRAINT chk_import_logs_status CHECK (status IN ('SUCCESS', 'FAILED', 'PARTIAL'))
);

USE web_fixture_management;

ALTER TABLE users
  DROP CHECK chk_users_role;

ALTER TABLE users
  ADD CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'SUPERVISOR', 'ENGINEER', 'OPERATOR'));

INSERT INTO users (username, password, role, enabled)
VALUES ('supervisor', '$2y$10$NBxtsR78JVgR3.OD0pOzr.w9fFl1Bchi0mvYpNsHP6n5baNDmM22e', 'SUPERVISOR', TRUE)
ON DUPLICATE KEY UPDATE
  role = VALUES(role),
  enabled = VALUES(enabled);

ALTER TABLE jigs
  ADD COLUMN created_by BIGINT NULL AFTER due_date_updated_by,
  ADD COLUMN updated_by BIGINT NULL AFTER created_by;

UPDATE jigs
SET
  created_by = COALESCE(created_by, (SELECT id FROM users WHERE username = 'admin')),
  updated_by = COALESCE(updated_by, (SELECT id FROM users WHERE username = 'admin'));

ALTER TABLE jigs
  ADD CONSTRAINT fk_jigs_created_by FOREIGN KEY (created_by) REFERENCES users (id),
  ADD CONSTRAINT fk_jigs_updated_by FOREIGN KEY (updated_by) REFERENCES users (id);

ALTER TABLE jig_logs
  DROP CHECK chk_jig_logs_action_type;

ALTER TABLE jig_logs
  ADD CONSTRAINT chk_jig_logs_action_type CHECK (
    action_type IN ('CREATE', 'UPDATE', 'DELETE', 'FILE_REPLACE', 'FILE_DELETE', 'STATUS_CHANGE', 'DUE_DATE_CHANGE', 'NOTE')
  );

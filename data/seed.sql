USE web_fixture_management;

-- Password note:
-- Default password for admin, supervisor, engineer, and operator is 123456.
-- Replace these accounts or passwords before production use.

INSERT INTO users (username, password, role, enabled)
VALUES
  ('admin', '$2y$10$NBxtsR78JVgR3.OD0pOzr.w9fFl1Bchi0mvYpNsHP6n5baNDmM22e', 'ADMIN', TRUE),
  ('supervisor', '$2y$10$NBxtsR78JVgR3.OD0pOzr.w9fFl1Bchi0mvYpNsHP6n5baNDmM22e', 'SUPERVISOR', TRUE),
  ('engineer', '$2y$10$NBxtsR78JVgR3.OD0pOzr.w9fFl1Bchi0mvYpNsHP6n5baNDmM22e', 'ENGINEER', TRUE),
  ('operator', '$2y$10$NBxtsR78JVgR3.OD0pOzr.w9fFl1Bchi0mvYpNsHP6n5baNDmM22e', 'OPERATOR', TRUE)
ON DUPLICATE KEY UPDATE
  role = VALUES(role),
  enabled = VALUES(enabled);

INSERT INTO jigs (
  classification,
  model_name,
  jig_name,
  customer,
  jig_no,
  jig_base_no,
  set_no,
  assembly_line,
  quantity,
  mro_no,
  pr_no,
  status,
  dri,
  start_date,
  due_date
)
VALUES
  ('Mouse', 'Demo Mouse A', 'Function Test Jig', 'Demo Customer', 'AM-ME-001', 'AM-ME-001', NULL, 'Line 1', 1, 'MRO-001', 'PR-001', 'OnProcess', 'JJ', '2026-05-21', '2026-06-21'),
  ('Mouse', 'Demo Mouse A', 'Function Test Jig Set 2', 'Demo Customer', 'AM-ME-001-02', 'AM-ME-001', '02', 'Line 1', 1, 'MRO-002', 'PR-002', 'Repair', 'JJ', '2026-05-21', '2026-06-28'),
  ('Mouse', 'Demo Mouse B', 'Assembly Check Jig', 'Demo Customer', 'BM-ME-002', 'BM-ME-002', NULL, 'Line 2', 2, 'MRO-003', 'PR-003', 'Hold', 'Engineer', '2026-05-21', '2026-07-05')
ON DUPLICATE KEY UPDATE
  model_name = VALUES(model_name),
  jig_name = VALUES(jig_name),
  customer = VALUES(customer),
  status = VALUES(status),
  due_date = VALUES(due_date);

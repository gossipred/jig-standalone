USE web_fixture_management;

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

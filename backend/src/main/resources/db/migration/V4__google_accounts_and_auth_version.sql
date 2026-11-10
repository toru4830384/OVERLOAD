-- パスワード変更前のセッションを、保存やログインの競合があっても拒否する。
ALTER TABLE user_credentials ADD COLUMN auth_version BIGINT NOT NULL DEFAULT 0;
-- Googleだけで登録した本人には、未設定のパスワードを捏造しない。
ALTER TABLE user_credentials MODIFY COLUMN password_hash VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin NULL;
CREATE TABLE google_accounts (
    user_id BIGINT NOT NULL PRIMARY KEY,
    google_subject VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    linked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_google_subject UNIQUE (google_subject),
    CONSTRAINT fk_google_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

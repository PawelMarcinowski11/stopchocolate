CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id uuid PRIMARY KEY,
    hashed_token BYTEA NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL
);


-- Extension required for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ── Enum types ────────────────────────────────────────────────────────────────

CREATE TYPE user_role AS ENUM (
  'SYSTEM_ADMIN',
  'CONGRESS_ADMIN',
  'PARTICIPANT',
  'GUEST_SPEAKER'
);

-- ── Tables ───────────────────────────────────────────────────────────────────

-- Main users table with soft-delete support
CREATE TABLE users (
  id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
  email         VARCHAR(320)  NOT NULL,
  password_hash VARCHAR(72),               -- NULL for guest speakers with no credentials yet
  full_name     VARCHAR(255)  NOT NULL,
  organization  VARCHAR(255)  NOT NULL,
  phone         VARCHAR(50)   NOT NULL,
  personal_id   VARCHAR(50)   NOT NULL,
  photo_url     TEXT,
  active        BOOLEAN       NOT NULL DEFAULT true,
  created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
  created_by    UUID,                      -- NULL for self-registration (bootstrap)
  updated_by    UUID,

  CONSTRAINT uq_users_email       UNIQUE (email),
  CONSTRAINT uq_users_personal_id UNIQUE (personal_id),
  CONSTRAINT ck_personal_id_alnum CHECK (personal_id ~ '^[A-Za-z0-9]+$')
);

CREATE INDEX idx_users_email  ON users (email);
CREATE INDEX idx_users_active ON users (active);

-- Roles granted to a user (multi-valued; enforces set semantics)
CREATE TABLE user_roles (
  user_id UUID       NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
  role    user_role  NOT NULL,
  PRIMARY KEY (user_id, role)
);

CREATE INDEX idx_user_roles_user ON user_roles (user_id);

-- Institutions a CONGRESS_ADMIN is linked to (logical FK; institution lives in conference_db)
CREATE TABLE user_institutions (
  user_id        UUID  NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
  institution_id UUID  NOT NULL,
  PRIMARY KEY (user_id, institution_id)
);

CREATE INDEX idx_user_institutions_user ON user_institutions (user_id);

-- Refresh token blacklist (for logout / token rotation)
-- Stores hash of token to avoid storing secrets in plaintext
CREATE TABLE refresh_token_blacklist (
  token_hash  VARCHAR(64) PRIMARY KEY,      -- SHA-256 hex of the refresh token
  user_id     UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  expires_at  TIMESTAMPTZ NOT NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_rtbl_user ON refresh_token_blacklist (user_id);
CREATE INDEX idx_rtbl_expires ON refresh_token_blacklist (expires_at);


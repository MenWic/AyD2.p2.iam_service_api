-- Migrate user_roles.role from PostgreSQL custom enum to VARCHAR.
-- This resolves the Hibernate 6 type-binding mismatch with @Enumerated(EnumType.STRING).
-- DB-level validation is preserved via CHECK constraint.

ALTER TABLE user_roles
    ALTER COLUMN role TYPE VARCHAR(50) USING role::VARCHAR;

ALTER TABLE user_roles
ADD CONSTRAINT chk_user_role CHECK (
    role IN (
        'SYSTEM_ADMIN',
        'CONGRESS_ADMIN',
        'PARTICIPANT',
        'GUEST_SPEAKER'
    )
);

DROP TYPE IF EXISTS user_role;
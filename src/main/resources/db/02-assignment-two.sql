-- Run against the existing bài 1 database after taking a backup.
-- Existing passwords, roles and user rows are retained.
IF COL_LENGTH('dbo.users', 'username') IS NULL
    ALTER TABLE dbo.users ADD username VARCHAR(50) NULL;
IF COL_LENGTH('dbo.users', 'images') IS NULL
    ALTER TABLE dbo.users ADD images VARCHAR(500) NULL;

-- Stable unique usernames for users created before bài 2. Rename individually if desired.
UPDATE dbo.users SET username = CONCAT('user', id) WHERE username IS NULL;

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.users')
           AND name = 'username' AND is_nullable = 1)
    ALTER TABLE dbo.users ALTER COLUMN username VARCHAR(50) NOT NULL;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('dbo.users')
               AND name = 'uk_users_username')
    CREATE UNIQUE INDEX uk_users_username ON dbo.users(username);
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE parent_object_id = OBJECT_ID('dbo.users')
               AND name = 'ck_users_username_no_at')
    ALTER TABLE dbo.users ADD CONSTRAINT ck_users_username_no_at CHECK (username NOT LIKE '%@%');

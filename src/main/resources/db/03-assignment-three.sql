-- Apply to hnhstore_login after 01 + 02. Existing users, roles and passwords are untouched.
IF OBJECT_ID('dbo.products', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.products (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(200) NOT NULL,
        description NVARCHAR(1000) NULL,
        price DECIMAL(18,2) NOT NULL,
        image_url VARCHAR(1000) NULL,
        image_public_id VARCHAR(500) NULL,
        created_at DATETIME2(6) NOT NULL,
        user_id BIGINT NOT NULL,
        CONSTRAINT fk_products_user FOREIGN KEY (user_id) REFERENCES dbo.users(id),
        CONSTRAINT ck_products_price CHECK (price >= 0)
    );
    CREATE INDEX idx_products_name ON dbo.products(name);
    CREATE INDEX idx_products_user_id ON dbo.products(user_id);
END;
GO
IF OBJECT_ID('dbo.otp_tokens', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.otp_tokens (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        email VARCHAR(150) NOT NULL,
        type VARCHAR(30) NOT NULL,
        otp_hash VARCHAR(100) NOT NULL,
        expires_at DATETIME2(6) NOT NULL,
        created_at DATETIME2(6) NOT NULL,
        attempts INT NOT NULL,
        used BIT NOT NULL,
        version BIGINT NOT NULL,
        CONSTRAINT ck_otp_type CHECK (type IN ('REGISTER','RESET')),
        CONSTRAINT ck_otp_attempts CHECK (attempts >= 0)
    );
    CREATE INDEX idx_otp_email_type_created ON dbo.otp_tokens(email, type, created_at DESC);
END;

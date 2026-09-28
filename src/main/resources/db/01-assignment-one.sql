-- Run once against a new hnhstore_login database. No passwords are stored in this script.
CREATE TABLE roles (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE
);
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name NVARCHAR(200) NOT NULL,
    enabled BIT NOT NULL,
    role_id BIGINT NOT NULL REFERENCES roles(id)
);

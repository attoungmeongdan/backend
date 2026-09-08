-- example
CREATE TABLE example
(
    id         BIGSERIAL    NOT NULL,
    name       VARCHAR(255) NOT NULL,
    is_deleted BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL,
    PRIMARY KEY (id)
);

-- users
CREATE TABLE users
(
    id          BIGSERIAL    NOT NULL,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255),
    nickname    VARCHAR(100) NOT NULL,
    provider    VARCHAR(20)  NOT NULL,
    provider_id VARCHAR(255),
    role        VARCHAR(20)  NOT NULL,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_provider UNIQUE (provider, provider_id)
);

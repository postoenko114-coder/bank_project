CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(255),
    password   VARCHAR(255),
    role_user  VARCHAR(50)         NOT NULL,
    email      VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP
);

CREATE TABLE support_messages
(
    id                     BIGSERIAL PRIMARY KEY,
    subject                VARCHAR(255),
    message                TEXT,
    user_email             VARCHAR(255),
    status_support_message VARCHAR(50) ,
    created_at             TIMESTAMP
);

CREATE TABLE services
(
    id                BIGSERIAL PRIMARY KEY,
    bank_service_name VARCHAR(255),
    duration          VARCHAR(50),
    description       TEXT
);

CREATE TABLE bank_branches
(
    id               BIGSERIAL PRIMARY KEY,
    bank_branch_name VARCHAR(255),
    latitude         DOUBLE PRECISION,
    longitude        DOUBLE PRECISION,
    city             VARCHAR(255),
    address          VARCHAR(255),
    country          VARCHAR(255),
    post_code        VARCHAR(255)
);


CREATE TABLE accounts
(
    id               BIGSERIAL PRIMARY KEY,
    account_number   VARCHAR(255),
    currency_account VARCHAR(50),
    balance          DECIMAL(15, 2),
    status_account   VARCHAR(50),
    user_id          BIGINT REFERENCES users (id),
    created_at       TIMESTAMP
);

CREATE TABLE cards
(
    id               BIGSERIAL PRIMARY KEY,
    card_number      VARCHAR(255),
    expiry_date      DATE,
    card_holder_name VARCHAR(255),
    status_card      VARCHAR(50),
    type_card        VARCHAR(50),
    account_id       BIGINT REFERENCES accounts (id),
    user_id          BIGINT REFERENCES users (id)
);

CREATE TABLE transactions
(
    id                 BIGSERIAL PRIMARY KEY,
    type_transaction   VARCHAR(50),
    status_transaction VARCHAR(50),
    amount             DECIMAL(15, 2),
    description        TEXT,
    account_from_id    BIGINT REFERENCES accounts (id),
    account_to_id      BIGINT REFERENCES accounts (id),
    created_at         TIMESTAMP,
    is_hidden          BOOLEAN DEFAULT FALSE
);

CREATE TABLE notifications
(
    id                  BIGSERIAL PRIMARY KEY,
    type_notification   VARCHAR(50),
    message             TEXT,
    status_notification VARCHAR(50),
    user_id             BIGINT REFERENCES users (id),
    created_at          TIMESTAMP
);

CREATE TABLE reservations
(
    id                BIGSERIAL PRIMARY KEY,
    start_reservation TIMESTAMP,
    end_reservation   TIMESTAMP,
    user_id           BIGINT REFERENCES users (id),
    bank_branch_id   BIGINT REFERENCES bank_branches (id),
    service_id        BIGINT REFERENCES services (id),
    status            VARCHAR(50)
);


CREATE TABLE bank_branch_bank_service
(
    bank_branch_id  BIGINT REFERENCES bank_branches (id),
    bank_service_id BIGINT REFERENCES services (id),
    PRIMARY KEY (bank_branch_id, bank_service_id)
);

CREATE TABLE branch_schedule
(
    branch_id  BIGINT REFERENCES bank_branches (id),
    day        VARCHAR(50),
    open_time  TIME,
    close_time TIME
);
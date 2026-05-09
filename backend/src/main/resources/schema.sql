CREATE TABLE IF NOT EXISTS category (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    label VARCHAR(64) NOT NULL,
    sort_order INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dish (
    id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    category_id VARCHAR(32) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    rating DECIMAL(3, 1) NOT NULL,
    calories INT NOT NULL DEFAULT 0,
    description VARCHAR(255) NOT NULL,
    highlight VARCHAR(128) NOT NULL,
    image_url VARCHAR(255) NULL,
    available TINYINT(1) NOT NULL DEFAULT 1,
    stock INT NOT NULL DEFAULT -1,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_dish_category FOREIGN KEY (category_id) REFERENCES category (id)
);

CREATE TABLE IF NOT EXISTS customer_order (
    id BIGINT NOT NULL PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    order_type VARCHAR(16) NOT NULL,
    note VARCHAR(60) NULL,
    table_number VARCHAR(32) NULL,
    pickup_number VARCHAR(32) NULL,
    contact_name VARCHAR(64) NULL,
    contact_phone VARCHAR(32) NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    package_fee DECIMAL(10, 2) NOT NULL,
    delivery_fee DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    item_count INT NOT NULL,
    status VARCHAR(16) NOT NULL,
    payment_status VARCHAR(16) NOT NULL DEFAULT 'UNPAID',
    cancel_reason VARCHAR(255) NULL,
    accepted_at DATETIME NULL,
    preparing_at DATETIME NULL,
    ready_at DATETIME NULL,
    completed_at DATETIME NULL,
    cancelled_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT NOT NULL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    dish_id BIGINT NOT NULL,
    dish_name VARCHAR(64) NOT NULL,
    dish_price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    line_total DECIMAL(10, 2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES customer_order (id),
    CONSTRAINT fk_order_item_dish FOREIGN KEY (dish_id) REFERENCES dish (id)
);

ALTER TABLE category
    ADD COLUMN IF NOT EXISTS created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE dish
    ADD COLUMN IF NOT EXISTS rating DECIMAL(3, 1) NOT NULL DEFAULT 4.8,
    ADD COLUMN IF NOT EXISTS calories INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS description VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS highlight VARCHAR(128) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS available TINYINT(1) NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS stock INT NOT NULL DEFAULT -1,
    ADD COLUMN IF NOT EXISTS deleted TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE customer_order
    ADD COLUMN IF NOT EXISTS table_number VARCHAR(32) NULL,
    ADD COLUMN IF NOT EXISTS pickup_number VARCHAR(32) NULL,
    ADD COLUMN IF NOT EXISTS contact_name VARCHAR(64) NULL,
    ADD COLUMN IF NOT EXISTS contact_phone VARCHAR(32) NULL,
    ADD COLUMN IF NOT EXISTS payment_status VARCHAR(16) NOT NULL DEFAULT 'UNPAID',
    ADD COLUMN IF NOT EXISTS cancel_reason VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS accepted_at DATETIME NULL,
    ADD COLUMN IF NOT EXISTS preparing_at DATETIME NULL,
    ADD COLUMN IF NOT EXISTS ready_at DATETIME NULL,
    ADD COLUMN IF NOT EXISTS completed_at DATETIME NULL,
    ADD COLUMN IF NOT EXISTS cancelled_at DATETIME NULL,
    ADD COLUMN IF NOT EXISTS created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE order_item
    ADD COLUMN IF NOT EXISTS created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX idx_category_sort_order ON category (sort_order);
CREATE INDEX idx_dish_sale_status ON dish (available, deleted, category_id);
CREATE INDEX idx_dish_category_id ON dish (category_id);
CREATE INDEX idx_customer_order_created_at ON customer_order (created_at);
CREATE INDEX idx_customer_order_status_created ON customer_order (status, created_at);
CREATE INDEX idx_customer_order_type_status_created ON customer_order (order_type, status, created_at);
CREATE INDEX idx_customer_order_table_number ON customer_order (table_number);
CREATE INDEX idx_customer_order_pickup_number ON customer_order (pickup_number);
CREATE INDEX idx_order_item_order_id ON order_item (order_id);
CREATE INDEX idx_order_item_dish_id ON order_item (dish_id);

CREATE TABLE IF NOT EXISTS order_status_log (
    id BIGINT NOT NULL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    order_no VARCHAR(32) NOT NULL,
    from_status VARCHAR(16) NULL,
    to_status VARCHAR(16) NOT NULL,
    reason VARCHAR(255) NULL,
    operator VARCHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_status_log_order FOREIGN KEY (order_id) REFERENCES customer_order (id)
);

CREATE INDEX idx_order_status_log_order_no ON order_status_log (order_no, created_at);

CREATE TABLE IF NOT EXISTS users (
    id            BIGINT       NOT NULL PRIMARY KEY,
    username      VARCHAR(64)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name  VARCHAR(64)  NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username ON users (username);

CREATE TABLE IF NOT EXISTS review (
    id         BIGINT       NOT NULL PRIMARY KEY,
    order_no   VARCHAR(32)  NOT NULL,
    dish_id    BIGINT       NOT NULL,
    rating     INT          NOT NULL,
    comment    VARCHAR(255) NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_order_no FOREIGN KEY (order_no) REFERENCES customer_order (order_no),
    CONSTRAINT fk_review_dish FOREIGN KEY (dish_id) REFERENCES dish (id)
);

CREATE INDEX idx_review_dish_id ON review (dish_id, created_at);
CREATE INDEX idx_review_order_no ON review (order_no, created_at);

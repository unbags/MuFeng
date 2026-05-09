package com.example.ordering.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Component
@Order(0)
public class DatabaseSchemaInitializer implements ApplicationRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaInitializer(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        createBaseTables();
        patchExistingTables();
        createIndexes();
    }

    private void createBaseTables() {
        execute("CREATE TABLE IF NOT EXISTS category ("
            + "id VARCHAR(32) NOT NULL PRIMARY KEY,"
            + "label VARCHAR(64) NOT NULL,"
            + "sort_order INT NOT NULL,"
            + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
            + "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
            + ")");

        execute("CREATE TABLE IF NOT EXISTS dish ("
            + "id BIGINT NOT NULL PRIMARY KEY,"
            + "name VARCHAR(64) NOT NULL,"
            + "category_id VARCHAR(32) NOT NULL,"
            + "price DECIMAL(10, 2) NOT NULL,"
            + "rating DECIMAL(3, 1) NOT NULL DEFAULT 4.8,"
            + "calories INT NOT NULL DEFAULT 0,"
            + "description VARCHAR(255) NOT NULL DEFAULT '',"
            + "highlight VARCHAR(128) NOT NULL DEFAULT '',"
            + "image_url VARCHAR(255) NULL,"
            + "available TINYINT(1) NOT NULL DEFAULT 1,"
            + "stock INT NOT NULL DEFAULT -1,"
            + "deleted TINYINT(1) NOT NULL DEFAULT 0,"
            + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
            + "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
            + ")");

        execute("CREATE TABLE IF NOT EXISTS customer_order ("
            + "id BIGINT NOT NULL PRIMARY KEY,"
            + "order_no VARCHAR(32) NOT NULL UNIQUE,"
            + "order_type VARCHAR(16) NOT NULL,"
            + "note VARCHAR(60) NULL,"
            + "table_number VARCHAR(32) NULL,"
            + "pickup_number VARCHAR(32) NULL,"
            + "contact_name VARCHAR(64) NULL,"
            + "contact_phone VARCHAR(32) NULL,"
            + "subtotal DECIMAL(10, 2) NOT NULL,"
            + "package_fee DECIMAL(10, 2) NOT NULL,"
            + "delivery_fee DECIMAL(10, 2) NOT NULL,"
            + "total_amount DECIMAL(10, 2) NOT NULL,"
            + "item_count INT NOT NULL,"
            + "status VARCHAR(16) NOT NULL,"
            + "payment_status VARCHAR(16) NOT NULL DEFAULT 'UNPAID',"
            + "cancel_reason VARCHAR(255) NULL,"
            + "accepted_at DATETIME NULL,"
            + "preparing_at DATETIME NULL,"
            + "ready_at DATETIME NULL,"
            + "completed_at DATETIME NULL,"
            + "cancelled_at DATETIME NULL,"
            + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
            + "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
            + ")");

        execute("CREATE TABLE IF NOT EXISTS order_item ("
            + "id BIGINT NOT NULL PRIMARY KEY,"
            + "order_id BIGINT NOT NULL,"
            + "dish_id BIGINT NOT NULL,"
            + "dish_name VARCHAR(64) NOT NULL,"
            + "dish_price DECIMAL(10, 2) NOT NULL,"
            + "quantity INT NOT NULL,"
            + "line_total DECIMAL(10, 2) NOT NULL,"
            + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP"
            + ")");

        execute("CREATE TABLE IF NOT EXISTS users ("
            + "id BIGINT NOT NULL PRIMARY KEY,"
            + "username VARCHAR(64) NOT NULL UNIQUE,"
            + "password_hash VARCHAR(255) NOT NULL,"
            + "display_name VARCHAR(64) NOT NULL,"
            + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
            + "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
            + ")");

        execute("CREATE TABLE IF NOT EXISTS review ("
            + "id BIGINT NOT NULL PRIMARY KEY,"
            + "order_no VARCHAR(32) NOT NULL,"
            + "dish_id BIGINT NOT NULL,"
            + "rating INT NOT NULL,"
            + "comment VARCHAR(255) NULL,"
            + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP"
            + ")");

        execute("CREATE TABLE IF NOT EXISTS order_status_log ("
            + "id BIGINT NOT NULL PRIMARY KEY,"
            + "order_id BIGINT NOT NULL,"
            + "order_no VARCHAR(32) NOT NULL,"
            + "from_status VARCHAR(16) NULL,"
            + "to_status VARCHAR(16) NOT NULL,"
            + "reason VARCHAR(255) NULL,"
            + "operator VARCHAR(64) NULL,"
            + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP"
            + ")");
    }

    private void patchExistingTables() throws SQLException {
        addColumnIfMissing("category", "created_at", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        addColumnIfMissing("category", "updated_at", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");

        addColumnIfMissing("dish", "rating", "DECIMAL(3, 1) NOT NULL DEFAULT 4.8");
        addColumnIfMissing("dish", "calories", "INT NOT NULL DEFAULT 0");
        addColumnIfMissing("dish", "description", "VARCHAR(255) NOT NULL DEFAULT ''");
        addColumnIfMissing("dish", "highlight", "VARCHAR(128) NOT NULL DEFAULT ''");
        addColumnIfMissing("dish", "image_url", "VARCHAR(255) NULL");
        addColumnIfMissing("dish", "available", "TINYINT(1) NOT NULL DEFAULT 1");
        addColumnIfMissing("dish", "stock", "INT NOT NULL DEFAULT -1");
        addColumnIfMissing("dish", "deleted", "TINYINT(1) NOT NULL DEFAULT 0");
        addColumnIfMissing("dish", "created_at", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        addColumnIfMissing("dish", "updated_at", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
        execute("UPDATE dish SET stock = -1 WHERE stock IS NULL");
        execute("ALTER TABLE dish MODIFY COLUMN stock INT NOT NULL DEFAULT -1");
        execute("UPDATE dish SET stock = -1 WHERE id BETWEEN 1 AND 12 AND stock = 0 AND available = 1");

        addColumnIfMissing("customer_order", "created_at", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        addColumnIfMissing("customer_order", "updated_at", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
        addColumnIfMissing("customer_order", "table_number", "VARCHAR(32) NULL");
        addColumnIfMissing("customer_order", "pickup_number", "VARCHAR(32) NULL");
        addColumnIfMissing("customer_order", "contact_name", "VARCHAR(64) NULL");
        addColumnIfMissing("customer_order", "contact_phone", "VARCHAR(32) NULL");
        addColumnIfMissing("customer_order", "payment_status", "VARCHAR(16) NOT NULL DEFAULT 'UNPAID'");
        addColumnIfMissing("customer_order", "cancel_reason", "VARCHAR(255) NULL");
        addColumnIfMissing("customer_order", "accepted_at", "DATETIME NULL");
        addColumnIfMissing("customer_order", "preparing_at", "DATETIME NULL");
        addColumnIfMissing("customer_order", "ready_at", "DATETIME NULL");
        addColumnIfMissing("customer_order", "completed_at", "DATETIME NULL");
        addColumnIfMissing("customer_order", "cancelled_at", "DATETIME NULL");

        addColumnIfMissing("order_item", "created_at", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
    }

    private void createIndexes() throws SQLException {
        createIndexIfMissing("category", "idx_category_sort_order", "CREATE INDEX idx_category_sort_order ON category (sort_order)");
        createIndexIfMissing("dish", "idx_dish_sale_status", "CREATE INDEX idx_dish_sale_status ON dish (available, deleted, category_id)");
        createIndexIfMissing("dish", "idx_dish_category_id", "CREATE INDEX idx_dish_category_id ON dish (category_id)");
        createIndexIfMissing("customer_order", "idx_customer_order_created_at", "CREATE INDEX idx_customer_order_created_at ON customer_order (created_at)");
        createIndexIfMissing("customer_order", "idx_customer_order_status_created", "CREATE INDEX idx_customer_order_status_created ON customer_order (status, created_at)");
        createIndexIfMissing("customer_order", "idx_customer_order_type_status_created", "CREATE INDEX idx_customer_order_type_status_created ON customer_order (order_type, status, created_at)");
        createIndexIfMissing("customer_order", "idx_customer_order_table_number", "CREATE INDEX idx_customer_order_table_number ON customer_order (table_number)");
        createIndexIfMissing("customer_order", "idx_customer_order_pickup_number", "CREATE INDEX idx_customer_order_pickup_number ON customer_order (pickup_number)");
        createIndexIfMissing("order_item", "idx_order_item_order_id", "CREATE INDEX idx_order_item_order_id ON order_item (order_id)");
        createIndexIfMissing("order_item", "idx_order_item_dish_id", "CREATE INDEX idx_order_item_dish_id ON order_item (dish_id)");
        createIndexIfMissing("users", "idx_users_username", "CREATE INDEX idx_users_username ON users (username)");
        createIndexIfMissing("review", "idx_review_dish_id", "CREATE INDEX idx_review_dish_id ON review (dish_id, created_at)");
        createIndexIfMissing("review", "idx_review_order_no", "CREATE INDEX idx_review_order_no ON review (order_no, created_at)");
        createIndexIfMissing("order_status_log", "idx_order_status_log_order_no", "CREATE INDEX idx_order_status_log_order_no ON order_status_log (order_no, created_at)");
    }

    private void addColumnIfMissing(String tableName, String columnName, String columnDefinition) throws SQLException {
        if (!columnExists(tableName, columnName)) {
            execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
        }
    }

    private boolean columnExists(String tableName, String columnName) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            List<String> names = Arrays.asList(tableName, tableName.toUpperCase(), tableName.toLowerCase());
            for (String name : names) {
                try (ResultSet columns = metaData.getColumns(connection.getCatalog(), null, name, columnName)) {
                    if (columns.next()) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private void createIndexIfMissing(String tableName, String indexName, String ddl) throws SQLException {
        if (!indexExists(tableName, indexName)) {
            execute(ddl);
        }
    }

    private boolean indexExists(String tableName, String indexName) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            List<String> names = Arrays.asList(tableName, tableName.toUpperCase(), tableName.toLowerCase());
            for (String name : names) {
                try (ResultSet indexes = metaData.getIndexInfo(connection.getCatalog(), null, name, false, false)) {
                    while (indexes.next()) {
                        if (indexName.equalsIgnoreCase(indexes.getString("INDEX_NAME"))) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    private void execute(String sql) {
        jdbcTemplate.execute(sql);
    }
}

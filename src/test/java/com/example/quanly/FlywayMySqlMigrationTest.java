package com.example.quanly;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class FlywayMySqlMigrationTest {
    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("pickleball_flyway_test")
            .withUsername("test")
            .withPassword("test");

    @Test
    void allMigrationsApplyToRealMySqlAndReachVersion4() throws Exception {
        Flyway flyway = Flyway.configure()
                .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                .locations("classpath:db/migration")
                .validateMigrationNaming(true)
                .load();

        MigrateResult result = flyway.migrate();

        assertTrue(result.success);
        assertEquals("4", flyway.info().current().getVersion().getVersion());
        try (Connection connection = MYSQL.createConnection("");
             ResultSet columns = connection.getMetaData().getColumns(
                     MYSQL.getDatabaseName(), null, "user", "active")) {
            assertTrue(columns.next(), "V1 phải tạo cột user.active trên MySQL thật");
        }
        try (Connection connection = MYSQL.createConnection("");
             ResultSet columns = connection.getMetaData().getColumns(
                     MYSQL.getDatabaseName(), null, "pending_booking_payment",
                     "temporary_booking_ids_json")) {
            assertTrue(columns.next(),
                    "V1 phải tạo cột pending_booking_payment.temporary_booking_ids_json");
        }
    }
}

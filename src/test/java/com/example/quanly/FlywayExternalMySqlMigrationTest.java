package com.example.quanly;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfSystemProperty(named = "flyway.test.url", matches = ".+")
class FlywayExternalMySqlMigrationTest {
    @Test
    void allMigrationsApplyToConfiguredRealMySql() throws Exception {
        String url = System.getProperty("flyway.test.url");
        String username = System.getProperty("flyway.test.username", "root");
        String password = System.getProperty("flyway.test.password", "");

        Flyway flyway = Flyway.configure()
                .dataSource(url, username, password)
                .locations("classpath:db/migration")
                .validateMigrationNaming(true)
                .load();

        MigrateResult result = flyway.migrate();

        assertTrue(result.success);
        assertEquals("4", flyway.info().current().getVersion().getVersion());
        try (Connection connection = DriverManager.getConnection(url, username, password);
             ResultSet columns = connection.getMetaData().getColumns(
                     connection.getCatalog(), null, "user", "active")) {
            assertTrue(columns.next(), "V1 phải tạo cột user.active trên MySQL thật");
        }
        try (Connection connection = DriverManager.getConnection(url, username, password);
             ResultSet columns = connection.getMetaData().getColumns(
                     connection.getCatalog(), null, "pending_booking_payment",
                     "temporary_booking_ids_json")) {
            assertTrue(columns.next(),
                    "V1 phải tạo cột pending_booking_payment.temporary_booking_ids_json");
        }
    }
}

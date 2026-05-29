package com.jj.jig.auth;

import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Removes H2 CHECK constraints on enum columns that block new enum values.
 * Uses ALTER COLUMN to re-define the column without any inline CHECK constraint.
 * Runs before DataInitializer so the DB is ready when the app starts.
 */
@Component
@Order(0)
public class DbEnumMigration implements org.springframework.boot.CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DbEnumMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        // Re-define ACTION_TYPE as plain VARCHAR(30) — drops any inline CHECK constraint.
        tryExec("ALTER TABLE JIG_LOGS ALTER COLUMN ACTION_TYPE VARCHAR(30) NOT NULL");

        // Also drop any named CHECK constraints that Hibernate may have created separately.
        dropNamedCheckConstraints("JIG_LOGS");
    }

    private void tryExec(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception ignored) {}
    }

    private void dropNamedCheckConstraints(String tableName) {
        // H2 2.x: TABLE_CONSTRAINTS view
        try {
            List<String> names = jdbcTemplate.queryForList(
                    "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS " +
                    "WHERE UPPER(TABLE_NAME) = ? AND CONSTRAINT_TYPE = 'CHECK'",
                    String.class, tableName.toUpperCase()
            );
            for (String name : names) {
                tryExec("ALTER TABLE " + tableName + " DROP CONSTRAINT IF EXISTS \"" + name + "\"");
            }
        } catch (Exception ignored) {}
    }
}

package com.inventorymanagement.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Database Explorer Controller for development and debugging purposes. This controller is only available in the dev profile.
 */
@RestController
@RequestMapping("/database")
@Profile("dev")
@Tag(name = "Database Explorer", description = "Database exploration endpoints (Development only)")
public class DatabaseExplorerController {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseExplorerController(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @GetMapping("/info")
    @Operation(summary = "Get database information", description = "Returns basic database metadata")
    public ResponseEntity<Map<String, Object>> getDatabaseInfo() {
        Map<String, Object> info = new HashMap<>();

        try {
            DatabaseMetaData metaData = jdbcTemplate.getDataSource().getConnection().getMetaData();
            info.put("databaseProductName", metaData.getDatabaseProductName());
            info.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            info.put("driverName", metaData.getDriverName());
            info.put("driverVersion", metaData.getDriverVersion());
            info.put("url", metaData.getURL());
        } catch (Exception e) {
            info.put("error", e.getMessage());
        }

        return ResponseEntity.ok(info);
    }

    @GetMapping("/tables")
    @Operation(
            summary = "List all tables",
            description = "Returns a list of all tables in the database")
    public ResponseEntity<List<Map<String, Object>>> getTables() {
        List<Map<String, Object>> tables = new ArrayList<>();

        try {
            DatabaseMetaData metaData = jdbcTemplate.getDataSource().getConnection().getMetaData();
            ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            while (rs.next()) {
                Map<String, Object> table = new HashMap<>();
                String tableName = rs.getString("TABLE_NAME");
                table.put("tableName", tableName);

                // Get row count for each table
                try {
                    Integer count =
                            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
                    table.put("rowCount", count);
                } catch (Exception e) {
                    table.put("rowCount", "Error: " + e.getMessage());
                }

                tables.add(table);
            }
            rs.close();
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            tables.add(error);
        }

        return ResponseEntity.ok(tables);
    }

    @GetMapping("/tables/{tableName}/schema")
    @Operation(summary = "Get table schema", description = "Returns the schema of a specific table")
    public ResponseEntity<List<Map<String, Object>>> getTableSchema(
            @Parameter(description = "Name of the table") @PathVariable final String tableName) {
        List<Map<String, Object>> columns = new ArrayList<>();

        try {
            DatabaseMetaData metaData = jdbcTemplate.getDataSource().getConnection().getMetaData();
            ResultSet rs = metaData.getColumns(null, null, tableName, null);

            while (rs.next()) {
                Map<String, Object> column = new HashMap<>();
                column.put("columnName", rs.getString("COLUMN_NAME"));
                column.put("dataType", rs.getString("TYPE_NAME"));
                column.put("columnSize", rs.getInt("COLUMN_SIZE"));
                column.put("nullable", rs.getString("IS_NULLABLE"));
                column.put("autoIncrement", rs.getString("IS_AUTOINCREMENT"));
                column.put("defaultValue", rs.getString("COLUMN_DEF"));
                columns.add(column);
            }
            rs.close();
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            columns.add(error);
        }

        return ResponseEntity.ok(columns);
    }

    @GetMapping("/tables/{tableName}/data")
    @Operation(
            summary = "Get table data",
            description = "Returns data from a specific table with pagination")
    public ResponseEntity<Map<String, Object>> getTableData(
            @Parameter(description = "Name of the table") @PathVariable final String tableName,
            @Parameter(description = "Limit number of rows") @RequestParam(defaultValue = "10") final int limit,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") final int offset) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Get total count
            Integer totalCount =
                    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
            result.put("totalRows", totalCount);

            // Get data with pagination
            String query = String.format("SELECT * FROM %s LIMIT ? OFFSET ?", tableName);
            List<Map<String, Object>> data = jdbcTemplate.queryForList(query, limit, offset);
            result.put("data", data);
            result.put("limit", limit);
            result.put("offset", offset);

        } catch (Exception e) {
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/query")
    @Operation(
            summary = "Execute custom query",
            description = "Execute a custom SQL query (SELECT only)")
    public ResponseEntity<Map<String, Object>> executeQuery(
            @Parameter(description = "SQL query to execute") @RequestBody final Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();

        String query = request.get("query");
        if (query == null || query.trim().isEmpty()) {
            result.put("error", "Query is required");
            return ResponseEntity.badRequest().body(result);
        }

        // Only allow SELECT queries for safety
        if (!query.trim().toUpperCase().startsWith("SELECT")) {
            result.put("error", "Only SELECT queries are allowed");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            List<Map<String, Object>> data = jdbcTemplate.queryForList(query);
            result.put("data", data);
            result.put("rowCount", data.size());
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}

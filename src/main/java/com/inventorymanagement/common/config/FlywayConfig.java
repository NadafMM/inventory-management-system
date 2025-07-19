package com.inventorymanagement.common.config;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class for Flyway database migrations.
 *
 * <p>This configuration ensures that database migrations are properly executed and validated before
 * the application starts. It provides custom configuration for different environments and handles SQLite-specific requirements.
 *
 * <p>Key features: - Baseline on migrate for existing databases - Validation of migration scripts -
 * Environment-specific configuration - Proper dependency management
 *
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = true)
public class FlywayConfig {

    private static final Logger logger = LoggerFactory.getLogger(FlywayConfig.class);

    private final DataSource dataSource;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    /**
     * Constructs a new FlywayConfig with the specified DataSource.
     *
     * @param dataSource the DataSource to use for migrations
     */
    @Autowired
    public FlywayConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Creates and configures a Flyway instance for database migrations.
     *
     * <p>This bean is created early in the application lifecycle to ensure migrations are executed
     * before JPA entities are processed.
     *
     * @return configured Flyway instance
     */
    @Bean(initMethod = "migrate")
    @DependsOn("dataSource")
    public Flyway flyway() {
        Flyway flyway =
                Flyway.configure()
                        .dataSource(dataSource)
                        .locations("classpath:db/migration")
                        .baselineOnMigrate(true)
                        .baselineVersion("0")
                        .validateOnMigrate(true)
                        .outOfOrder(false)
                        .cleanDisabled(true) // Disable clean for safety
                        .sqlMigrationPrefix("V")
                        .sqlMigrationSeparator("__")
                        .sqlMigrationSuffixes(".sql")
                        .repeatableSqlMigrationPrefix("R")
                        .load();

        // In development, repair the schema history if there are checksum mismatches
        if ("dev".equals(activeProfile)) {
            try {
                logger.info("Running Flyway repair in development mode to fix checksum mismatches");
                flyway.repair();
            } catch (Exception e) {
                logger.warn("Flyway repair failed, continuing with migration: {}", e.getMessage());
            }
        }

        return flyway;
    }

    /**
     * Development-only bean that repairs Flyway schema history. This is useful when migration files are modified during development.
     */
    @Bean
    @Profile("dev")
    @DependsOn("dataSource")
    public Flyway flywayRepair() {
        logger.info("Creating Flyway repair bean for development environment");
        Flyway flyway =
                Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load();

        // Repair the schema history table
        flyway.repair();

        return flyway;
    }
}

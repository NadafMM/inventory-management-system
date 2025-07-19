package com.inventorymanagement.common.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Database configuration for the Inventory Management System.
 *
 * <p>This configuration class sets up the database connection pool using HikariCP.
 *
 * @version 1.0.0
 * @since 2025-01-15
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url}")
    private String dataSourceUrl;

    @Value("${spring.datasource.username}")
    private String dataSourceUsername;

    @Value("${spring.datasource.password}")
    private String dataSourcePassword;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    /**
     * Configures the primary DataSource with HikariCP connection pool.
     *
     * @return configured DataSource
     */
    @Bean
    @Primary
    @ConditionalOnProperty(
            prefix = "spring.datasource",
            name = "url",
            havingValue = "",
            matchIfMissing = true)
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        // Basic configuration
        config.setJdbcUrl(dataSourceUrl);
        config.setUsername(dataSourceUsername);
        config.setPassword(dataSourcePassword);
        config.setDriverClassName(driverClassName);

        // Connection pool configuration
        config.setPoolName("InventoryHikariCP");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(300000); // 5 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        config.setLeakDetectionThreshold(60000); // 1 minute

        // Performance optimizations
        config.setAutoCommit(true);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        logger.info("Configuring HikariCP DataSource for: {}", dataSourceUrl);

        return new HikariDataSource(config);
    }

    /**
     * Configures the auditor aware bean for JPA auditing.
     *
     * @return AuditorAware that provides the current auditor
     */
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of("SYSTEM");
    }
}

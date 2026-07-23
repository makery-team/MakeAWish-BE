package org.makery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseFixConfig {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return properties -> {
            try (Connection conn = dataSource.getConnection()) {
                String url = conn.getMetaData().getURL();
                if (url != null && url.toLowerCase().contains("h2")) {
                    log.info("H2 Database detected. Enabling Hibernate ddl-auto: update for local dev");
                    properties.put("hibernate.hbm2ddl.auto", "update");
                }
            } catch (SQLException e) {
                log.warn("Failed to determine DB type for ddl-auto", e);
            }
        };
    }

    @PostConstruct
    public void init() {
        try (Connection conn = dataSource.getConnection()) {
            String url = conn.getMetaData().getURL();
            // MySQL 환경(Production)에서만 수동 스키마 초기화 진행 (H2는 위에서 ddl-auto로 자동 처리됨)
            if (url != null && url.toLowerCase().contains("mysql")) {
                log.info("MySQL Database detected. Executing safe table initialization...");
                
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS portfolios (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "title VARCHAR(255), " +
                        "description TEXT, " +
                        "image_url VARCHAR(255) NOT NULL, " +
                        "product_id BIGINT, " +
                        "store_id BIGINT, " +
                        "is_inpainting_allowed BOOLEAN DEFAULT TRUE, " +
                        "like_count INT DEFAULT 0, " +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "modified_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                        ")");
                        
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS portfolio_tags (" +
                        "portfolio_id BIGINT, " +
                        "tag_id BIGINT, " +
                        "PRIMARY KEY(portfolio_id, tag_id)" +
                        ")");

                executeConstraintWithLogging("ALTER TABLE payments ADD CONSTRAINT UK_payments_order_id UNIQUE (order_id)");
                executeConstraintWithLogging("ALTER TABLE refresh_token ADD CONSTRAINT UK_rt_user_id UNIQUE (user_id)");
                executeConstraintWithLogging("ALTER TABLE reviews ADD CONSTRAINT UK_reviews_order_id UNIQUE (order_id)");
                
                log.info("Safe table initialization completed.");
            }
        } catch (Exception e) {
            log.error("Error during manual DB init", e);
            throw new RuntimeException("Database initialization failed!", e);
        }
    }

    private void executeConstraintWithLogging(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            String msg = e.getMessage();
            // MySQL duplicate key error code is 1061
            if (msg != null && (msg.contains("Duplicate") || msg.contains("1061"))) {
                log.info("Constraint already exists, skipping: {}", sql);
            } else {
                log.error("Failed to execute constraint: {} - Reason: {}", sql, msg);
                throw new RuntimeException("Failed to add required DB constraint", e);
            }
        }
    }
}

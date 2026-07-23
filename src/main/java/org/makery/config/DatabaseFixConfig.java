package org.makery.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseFixConfig {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        try {
            log.info("Executing safe table initialization...");
            
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

            // Ignore duplicate constraint errors if they already exist
            try { jdbcTemplate.execute("ALTER TABLE payments ADD CONSTRAINT UK_payments_order_id UNIQUE (order_id)"); } catch(Exception e) {}
            try { jdbcTemplate.execute("ALTER TABLE refresh_token ADD CONSTRAINT UK_rt_user_id UNIQUE (user_id)"); } catch(Exception e) {}
            try { jdbcTemplate.execute("ALTER TABLE reviews ADD CONSTRAINT UK_reviews_order_id UNIQUE (order_id)"); } catch(Exception e) {}
            
            log.info("Safe table initialization completed.");
        } catch (Exception e) {
            log.error("Error during manual DB init", e);
        }
    }
}

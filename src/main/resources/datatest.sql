SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE ai_agent_messages;
TRUNCATE TABLE ai_inpainted_designs;
TRUNCATE TABLE notifications;
TRUNCATE TABLE likes;
TRUNCATE TABLE portfolio_tags;
TRUNCATE TABLE reviews;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE portfolios;
TRUNCATE TABLE products;
TRUNCATE TABLE categories;
TRUNCATE TABLE stores;
TRUNCATE TABLE seller_profiles;
TRUNCATE TABLE tags;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. 유저 (참조 대상)
INSERT INTO users (id, email, name, nickname, user_role, o_auth_provider, created_at, modified_at)
VALUES (1, 'admin@test.com', '관리자', '메이커리마스터', 'ROLE_ADMIN', 'GOOGLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (2, 'user1@test.com', '홍길동', '빵순이', 'ROLE_USER', 'KAKAO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2. 셀러 프로필 & 매장
INSERT INTO seller_profiles (id, user_id, business_no, status, created_at, modified_at)
VALUES (1, 1, '123-45-67890', 'VERIFIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO stores (id, name, description, latitude, longitude, rating, review_count, seller_profile_id, created_at, modified_at)
VALUES (1, '메이커리 강남점', '레터링 케이크 전문점', 37.5665, 126.9780, 4.5, 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 3. 제품 (반드시 여기 먼저 등록되어야 order_items가 참조함)
INSERT INTO products (id, name, price, description, is_available, store_id, order_schema, created_at, modified_at)
VALUES (1, '도시락 케이크', 15000, '미니 케이크', TRUE, 1, '{"templates": []}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (2, '레터링 케이크', 35000, '시그니처 케이크', TRUE, 1, '{"templates": []}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 4. 태그 & 포트폴리오
INSERT INTO tags (id, name) VALUES (1, '초코'), (2, '생일');
INSERT INTO portfolios (id, title, description, image_url, is_inpainting_allowed, like_count, store_id, product_id, created_at, modified_at)
VALUES (1, '기본 초코 케이크', '인기 모델', 'https://image.idus.com/image/files/10b6568597594967a777aa61d1a05683_400.jpg', TRUE, 0, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO portfolio_tags (portfolio_id, tag_id) VALUES (1, 1), (1, 2);

-- 5. 주문 (데이터 존재 확인 후 진행)
INSERT INTO orders (id, order_number, status, pickup_date, total_price, order_data, user_id, store_id, created_at, modified_at)
VALUES (1, 'ORD-2026-001', 'COMPLETED', CURRENT_TIMESTAMP, 15000, '{}', 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 6. 주문 항목 (반드시 products와 portfolios에 존재하는 ID만 사용)
INSERT INTO order_items (id, order_id, product_id, portfolio_id, name, quantity, unit_price)
VALUES (1, 1, 1, 1, '도시락 케이크', 1, 15000);

-- 시퀀스 초기화
ALTER TABLE users AUTO_INCREMENT = 10;
ALTER TABLE stores AUTO_INCREMENT = 10;
ALTER TABLE products AUTO_INCREMENT = 10;
ALTER TABLE portfolios AUTO_INCREMENT = 10;
ALTER TABLE orders AUTO_INCREMENT = 10;
ALTER TABLE order_items AUTO_INCREMENT = 10;
-- 1. 제약 조건 잠시 끄기 (MySQL 방식)
SET FOREIGN_KEY_CHECKS = 0;

-- 2. 기존 데이터 삭제
DELETE FROM ai_agent_messages;
DELETE FROM ai_inpainted_designs;
DELETE FROM portfolio_tags;
DELETE FROM portfolios;
DELETE FROM reviews;
DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM products;
DELETE FROM stores;
DELETE FROM seller_profiles;
DELETE FROM users;
DELETE FROM tags;

-- 3. 제약 조건 다시 켜기 (MySQL 방식)
SET FOREIGN_KEY_CHECKS = 1;

-- 4. 유저 데이터 (💡 사람 4명 추가)
-- 기존 관리자 (ID: 1)
INSERT INTO users (id, email, name, nickname, user_role, o_auth_provider, created_at, modified_at)
VALUES (1, 'admin@test.com', '관리자', '메이커리마스터', 'ROLE_ADMIN', 'GOOGLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 추가 유저 1 (일반 고객, 카카오 로그인)
INSERT INTO users (id, email, name, nickname, user_role, o_auth_provider, created_at, modified_at)
VALUES (2, 'user1@test.com', '홍길동', '빵순이', 'ROLE_USER', 'GOOGLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 추가 유저 2 (일반 고객, 네이버 로그인)
INSERT INTO users (id, email, name, nickname, user_role, o_auth_provider, created_at, modified_at)
VALUES (3, 'user2@test.com', '김철수', '케이크매니아', 'ROLE_USER', 'GOOGLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 추가 유저 3 (다른 셀러, 구글 로그인)
INSERT INTO users (id, email, name, nickname, user_role, o_auth_provider, created_at, modified_at)
VALUES (4, 'seller2@test.com', '이영희', '디저트장인', 'ROLE_SELLER', 'GOOGLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 추가 유저 4 (게스트, 카카오 로그인)
INSERT INTO users (id, email, name, nickname, user_role, o_auth_provider, created_at, modified_at)
VALUES (5, 'guest1@test.com', '박지민', '구경꾼', 'ROLE_GUEST', 'GOOGLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 5. 셀러 프로필
INSERT INTO seller_profiles (id, user_id, status, created_at, modified_at)
VALUES (1, 1, 'VERIFIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 6. 매장 데이터
INSERT INTO stores (id, name, description, latitude, longitude, rating, review_count, seller_profile_id, caution_notice, created_at, modified_at)
VALUES (1, '메이커리 강남점', '레터링 케이크 전문점입니다.', 37.5665, 126.9780, 4.5, 0, 1,
        '알러지 안내: 유제품 및 복숭아 알러지가 있으신 분은 상담 시 말씀해 주세요.',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 7. 제품 데이터 (💡 JSON 키워드 제거)
INSERT INTO products (id, name, price, description, is_available, store_id, order_schema, created_at, modified_at)
VALUES (1, '도시락 케이크', 15000, '아담한 사이즈의 커스텀 케이크', TRUE, 1,
        '{"templates": [{"label": "맛 선택", "name": "flavor", "type": "select", "required": true, "options": ["초코", "바닐라"]}]}',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 8. 포트폴리오 데이터
INSERT INTO portfolios (id, title, description, image_url, is_inpainting_allowed, like_count, store_id, product_id, created_at, modified_at)
VALUES (1, '기본 초코 케이크', '가장 인기 있는 모델입니다.', 'https://example.com/cake1.jpg', TRUE, 0, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 9. 태그 데이터
INSERT INTO tags (id, name) VALUES (1, '초코');
INSERT INTO tags (id, name) VALUES (2, '생일');

-- 10. 포트폴리오-태그 연결
INSERT INTO portfolio_tags (portfolio_id, tag_id) VALUES (1, 1);
INSERT INTO portfolio_tags (portfolio_id, tag_id) VALUES (1, 2);

-- 11. 주문 데이터 (💡 JSON 키워드 제거)
INSERT INTO orders (id, order_number, status, pickup_date, total_price, order_data, user_id, store_id, created_at, modified_at)
VALUES (1, 'ORD-2026-001', 'COMPLETED', '2026-04-20 15:30:00', 15000,
        '{"flavor": "초코", "문구": "축하해"}',
        1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 12. 주문 항목 상세
INSERT INTO order_items (id, order_id, product_id, portfolio_id, name, quantity, unit_price, customized_image_url)
VALUES (1, 1, 1, 1, '도시락 케이크', 1, 15000, 'https://example.com/ai-cake.jpg');

-- 13. 리뷰 데이터
INSERT INTO reviews (id, content, rating, store_id, order_id, created_at, modified_at)
VALUES (1, '케이크가 너무 예뻐요!', 5, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 14. AI 인페인팅 결과 데이터
INSERT INTO ai_inpainted_designs (id, before_image_url, after_image_url, inpainting_prompt, is_stored_in_album, origin_portfolio_id, user_id, created_at)
VALUES (1, 'https://example.com/cake1.jpg', 'https://example.com/ai-cake.jpg', '레터링 추가해줘', TRUE, 1, 1, CURRENT_TIMESTAMP);

-- 15. 시퀀스(Auto Increment) 시작값 재설정 (💡 MySQL 방식)
ALTER TABLE users AUTO_INCREMENT = 6;
ALTER TABLE orders AUTO_INCREMENT = 2;
ALTER TABLE order_items AUTO_INCREMENT = 2;
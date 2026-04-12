-- 1. 제약 조건 잠시 끄기
SET REFERENTIAL_INTEGRITY FALSE;

-- 2. 기존 데이터 삭제
DELETE FROM inpaintings;
DELETE FROM portfolio_tags;
DELETE FROM portfolios;
DELETE FROM reviews;
DELETE FROM orders;
DELETE FROM stores;
DELETE FROM seller_profiles;
DELETE FROM users;
DELETE FROM tags;

-- 3. 제약 조건 다시 켜기
SET REFERENTIAL_INTEGRITY TRUE;

-- 4. 유저 데이터 (이메일, 이름, 닉네임, 역할 등)
INSERT INTO users (id, email, name, nickname, user_role, o_auth_provider, created_at, modified_at)
VALUES (1, 'admin@test.com', '관리자', '메이커리마스터', 'ROLE_SELLER', 'GOOGLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 5. 셀러 프로필
INSERT INTO seller_profiles (id, user_id, status, created_at, modified_at)
VALUES (1, 1, 'VERIFIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 6. 매장 데이터 (order_schema 필드 추가)
INSERT INTO stores (id, name, description, latitude, longitude, rating, review_count, seller_profile_id, order_schema, created_at, modified_at)
VALUES (1, '메이커리 강남점', '레터링 케이크 전문점입니다.', 37.5665, 126.9780, 4.5, 0, 1,
        '{"templates": [{"label": "맛 선택", "key": "flavor", "type": "select", "options": ["초코", "바닐라"]}]}',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 7. 포트폴리오 데이터
INSERT INTO portfolios (id, title, description, image_url, is_inpainting_allowed, like_count, store_id, created_at, modified_at)
VALUES (1, '기본 초코 케이크', '가장 인기 있는 모델입니다.', 'https://example.com/cake1.jpg', TRUE, 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 8. 태그 데이터
INSERT INTO tags (id, name) VALUES (1, '초코');
INSERT INTO tags (id, name) VALUES (2, '생일');

-- 9. 포트폴리오-태그 연결
INSERT INTO portfolio_tags (portfolio_id, tag_id) VALUES (1, 1);
INSERT INTO portfolio_tags (portfolio_id, tag_id) VALUES (1, 2);

-- 10. 주문 데이터
INSERT INTO orders (id, order_number, status, pickup_date, total_price, order_data, user_id, store_id, created_at, modified_at)
VALUES (1, 'ORD-2026-001', 'COMPLETED', '2026-04-20 15:30:00', 35000, '{"맛": "초코", "문구": "축하해"}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 11. 리뷰 데이터
INSERT INTO reviews (id, content, rating, store_id, order_id, created_at, modified_at)
VALUES (1, '케이크가 너무 예뻐요!', 5, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 12. AI 결과 데이터 (Inpaintings)
INSERT INTO inpaintings (id, result_image_url, prompt, portfolio_id, user_id, created_at)
VALUES (1, 'https://example.com/ai-cake.jpg', '레터링 추가해줘', 1, 1, CURRENT_TIMESTAMP);
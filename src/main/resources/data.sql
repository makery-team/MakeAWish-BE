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

-- 4. 유저 데이터 (user_role, o_auth_provider 등 컬럼명 주의)
INSERT INTO users (id, email, name, nickname, user_role, o_auth_provider, created_at)
VALUES (1, 'admin@test.com', '관리자', '메이커리마스터', 'ROLE_SELLER', 'GOOGLE', CURRENT_TIMESTAMP);

-- 5. 셀러 프로필
INSERT INTO seller_profiles (id, user_id, status, created_at)
VALUES (1, 1, 'VERIFIED', CURRENT_TIMESTAMP);

-- 6. 매장 데이터 (order_schema 삽입 시 JSON '...' 문법 사용)
INSERT INTO stores (id, name, description, latitude, longitude, rating, review_count, seller_profile_id, order_schema, created_at)
VALUES (1, '메이커리 강남점', '레터링 케이크 전문점입니다.', 37.5665, 126.9780, 4.5, 0, 1,
        '{"size": ["도시락", "1호", "2호"], "flavors": ["초코", "바닐라"], "options": ["레터링"]}', CURRENT_TIMESTAMP);

-- 7. 포트폴리오 데이터
INSERT INTO portfolios (id, title, description, image_url, is_inpainting_allowed, store_id, created_at)
VALUES (1, '기본 초코 케이크', '가장 인기 있는 모델입니다.', 'https://example.com/cake1.jpg', TRUE, 1, CURRENT_TIMESTAMP);

-- 8. 태그 데이터 (멀티 행 삽입 에러 방지를 위해 분리)
INSERT INTO tags (id, name) VALUES (1, '초코');
INSERT INTO tags (id, name) VALUES (2, '생일');

-- 9. 포트폴리오-태그 연결
INSERT INTO portfolio_tags (portfolio_id, tag_id) VALUES (1, 1);
INSERT INTO portfolio_tags (portfolio_id, tag_id) VALUES (1, 2);

-- 10. 주문 데이터
INSERT INTO orders (id, order_number, status, total_price, user_id, store_id, created_at)
VALUES (1, 'ORD-2026-001', 'COMPLETED', 35000, 1, 1, CURRENT_TIMESTAMP);

-- 11. 리뷰 데이터 (💡 중요: 엔티티에 user_id, store_id가 없으므로 빼고 작성)
INSERT INTO reviews (id, content, rating, order_id, created_at)
VALUES (1, '케이크가 너무 예뻐요!', 5, 1, CURRENT_TIMESTAMP);

-- 12. AI 결과 데이터 (Inpaintings)
INSERT INTO inpaintings (id, result_image_url, prompt, portfolio_id, created_at)
VALUES (1, 'https://example.com/ai-cake.jpg', '레터링 추가해줘', 1, CURRENT_TIMESTAMP);
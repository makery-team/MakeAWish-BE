-- [1] 제약 조건 해제
SET FOREIGN_KEY_CHECKS = 0;

-- [2] 고정 유저 데이터 (이미 있으면 건너뜀)
INSERT IGNORE INTO users (id, email, name, nickname, user_role, o_auth_provider, created_at, modified_at)
VALUES
(1, 'admin@test.com', '관리자', '메이커리마스터', 'ROLE_ADMIN', 'GOOGLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'user1@test.com', '홍길동', '빵순이', 'ROLE_USER', 'KAKAO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'user2@test.com', '김철수', '케이크매니아', 'ROLE_USER', 'NAVER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'seller2@test.com', '이영희', '디저트장인', 'ROLE_SELLER', 'GOOGLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'guest1@test.com', '박지민', '구경꾼', 'ROLE_GUEST', 'KAKAO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- [3] 필수 기초 데이터 (셀러, 매장, 제품, 태그)
INSERT IGNORE INTO seller_profiles (id, user_id, status, created_at, modified_at) VALUES (1, 1, 'VERIFIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT IGNORE INTO stores (id, name, description, latitude, longitude, rating, review_count, seller_profile_id, caution_notice, created_at, modified_at)
VALUES (1, '메이커리 강남점', '레터링 케이크 전문점', 37.5665, 126.9780, 4.5, 0, 1, '알러지 주의', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT IGNORE INTO products (id, name, price, description, is_available, store_id, order_schema, created_at, modified_at)
VALUES (1, '도시락 케이크', 15000, '아담한 사이즈의 커스텀 케이크', TRUE, 1, '{"templates": []}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT IGNORE INTO tags (id, name) VALUES (1, '레터링케이크'), (2, '도시락케이크'), (3, '강아지'), (4, '생일'), (5, '기념일'), (6, '포토케이크'), (7, '로또'), (8, '퇴사'), (9, '지니'), (10, '커플');

-- [4] 7개 이미지 기반 포트폴리오 (ID 1~7 유지)
INSERT IGNORE INTO portfolios (id, title, description, image_url, is_inpainting_allowed, like_count, store_id, product_id, created_at, modified_at)
VALUES
(1, '댕댕이 입체 캐릭터', '강아지 디자인', 'https://image.idus.com/image/files/10b6568597594967a777aa61d1a05683_400.jpg', TRUE, 10, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '커플 데이트 포토', '커플 기념일', 'https://cdn.011st.com/11dims/resize/2000x2000/quality/75/11src/product/5740566358/B.jpg?478000000', FALSE, 20, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '로또 당첨 기원', '위트있는 선물', 'https://thumbnail.coupangcdn.com/thumbnails/remote/657x657q90trim/image/vendor_inventory/5111/973f67933f06a761fc8e81468dbacd56ece8bc9f905932bbc36d14885a89.png', TRUE, 15, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '퇴사 축하 시바', '퇴사 축하', 'https://thumbnail.coupangcdn.com/thumbnails/remote/492x492ex/image/vendor_inventory/e6ec/6f6935cbbd537d1e2768c56df673d8ab62221220ee03cd66a37c7ac43514.png', TRUE, 25, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, '아빠 생신 인물', '부모님 생신', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTOjzIyDGx9qzXEWDV3VJHvpL-Q1SjqvA9THw&s', TRUE, 30, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, '인스타 감성 프레임', '인스타 피드', 'https://cf.product-image.s.zigzag.kr/original/d/2024/7/9/35807_202407091029430092_44449.jpeg?width=400&height=400&quality=80&format=webp', FALSE, 40, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, '요정 지니 캐릭터', '캐릭터 홈파티', 'https://turtlehip.com/upload/product/110_1618413658_0.07%20%20(6%E2%98%85)', TRUE, 50, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- [5] 대량 데이터 누적 삽입 (포트폴리오 500개, 주문 200개씩 실행마다 추가)
INSERT INTO portfolios (title, description, image_url, is_inpainting_allowed, like_count, store_id, product_id, created_at, modified_at)
WITH RECURSIVE cnt AS (SELECT 1 AS n UNION ALL SELECT n + 1 FROM cnt WHERE n < 500)
SELECT CONCAT('대량데이터_PF_', n), '자동 생성된 테스트 데이터', 'https://example.com/cake.jpg', TRUE, FLOOR(RAND()*100), 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM cnt;

INSERT INTO orders (order_number, status, pickup_date, total_price, order_data, user_id, store_id, created_at, modified_at)
WITH RECURSIVE cnt AS (SELECT 1 AS n UNION ALL SELECT n + 1 FROM cnt WHERE n < 200)
SELECT CONCAT('ORD-', FLOOR(RAND()*9999999)), 'COMPLETED', CURRENT_TIMESTAMP, 15000, '{}', 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM cnt;

-- [6] AI 및 부가 데이터 (1번 로직 유지)
INSERT IGNORE INTO ai_agent_messages (id, user_id, message, sender_role, created_at, modified_at) VALUES (1, 1, '테스트 메시지', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT IGNORE INTO ai_inpainted_designs (id, before_image_url, after_image_url, inpainting_prompt, is_stored_in_album, origin_portfolio_id, user_id, created_at)
VALUES (1, 'https://example.com/cake1.jpg', 'https://example.com/ai-cake.jpg', '레터링 추가해줘', TRUE, 1, 1, CURRENT_TIMESTAMP);
INSERT IGNORE INTO reviews (id, content, rating, store_id, order_id, created_at, modified_at) VALUES (1, '케이크 정말 예뻐요!', 5, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- [7] 제약 조건 복구 및 시퀀스 보정
SET FOREIGN_KEY_CHECKS = 1;
ALTER TABLE users AUTO_INCREMENT = 100;
ALTER TABLE portfolios AUTO_INCREMENT = 5000;
ALTER TABLE orders AUTO_INCREMENT = 5000;
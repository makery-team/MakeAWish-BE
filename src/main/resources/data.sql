-- 1. 제약 조건 잠시 끄기
SET REFERENTIAL_INTEGRITY FALSE;

-- 2. 기존 데이터 삭제 (IF EXISTS 추가로 안전하게)
DELETE FROM portfolios IF EXISTS;
DELETE FROM stores IF EXISTS;
DELETE FROM users IF EXISTS;

-- 3. 제약 조건 다시 켜기
SET REFERENTIAL_INTEGRITY TRUE;

-- 4. 유저 데이터 삽입
INSERT INTO users (email, password, nickname, role, provider)
VALUES ('admin@test.com', '1234', '관리자', 'SELLER', 'LOCAL');

-- 5. 매장 데이터 삽입 (owner_id는 1번 유저 참조)
INSERT INTO stores (name, description, rating, review_count, owner_id, order_schema)
VALUES ('메이커리 강남점', '레터링 케이크 전문점입니다.', 4.5, 120, 1, '{"template": "기본양식"}');


-- 데이터 임의 삽입(확인용)
-- 1번 유저(관리자)가 1번 매장(강남점)에 남기는 테스트 리뷰
INSERT INTO reviews (content, rating, user_id, store_id, created_at)
VALUES ('케이크가 너무 예뻐요! 다음에 또 주문할게요.', 5, 1, 1, NOW());

-- 1번 매장(강남점)의 테스트 포트폴리오 데이터
INSERT INTO portfolios (image_url, tags, is_inpainting_allowed, like_count, store_id)
VALUES ('https://example.com/cake1.jpg', '초코, 생일, 레터링', true, 10, 1);

-- 메이커리 강남점(ID 1) 위치를 서울 시청 근처로 설정
UPDATE stores SET latitude = 37.5665, longitude = 126.9780 WHERE id = 1;

-- 1. 1번 포트폴리오에 대한 AI 결과 데이터 강제로 하나 넣기
INSERT INTO inpaintings (result_image_url, prompt, portfolio_id, created_at)
VALUES ('https://example.com/ai-cake.jpg', '레터링 추가해줘', 1, NOW());

-- 1번 포트폴리오에 대한 AI 가짜 결과물 강제 삽입
INSERT INTO inpaintings (result_image_url, prompt, portfolio_id, created_at)
VALUES ('https://example.com/ai-cake-result.jpg', '민트색 하트 추가', 1, NOW());

UPDATE stores
SET order_schema = '{"size": ["도시락", "1호", "2호"], "flavors": ["초코", "바닐라", "딸기"], "options": ["레터링", "생화"]}'
WHERE id = 1;

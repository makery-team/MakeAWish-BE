SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM ai_agent_messages;
DELETE FROM ai_inpainted_designs;
DELETE FROM notifications;
DELETE FROM likes;
DELETE FROM portfolio_tags;
DELETE FROM reviews;
DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM payments;
DELETE FROM portfolios;
DELETE FROM products;
DELETE FROM categories;
DELETE FROM stores;
DELETE FROM seller_profiles;
DELETE FROM tags;
DELETE FROM users;
DELETE FROM chat_room;
DELETE FROM chat_message;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. 유저 (Users) - 5명, 전원 구글 로그인, 1번 관리자
INSERT INTO users (id, email, password, name, nickname, phone_number, language, user_role, o_auth_provider, created_at, modified_at) VALUES
(1, 'admin@gmail.com', 'google_oauth_dummy', '김관리', '어드민케이커', '010-1111-1111', 'KO', 'ROLE_ADMIN', 'GOOGLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'user2@gmail.com', 'google_oauth_dummy', '이주문', '빵순이', '010-2222-2222', 'KO', 'ROLE_USER', 'GOOGLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'user3@gmail.com', 'google_oauth_dummy', '박달콤', '디저트러버', '010-3333-3333', 'KO', 'ROLE_USER', 'GOOGLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'user4@gmail.com', 'google_oauth_dummy', '최제빵', '케이크매니아', '010-4444-4444', 'KO', 'ROLE_USER', 'GOOGLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'user5@gmail.com', 'google_oauth_dummy', '정선물', '기념일요정', '010-5555-5555', 'KO', 'ROLE_USER', 'GOOGLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2. 판매자 프로필 (Seller Profiles) - 5개
INSERT INTO seller_profiles (id, business_no, bank_account, status, user_id, created_at, modified_at) VALUES
(1, '111-22-33333', '국민은행 123456-78-901234', 'VERIFIED', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '222-33-44444', '신한은행 110-123-456789', 'VERIFIED', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '333-44-55555', '우리은행 1002-111-222222', 'VERIFIED', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '444-55-66666', '하나은행 123-456789-01200', 'VERIFIED', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, '555-66-77777', '카카오뱅크 3333-01-1234567', 'PENDING', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 3. 매장 (Stores) - 5개
INSERT INTO stores (id, name, description, address, phone, hours, notice, caution_notice, latitude, longitude, rating, review_count, seller_profile_id, created_at, modified_at) VALUES
(1, '어드민 베이커리', '정성을 담아 만듭니다.', '서울 마포구 연남동 239-20 1층', '02-332-1004', '{"mon":"09:00 - 20:00", "tue":"09:00 - 20:00", "wed":"09:00 - 20:00", "thu":"09:00 - 20:00", "fri":"09:00 - 20:00", "sat":"10:00 - 18:00", "sun":"휴무"}', '휴무일은 인스타 공지', '견과류 알러지 주의', 37.5665, 126.9780, 5.0, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '달콤달콤 케이크', '특별한 날을 더 특별하게', '서울 강남구 테헤란로 123 2층', '02-555-0123', '11:00-21:00', '당일 예약 불가', '유제품 알러지 주의', 37.5651, 126.9895, 5.0, 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '위시 케이크', '원하는 디자인 모두 가능!', '서울 성동구 서울숲2길 14', '02-466-7890', '12:00-22:00', '리뷰 이벤트 진행중', '색소 주의', 37.5511, 126.9882, 4.5, 2, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '해피 베이킹', '수제 레터링 전문', '서울 송파구 올림픽로 300 1층', '02-411-2345', '09:00-19:00', '주말은 오전 픽업만', '알러지 성분 없음', 37.5412, 127.0392, 5.0, 1, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, '러블리 디저트', '귀여운 도시락 케이크 전문', '서울 용산구 이태원로 150 2층', '02-790-5678', '10:00-18:00', '예약은 최소 3일 전', '글루텐 프리 옵션 가능', 37.5211, 126.9242, 5.0, 1, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 4. 매장 카테고리 (Categories) - 5개
INSERT INTO categories (id, name, store_id, created_at, modified_at) VALUES
(1, '도시락 케이크', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '포토 케이크', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '레터링 케이크', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '캐릭터 케이크', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, '2단 케이크', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 5. 제품 (Products) - 5개 (JSON 키워드 제거)
INSERT INTO products (id, name, price, description, is_available, order_schema, store_id, created_at, modified_at) VALUES
(1, '커스텀 도시락 케이크', 18000, '간단한 선물용 미니 케이크', true, '{"type": "object", "properties": {"flavor": {"type": "string"}}}', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '프리미엄 포토 케이크', 35000, '식용 포토용지가 올라간 케이크', true, '{"type": "object", "properties": {"photoUrl": {"type": "string"}}}', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '시그니처 레터링 케이크', 25000, '마음을 전하는 레터링 케이크', true, '{"type": "object", "properties": {"text": {"type": "string"}}}', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '강아지 입체 케이크', 45000, '반려견 얼굴 입체 디자인', true, '{"type": "object", "properties": {"dogBreed": {"type": "string"}}}', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, '생화 장식 2단 케이크', 60000, '파티, 브라이덜 샤워용', true, '{"type": "object", "properties": {"flowerColor": {"type": "string"}}}', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 6. 태그 (Tags) - 원본 해시태그 41개 전부 등록
INSERT INTO tags (id, name) VALUES 
(1, '레터링케이크'), (2, '도시락케이크'), (3, '주문제작케이크'), (4, '생일'), (5, '기념일'), 
(6, '귀여운선물'), (7, '강아지'), (8, '반려견'), (9, '댕댕이'), (10, '캐릭터디자인'), 
(11, '포토케이크'), (12, '주문제작'), (13, '커플'), (14, '데이트'), (15, '사랑'), 
(16, '선물'), (17, '이벤트'), (18, '기념일선물'), (19, '특별한날'), (20, '로또'), 
(21, '레터링'), (22, '위트'), (23, '아이디어'), (24, '가족선물'), (25, '퇴사'), 
(26, '퇴사선물'), (27, '직장인'), (28, '이직'), (29, '축하'), (30, '응원'), 
(31, '센스있는선물'), (32, '시바'), (33, '부모님선물'), (34, '아빠생신'), (35, '맞춤제작'), 
(36, '인물케이크'), (37, '인스타그램케이크'), (38, '커플선물'), (39, '지니'), (40, '특별한선물'), (41, '홈파티');

-- 7. 포트폴리오 (Portfolios) - 제공된 이미지 7개
INSERT INTO portfolios (id, title, description, image_url, is_inpainting_allowed, like_count, store_id, product_id, created_at, modified_at) VALUES
(1, '귀여운 강아지 디자인', '반려견 생일 축하 케이크', 'https://image.idus.com/image/files/10b6568597594967a777aa61d1a05683_400.jpg', true, 12, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '커플 포토 케이크', '데이트 기념일 특별한 선물', 'https://cdn.011st.com/11dims/resize/2000x2000/quality/75/11src/product/5740566358/B.jpg?478000000', true, 45, 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '로또 도시락 케이크', '위트 있는 아이디어 선물', 'https://thumbnail.coupangcdn.com/thumbnails/remote/657x657q90trim/image/vendor_inventory/5111/973f67933f06a761fc8e81468dbacd56ece8bc9f905932bbc36d14885a89.png', true, 8, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '퇴사 응원 시바견', '직장인 이직 축하용', 'https://thumbnail.coupangcdn.com/thumbnails/remote/492x492ex/image/vendor_inventory/e6ec/6f6935cbbd537d1e2768c56df673d8ab62221220ee03cd66a37c7ac43514.png', true, 32, 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, '부모님 인물 케이크', '아빠 생신 맞춤 제작', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTOjzIyDGx9qzXEWDV3VJHvpL-Q1SjqvA9THw&s', false, 77, 3, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, '인스타 갬성 포토', '커플선물 기념일 이벤트', 'https://cf.product-image.s.zigzag.kr/original/d/2024/7/9/35807_202407091029430092_44449.jpeg?width=400&height=400&quality=80&format=webp', true, 55, 4, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, '지니 캐릭터 홈파티', '생일 기념일 특별한 선물', 'https://turtlehip.com/upload/product/110_1618413658_0.07%20%20(6%E2%98%85)', true, 19, 5, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 8. 포트폴리오-태그 매핑 (Portfolio Tags)
INSERT INTO portfolio_tags (portfolio_id, tag_id) VALUES 
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
(2, 11), (2, 5), (2, 12), (2, 13), (2, 14), (2, 15), (2, 16), (2, 17), (2, 18), (2, 19),
(3, 20), (3, 2), (3, 12), (3, 21), (3, 17), (3, 18), (3, 22), (3, 23), (3, 24), (3, 19),
(4, 25), (4, 2), (4, 12), (4, 21), (4, 26), (4, 27), (4, 28), (4, 29), (4, 30), (4, 31), (4, 7), (4, 32),
(5, 1), (5, 12), (5, 4), (5, 5), (5, 33), (5, 34), (5, 19), (5, 17), (5, 35), (5, 36),
(6, 11), (6, 37), (6, 12), (6, 21), (6, 5), (6, 4), (6, 38), (6, 17), (6, 19), (6, 18),
(7, 21), (7, 12), (7, 4), (7, 5), (7, 10), (7, 39), (7, 40), (7, 17), (7, 41);

-- 9. 주문 (Orders) - 총 7개 (JSON 키워드 제거)
INSERT INTO orders (id, order_number, status, pickup_date, total_price, order_data, user_id, store_id, created_at, modified_at) VALUES
(1, 'ORD-20260604-001', 'COMPLETED', CURRENT_TIMESTAMP, 18000, '{"flavor": "초코"}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'ORD-20260604-002', 'PAID', CURRENT_TIMESTAMP, 35000, '{"photoUrl": "http"}', 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'ORD-20260604-003', 'IN_PROGRESS', CURRENT_TIMESTAMP, 25000, '{"text": "생일축하해"}', 1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'ORD-20260604-004', 'COMPLETED', CURRENT_TIMESTAMP, 18000, '{"flavor": "바닐라"}', 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'ORD-20260604-005', 'PICKUP_READY', CURRENT_TIMESTAMP, 25000, '{"text": "퇴사축하"}', 3, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'ORD-20260604-006', 'PENDING_QUOTE', CURRENT_TIMESTAMP, 45000, '{"dogBreed": "비숑"}', 4, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'ORD-20260604-007', 'QUOTED', CURRENT_TIMESTAMP, 60000, '{"flowerColor": "핑크"}', 5, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 10. 주문 항목 (OrderItems) - 주문과 1:1 매칭
INSERT INTO order_items (id, name, quantity, unit_price, customized_image_url, order_id, product_id, portfolio_id) VALUES
(1, '귀여운 강아지 케이크', 1, 18000, 'https://custom.image.url/1', 1, 1, 1),
(2, '커플 포토 케이크', 1, 35000, NULL, 2, 2, 2),
(3, '부모님 인물 케이크', 1, 25000, 'https://custom.image.url/3', 3, 3, 5),
(4, '로또 도시락 케이크', 1, 18000, NULL, 4, 1, 3),
(5, '퇴사 응원 시바견', 1, 25000, 'https://custom.image.url/5', 5, 3, 4),
(6, '인스타 갬성 포토', 1, 45000, NULL, 6, 4, 6),
(7, '지니 캐릭터 홈파티', 1, 60000, 'https://custom.image.url/7', 7, 5, 7);

-- 11. 결제 (Payments)
INSERT INTO payments (id, amount, status, method_type, order_id, created_at, modified_at) VALUES
(1, 18000, 'PAID', 'KAKAOPAY', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 35000, 'PAID', 'CARD', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 25000, 'PAID', 'NAVERPAY', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 18000, 'PAID', 'CARD', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 25000, 'PAID', 'BANK_TRANSFER', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 45000, 'READY', 'KAKAOPAY', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 60000, 'READY', 'CARD', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 12. 리뷰 (Reviews) - 7개
INSERT INTO reviews (id, content, image_url, rating, store_id, order_id, created_at, modified_at) VALUES
(1, '강아지가 너무 귀여워요! 맛도 최고입니다.', 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=600&auto=format&fit=crop&q=80', 5, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '포토 화질이 진짜 좋아요 ㅠㅠ 감동!', 'https://images.unsplash.com/photo-1535141192574-5d4897c13136?w=600&auto=format&fit=crop&q=80', 5, 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '레터링 글씨체가 예쁩니다.', NULL, 4, 3, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '도시락 케이크 가성비 짱짱', 'https://images.unsplash.com/photo-1588195538326-c5b1e9f80a1b?w=600&auto=format&fit=crop&q=80', 5, 1, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, '퇴사하는 분이 엄청 좋아하셨어요 ㅋㅋ', NULL, 5, 3, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, '입체 강아지 케이크 너무 귀여워요 강추!', 'https://images.unsplash.com/photo-1588195538326-c5b1e9f80a1b?w=600&auto=format&fit=crop&q=80', 5, 4, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, '생일 파티 분위기 제대로 살렸습니다 최고!!', 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=600&auto=format&fit=crop&q=80', 5, 5, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 13. 좋아요 (Likes) - 5개
INSERT INTO likes (id, user_id, portfolio_id, created_at, modified_at) VALUES
(1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), 
(2, 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), 
(3, 3, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), 
(4, 4, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), 
(5, 5, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 14. 알림 (Notifications) - 5개
INSERT INTO notifications (id, user_id, message, is_read, created_at) VALUES
(1, 1, '[어드민 베이커리] 주문이 완료되었습니다.', false, CURRENT_TIMESTAMP),
(2, 2, '주문하신 케이크가 픽업 준비되었습니다.', true, CURRENT_TIMESTAMP),
(3, 3, '[위시 케이크] 픽업 준비가 완료되었습니다.', false, CURRENT_TIMESTAMP),
(4, 4, '견적서가 도착했습니다. 확인해주세요.', false, CURRENT_TIMESTAMP),
(5, 5, '결제가 성공적으로 처리되었습니다.', true, CURRENT_TIMESTAMP);

-- 15. 채팅방 (ChatRooms) - 1개
INSERT INTO chat_room (room_number, user_id, other_id, created_at, modified_at) VALUES
(1, 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 16. 채팅 메시지 (ChatMessages) - 5개
INSERT INTO chat_message (id, user_id, message, room_number, image_url, created_at, modified_at) VALUES
(1, 2, '안녕하세요 사장님, 케이크 예약 문의드립니다.', 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, '안녕하세요! 어드민 베이커리입니다. 어떤 디자인을 원하시나요?', 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 2, '도시락 케이크에 간단한 레터링만 추가하고 싶어요.', 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 1, '네, 가능합니다. 픽업 날짜와 시간은 언제가 좋으신가요?', 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 2, '이번 주 금요일 오후 5시에 방문하겠습니다!', 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 시퀀스 초기화
ALTER TABLE users AUTO_INCREMENT = 6;
ALTER TABLE seller_profiles AUTO_INCREMENT = 6;
ALTER TABLE stores AUTO_INCREMENT = 6;
ALTER TABLE categories AUTO_INCREMENT = 6;
ALTER TABLE products AUTO_INCREMENT = 6;
ALTER TABLE tags AUTO_INCREMENT = 42;
ALTER TABLE portfolios AUTO_INCREMENT = 8;
ALTER TABLE orders AUTO_INCREMENT = 8;
ALTER TABLE order_items AUTO_INCREMENT = 8;
ALTER TABLE payments AUTO_INCREMENT = 8;
ALTER TABLE reviews AUTO_INCREMENT = 6;
ALTER TABLE likes AUTO_INCREMENT = 6;
ALTER TABLE notifications AUTO_INCREMENT = 6;
ALTER TABLE chat_room AUTO_INCREMENT = 2;
ALTER TABLE chat_message AUTO_INCREMENT = 6;
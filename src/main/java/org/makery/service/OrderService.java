package org.makery.service;

import lombok.RequiredArgsConstructor;
import org.makery.domain.*;
import org.makery.dto.OrderDetailResponse;
import org.makery.dto.OrderItemRequest;
import org.makery.dto.OrderItemResponse;
import org.makery.dto.OrderRequest;
import org.makery.repository.OrderRepository;
import org.makery.repository.ProductRepository;
import org.makery.repository.StoreRepository;
import org.makery.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * 주문 생성 로직
     */
    @Transactional
    public Long createOrder(Long userId, OrderRequest req) {
        // 1. 주문자(User)와 매장(Store) 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Store store = storeRepository.findById(req.storeId())
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));

        // 2. 주문 객체 초기 생성 (빌더 패턴)
        Order order = Order.builder()
                .user(user)
                .store(store)
                .orderNumber(generateOrderNumber()) // 고유 주문번호 생성
                .status(OrderStatus.PENDING_QUOTE)        // 초기 상태: 대기
                .pickupDate(req.pickupDate())
                .orderData(req.orderData())
                .items(new ArrayList<>())
                .build();

        int calculatedTotalPrice = 0;

        // 3. 주문 상품(OrderItem) 리스트 생성 및 연관관계 설정
        for (OrderItemRequest itemReq : req.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new IllegalArgumentException("상품 정보를 찾을 수 없습니다."));

            // 상품의 현재 이름과 가격을 OrderItem에 스냅샷으로 저장
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .name(product.getName())
                    .unitPrice(product.getPrice())
                    .quantity(itemReq.quantity())
                    .customizedImageUrl(itemReq.customizedImageUrl())
                    .order(order) // 양방향 연관관계 설정
                    .build();

            order.getItems().add(orderItem);

            // 총 금액 누적 계산
            calculatedTotalPrice += orderItem.getUnitPrice() * orderItem.getQuantity();
        }

        // 4. 합산된 총 금액 설정
        order.setTotalPrice(calculatedTotalPrice);

        // 5. DB 저장 (CascadeType.ALL 설정 덕분에 OrderItem도 함께 저장됨)
        return orderRepository.save(order).getId();
    }

    /**
     * 주문 상세 조회
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long orderId, Long currentUserId, UserRole role) {
        Order order = orderRepository.findDetailById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 내역을 찾을 수 없습니다."));

        // 본인 확인 로직
        if (role == UserRole.ROLE_USER) {
            // 구매자라면: 주문서의 주인 ID와 현재 로그인한 유저 ID가 같은지 확인
            if (!order.getUser().getId().equals(currentUserId)) {
                throw new AccessDeniedException("본인의 주문만 조회할 수 있습니다.");
            }
        } else if (role == UserRole.ROLE_SELLER) {
            // 사장님이라면: 주문이 들어온 매장의 주인이 현재 유저인지 확인
            if (!order.getStore().getSellerProfile().getUser().getId().equals(currentUserId)) {
                throw new AccessDeniedException("본인 매장의 주문만 조회할 수 있습니다.");
            }
        }

        return OrderDetailResponse.from(order);
    }

    /**
     * 비즈니스 로직: 고유 주문번호 생성 (날짜-랜덤문자 조합)
     */
    private String generateOrderNumber() {
        return java.time.LocalDate.now().toString().replace("-", "")
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Transactional
    public void updateOrderStatus(Long orderId, Long userId, OrderStatus newStatus) {
        // 1. 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        // 2. 권한 검증: 현재 로그인한 사장님이 이 매장의 주인이 맞는지 확인
        // store -> sellerProfile -> user 구조를 따라가서 ID를 비교합니다.
        Long storeOwnerId = order.getStore().getSellerProfile().getUser().getId();

        if (!storeOwnerId.equals(userId)) {
            throw new RuntimeException("본인 매장의 주문 상태만 변경할 수 있습니다.");
        }

        // 3. 상태 업데이트
        order.updateStatus(newStatus);
    }
}

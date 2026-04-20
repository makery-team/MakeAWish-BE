package org.makery.service;

import lombok.RequiredArgsConstructor;
import org.makery.domain.*;
import org.makery.dto.OrderCreateRequest;
import org.makery.dto.OrderDetailResponse;
import org.makery.dto.OrderItemRequest;
import org.makery.dto.OrderSummaryResponse;
import org.makery.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PortfolioRepository portfolioRepository; // 💡 추가됨

    /**
     * 주문 생성
     */
    @Transactional
    public Long createOrder(Long userId, OrderCreateRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Store store = storeRepository.findById(req.storeId())
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));

        // 💡 1. 초기 주문 객체 생성 (기존 store.getOrderSchema() 호출 제거)
        Order order = Order.builder()
                .user(user)
                .store(store)
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.PENDING_QUOTE)
                .pickupDate(req.pickupDate())
                .orderData(req.orderData())
                .items(new ArrayList<>())
                .build();

        int calculatedTotalPrice = 0;

        for (OrderItemRequest itemReq : req.items()) {
            // 💡 2. 상품(카테고리) 정보 조회
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new IllegalArgumentException("상품 정보를 찾을 수 없습니다."));

            // 💡 3. 핵심 변경: 이제 Product(카테고리) 레벨의 스키마로 검증합니다.
            validateOrderData(product.getOrderSchema(), req.orderData());

            // 💡 4. 사용자가 선택한 디자인(Portfolio) 정보 연결
            Portfolio portfolio = null;
            if (itemReq.portfolioId() != null) {
                portfolio = portfolioRepository.findById(itemReq.portfolioId())
                        .orElseThrow(() -> new IllegalArgumentException("디자인 정보를 찾을 수 없습니다."));
            }

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .portfolio(portfolio) // 💡 Portfolio 참조 추가
                    .name(product.getName())
                    .unitPrice(product.getPrice())
                    .quantity(itemReq.quantity())
                    .customizedImageUrl(itemReq.customizedImageUrl())
                    .order(order)
                    .build();

            order.getItems().add(orderItem);
            calculatedTotalPrice += orderItem.getUnitPrice() * orderItem.getQuantity();
        }

        order.setTotalPrice(calculatedTotalPrice);

        return orderRepository.save(order).getId();
    }

    /**
     * 주문 데이터 검증 로직
     */
    private void validateOrderData(Map<String, Object> schema, Map<String, Object> orderData) {
        if (schema == null || !schema.containsKey("templates")) return;

        if (schema.get("templates") instanceof List<?> templates) {
            for (Object item : templates) {
                if (item instanceof Map<?, ?> field) {
                    if (!(field.get("name") instanceof String name)) continue;

                    Object reqObj = field.get("required");
                    boolean isRequired = reqObj instanceof Boolean b && b;

                    if (isRequired) {
                        Object labelObj = field.get("label");
                        String label = (labelObj instanceof String l) ? l : name;

                        if (orderData == null || !orderData.containsKey(name) ||
                                orderData.get(name) == null || orderData.get(name).toString().trim().isEmpty()) {

                            throw new IllegalArgumentException(label + " 항목은 필수 입력 사항입니다.");
                        }
                    }
                }
            }
        }
    }

    /**
     * 주문 상세 조회
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long orderId, Long currentUserId, UserRole role) {
        Order order = orderRepository.findDetailById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 내역을 찾을 수 없습니다."));

        if (role == UserRole.ROLE_USER) {
            if (!order.getUser().getId().equals(currentUserId)) {
                throw new AccessDeniedException("본인의 주문만 조회할 수 있습니다.");
            }
        } else if (role == UserRole.ROLE_SELLER) {
            if (!order.getStore().getSellerProfile().getUser().getId().equals(currentUserId)) {
                throw new AccessDeniedException("본인 매장의 주문만 조회할 수 있습니다.");
            }
        }

        return OrderDetailResponse.from(order);
    }

    /**
     * 고유 주문번호 생성 (날짜-랜덤문자 조합)
     */
    private String generateOrderNumber() {
        return java.time.LocalDate.now().toString().replace("-", "")
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 주문 상태 변경
     */
    @Transactional
    public void updateOrderStatus(Long orderId, Long userId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        Long storeOwnerId = order.getStore().getSellerProfile().getUser().getId();

        if (!storeOwnerId.equals(userId)) {
            throw new RuntimeException("본인 매장의 주문 상태만 변경할 수 있습니다.");
        }

        order.updateStatus(newStatus);
    }

    /**
     * 주문 목록 조회
     */
    public List<OrderSummaryResponse> getMyOrders(Long userId, UserRole role) {
        List<Order> orders;

        if (role == UserRole.ROLE_SELLER) {
            orders = orderRepository.findAllByStoreSellerProfileUserIdOrderByCreatedAtDesc(userId);
        } else {
            orders = orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        }

        return orders.stream()
                .map(OrderSummaryResponse::from)
                .toList();
    }
}
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

import org.makery.dto.MessageDraftResponse;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PortfolioRepository portfolioRepository;
    private final NotificationService notificationService;

    /**
     * 주문 생성
     */
    @Transactional
    public Long createOrder(Long userId, OrderCreateRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Store store = storeRepository.findById(req.storeId())
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));

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
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new IllegalArgumentException("상품 정보를 찾을 수 없습니다."));

            validateOrderData(product.getOrderSchema(), req.orderData());

            Portfolio portfolio = null;
            if (itemReq.portfolioId() != null) {
                portfolio = portfolioRepository.findById(itemReq.portfolioId())
                        .orElseThrow(() -> new IllegalArgumentException("디자인 정보를 찾을 수 없습니다."));
            }

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .portfolio(portfolio)
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

        Order savedOrder = orderRepository.save(order);

        // 🌟 사장님에게 실시간 새 주문 접수 알림 발송
        try {
            if (store.getSellerProfile() != null && store.getSellerProfile().getUser() != null) {
                User storeOwner = store.getSellerProfile().getUser();
                String customerName = (user.getName() != null && !user.getName().isBlank())
                        ? user.getName()
                        : ((user.getNickname() != null && !user.getNickname().isBlank()) ? user.getNickname() : "고객");

                notificationService.createNotification(
                        storeOwner,
                        "새 주문 접수",
                        String.format("새로운 주문이 접수되었습니다! (주문번호: %s, 고객: %s)", savedOrder.getOrderNumber(), customerName),
                        org.makery.domain.NotificationType.ORDER,
                        savedOrder.getId()
                );
            }
        } catch (Exception e) {
            log.warn("사장님 새 주문 알림 발송 실패 (Order ID: {}): {}", savedOrder.getId(), e.getMessage());
        }

        return savedOrder.getId();
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

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        UserRole effectiveRole = (user.getUserRole() != null) ? user.getUserRole() : role;
        boolean isSeller = (effectiveRole == UserRole.ROLE_SELLER) || (user.getSellerProfile() != null);

        if (isSeller) {
            if (!order.getStore().getSellerProfile().getUser().getId().equals(currentUserId)) {
                throw new AccessDeniedException("본인 매장의 주문만 조회할 수 있습니다.");
            }
        } else {
            if (!order.getUser().getId().equals(currentUserId)) {
                throw new AccessDeniedException("본인의 주문만 조회할 수 있습니다.");
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
    public void updateOrderStatus(Long orderId, Long userId, OrderStatus newStatus, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        Long storeOwnerId = order.getStore().getSellerProfile().getUser().getId();

        if (!storeOwnerId.equals(userId)) {
            throw new RuntimeException("본인 매장의 주문 상태만 변경할 수 있습니다.");
        }

        order.updateStatus(newStatus, reason);
        sendStatusNotification(order, newStatus, reason);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, Long userId, OrderStatus newStatus) {
        updateOrderStatus(orderId, userId, newStatus, null);
    }

    /**
     * 주문 목록 조회 (date: "today", roleParam: "consumer" / "seller")
     */
    public List<OrderSummaryResponse> getMyOrders(Long userId, UserRole role, String date, String roleParam) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        boolean isSeller;
        if ("consumer".equalsIgnoreCase(roleParam) || "customer".equalsIgnoreCase(roleParam)) {
            // 소비자 앱: 무조건 내가 주문한 내역(구매자 모드)
            isSeller = false;
        } else if ("seller".equalsIgnoreCase(roleParam)) {
            // 사장님 웹: 무조건 내 매장으로 들어온 주문(판매자 모드)
            isSeller = true;
        } else {
            // role 파라미터가 없는 경우
            if ("today".equalsIgnoreCase(date)) {
                isSeller = true;
            } else {
                UserRole effectiveRole = (user.getUserRole() != null) ? user.getUserRole() : role;
                isSeller = (effectiveRole == UserRole.ROLE_SELLER) || (user.getSellerProfile() != null);
            }
        }

        List<Order> orders;

        if (isSeller) {
            if ("today".equalsIgnoreCase(date)) {
                java.time.ZoneId kstZone = java.time.ZoneId.of("Asia/Seoul");
                java.time.LocalDate today = java.time.LocalDate.now(kstZone);
                orders = orderRepository.findAllBySellerId(userId).stream()
                        .filter(o -> {
                            boolean isPending = o.getStatus() == OrderStatus.PENDING_QUOTE;
                            boolean isTodayCreated = o.getCreatedAt() != null &&
                                    o.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).withZoneSameInstant(kstZone).toLocalDate().equals(today);
                            boolean isTodayPickup = o.getPickupDate() != null &&
                                    o.getPickupDate().atZone(java.time.ZoneId.systemDefault()).withZoneSameInstant(kstZone).toLocalDate().equals(today);
                            return isPending || isTodayCreated || isTodayPickup;
                        })
                        .toList();
            } else {
                orders = orderRepository.findAllBySellerId(userId);
            }
        } else {
            if ("today".equalsIgnoreCase(date)) {
                LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
                LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
                orders = orderRepository.findAllByUserIdAndCreatedAtBetween(userId, startOfDay, endOfDay);
            } else {
                orders = orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
            }
        }

        return orders.stream()
                .map(OrderSummaryResponse::from)
                .toList();
    }

    public List<OrderSummaryResponse> getMyOrders(Long userId, UserRole role, String date) {
        return getMyOrders(userId, role, date, null);
    }

    /**
     * 주문 상태 변경 (JSON 바디 수신 형태 - ACCEPTED/REJECTED 매핑)
     */
    @Transactional
    public void updateOrderStatusByBody(Long orderId, Long userId, String statusStr, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        Long storeOwnerId = order.getStore().getSellerProfile().getUser().getId();
        if (!storeOwnerId.equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("본인 매장의 주문 상태만 변경할 수 있습니다.");
        }

        OrderStatus newStatus = mapStatus(statusStr);
        order.updateStatus(newStatus, reason);
        sendStatusNotification(order, newStatus, reason);
    }

    @Transactional
    public void updateOrderStatusByBody(Long orderId, Long userId, String statusStr) {
        updateOrderStatusByBody(orderId, userId, statusStr, null);
    }

    private void sendStatusNotification(Order order, OrderStatus newStatus, String reason) {
        try {
            User customer = order.getUser();
            if (customer == null) return;
            String storeName = (order.getStore() != null && order.getStore().getName() != null)
                    ? order.getStore().getName() : "매장";
            String title = "주문 상태 안내";
            String msg = switch (newStatus) {
                case QUOTED -> String.format("'%s'에서 주문 견적이 도착했습니다. 확인 후 결제를 진행해주세요.", storeName);
                case REJECTED -> String.format("'%s'에서 주문이 거절되었습니다.%s", storeName, (reason != null && !reason.isBlank()) ? " (사유: " + reason + ")" : "");
                case CANCELED -> String.format("'%s' 주문이 취소되었습니다.", storeName);
                case IN_PROGRESS -> String.format("'%s'에서 케이크 제작을 시작했습니다.", storeName);
                case PICKUP_READY -> String.format("'%s'에서 케이크 준비가 완료되었습니다! 매장에서 픽업해주세요.", storeName);
                case COMPLETED -> String.format("'%s' 케이크 픽업이 완료되었습니다. 소중한 리뷰를 남겨보세요! 🎂", storeName);
                default -> String.format("주문번호 [%s]의 상태가 '%s'로 변경되었습니다.", order.getOrderNumber(), newStatus);
            };

            notificationService.createNotification(
                    customer,
                    title,
                    msg,
                    org.makery.domain.NotificationType.ORDER,
                    order.getId()
            );
        } catch (Exception e) {
            log.warn("고객 주문상태 알림 발송 실패 (Order ID: {}): {}", order.getId(), e.getMessage());
        }
    }

    private OrderStatus mapStatus(String input) {
        if (input == null) throw new IllegalArgumentException("상태 값이 비어있습니다.");
        String upper = input.toUpperCase().trim();
        switch (upper) {
            case "ACCEPTED":
                return OrderStatus.QUOTED;
            case "REJECTED":
                return OrderStatus.REJECTED;
            case "CANCELED":
                return OrderStatus.CANCELED;
            default:
                try {
                    return OrderStatus.valueOf(upper);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("알 수 없는 주문 상태입니다: " + input);
                }
        }
    }

    /**
     * 하위 호환성을 위한 기본 getMyOrders 메서드
     */
    public List<OrderSummaryResponse> getMyOrders(Long userId, UserRole role) {
        return getMyOrders(userId, role, null);
    }

    /**
     * (AI) 메시지 초안 생성
     */
    public MessageDraftResponse generateMessageDraft(Long orderId, Long userId) {
        Order order = orderRepository.findDetailById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 내역을 찾을 수 없습니다. ID: " + orderId));

        Long storeOwnerId = order.getStore().getSellerProfile().getUser().getId();
        if (!storeOwnerId.equals(userId)) {
            throw new AccessDeniedException("본인 매장의 주문에 대한 메시지 초안만 생성할 수 있습니다.");
        }

        String customerName = (order.getUser() != null && order.getUser().getNickname() != null)
                ? order.getUser().getNickname() : "고객";
        String storeName = order.getStore().getName();
        String orderNumber = order.getOrderNumber();
        int totalPrice = order.getTotalPrice();

        StringBuilder draft = new StringBuilder();
        draft.append(String.format("안녕하세요 %s님, %s입니다! 💖\n\n", customerName, storeName));
        draft.append(String.format("주문번호 [%s]의 주문 상태가 현재 '%s' 상태로 업데이트되었습니다.\n", orderNumber, order.getStatus()));
        draft.append(String.format("총 금액: %,d원\n", totalPrice));

        if (order.getExtraFee() != null && order.getExtraFee() > 0) {
            draft.append(String.format("※ 추가 금액 (%,d원) 사유: %s\n", order.getExtraFee(), order.getExtraFeeReason()));
        }

        if (order.getPickupDate() != null) {
            draft.append(String.format("픽업 예정 일시: %s\n", order.getPickupDate().toString().replace("T", " ")));
        }

        draft.append("\n정성껏 준비하여 안내해 드리겠습니다. 궁금하신 점이 있다면 언제든 문의해 주세요. 감사합니다! ✨");

        return MessageDraftResponse.builder()
                .orderId(order.getId())
                .orderNumber(orderNumber)
                .draftMessage(draft.toString())
                .build();
    }
}
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
    private final PortfolioRepository portfolioRepository;

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
     * 주문 목록 조회 (날짜 필터링 지원)
     */
    public List<OrderSummaryResponse> getMyOrders(Long userId, UserRole role, String date) {
        List<Order> orders;

        if (role == UserRole.ROLE_SELLER) {
            if ("today".equalsIgnoreCase(date)) {
                java.time.LocalDate today = java.time.LocalDate.now();
                java.time.LocalDateTime start = today.atStartOfDay();
                java.time.LocalDateTime end = today.atTime(23, 59, 59, 999999999);
                orders = orderRepository.findAllBySellerIdAndPickupDateBetween(userId, start, end);
            } else {
                orders = orderRepository.findAllBySellerId(userId);
            }
        } else {
            if ("today".equalsIgnoreCase(date)) {
                java.time.LocalDate today = java.time.LocalDate.now();
                java.time.LocalDateTime start = today.atStartOfDay();
                java.time.LocalDateTime end = today.atTime(23, 59, 59, 999999999);
                orders = orderRepository.findAllByUserIdAndPickupDateBetween(userId, start, end);
            } else {
                orders = orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
            }
        }

        return orders.stream()
                .map(OrderSummaryResponse::from)
                .toList();
    }

    /**
     * 주문 상태 변경 (JSON 바디 수신 형태)
     */
    @Transactional
    public void updateOrderStatusByBody(Long orderId, Long userId, String statusStr) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        Long storeOwnerId = order.getStore().getSellerProfile().getUser().getId();
        if (!storeOwnerId.equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("본인 매장의 주문 상태만 변경할 수 있습니다.");
        }

        OrderStatus newStatus = mapStatus(statusStr);
        order.updateStatus(newStatus);
    }

    /**
     * 주문 진행 단계별 AI 메시지 초안 생성
     */
    public String generateMessageDraft(Long orderId, Long currentUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        Long storeOwnerId = order.getStore().getSellerProfile().getUser().getId();
        if (!storeOwnerId.equals(currentUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("본인 매장의 주문에 대해서만 메시지 초안을 생성할 수 있습니다.");
        }

        String buyerName = order.getUser().getNickname();
        if (buyerName == null || buyerName.isBlank()) {
            buyerName = order.getUser().getName();
        }
        String storeName = order.getStore().getName();
        
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("M월 d일 H시 m분");
        String formattedPickupDate = order.getPickupDate() != null ? order.getPickupDate().format(formatter) : "미정";

        OrderStatus status = order.getStatus();
        StringBuilder draft = new StringBuilder();
        draft.append("안녕하세요, ").append(buyerName).append("님! ");
        draft.append(storeName).append("입니다. \n\n");

        switch (status) {
            case PENDING_QUOTE:
                draft.append("소중한 주문 요청을 확인하였습니다. ")
                     .append("작성해주신 시트, 맛, 디자인 요청사항을 검토하여 곧 정확한 견적을 안내해 드릴 예정입니다. ")
                     .append("잠시만 기다려주시면 감사하겠습니다.");
                break;
            case QUOTED:
                draft.append("요청하신 케이크 주문서에 대한 견적 작성이 완료되었습니다. ")
                     .append("상세 내역을 확인하신 후 결제를 완료해 주시면 최종 주문 접수가 되어 제작 단계로 넘어갑니다. ")
                     .append("추가 요구사항이나 변경을 원하시면 언제든 편하게 채팅을 남겨주세요!");
                break;
            case PAID:
                draft.append("결제가 정상적으로 완료되어 주문이 성공적으로 접수되었습니다! \n")
                     .append("지정해 주신 픽업 일시인 [").append(formattedPickupDate).append("]에 맞춰 ")
                     .append("가장 신선하고 예쁘게 제작하여 준비해 두겠습니다. 감사합니다.");
                break;
            case IN_PROGRESS:
                draft.append("현재 요청하신 디자인으로 케이크를 정성스럽게 제작하고 있습니다. \n")
                     .append("픽업 일시인 [").append(formattedPickupDate).append("]에 맞춰 ")
                     .append("최고의 퀄리티로 완성해 두겠습니다. 기대하셔도 좋습니다!");
                break;
            case PICKUP_READY:
                draft.append("주문하신 케이크가 예쁘게 완성되어 매장에 픽업 대기 중입니다! \n")
                     .append("오늘 [").append(formattedPickupDate).append("] 예약 시간에 맞춰 조심히 방문해 주시기 바랍니다. ")
                     .append("매장에서 뵙겠습니다.");
                break;
            case COMPLETED:
                draft.append("저희 케이크와 함께 행복한 시간 보내셨기를 바랍니다. \n")
                     .append("정성 어린 포토 리뷰나 피드백을 남겨주시면 저희에게 큰 힘이 됩니다. ")
                     .append("다음 기념일에도 또 뵙기를 희망합니다. 감사합니다!");
                break;
            case CANCELED:
                draft.append("주문 취소 처리가 완료되었습니다. ")
                     .append("이용에 불편을 드려 죄송하며, 환불 등 추가 문의 사항이 있으시면 언제든지 편하게 말씀해 주세요.");
                break;
            default:
                draft.append("주문(번호: ").append(order.getOrderNumber()).append(")의 현재 상태는 ")
                     .append(status.name()).append(" 입니다. 관련하여 상세 내용을 확인 중입니다.");
                break;
        }

        return draft.toString();
    }

    private OrderStatus mapStatus(String input) {
        if (input == null) throw new IllegalArgumentException("상태 값이 비어있습니다.");
        String upper = input.toUpperCase().trim();
        switch (upper) {
            case "ACCEPTED":
                return OrderStatus.IN_PROGRESS;
            case "REJECTED":
                return OrderStatus.CANCELED;
            default:
                try {
                    return OrderStatus.valueOf(upper);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("알 수 없는 주문 상태입니다: " + input);
                }
        }
    }
}
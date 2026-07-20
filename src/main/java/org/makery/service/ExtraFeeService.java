package org.makery.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.makery.domain.Order;
import org.makery.domain.Store;
import org.makery.domain.User;
import org.makery.dto.ExtraFeeCreateRequest;
import org.makery.dto.ExtraFeeResponse;
import org.makery.repository.OrderRepository;
import org.makery.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExtraFeeService {

    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;

    /**
     * 1. [사장님] 주문에 대한 추가금 및 사유 등록/수정
     */
    @Transactional
    public ExtraFeeResponse updateExtraFee(Long orderId, User seller, ExtraFeeCreateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 주문입니다. ID: " + orderId));

        // 로그인한 사장님이 해당 주문 매장의 주인인지 검증
        Store sellerStore = storeRepository.findByUserId(seller.getId())
                .orElseThrow(() -> new IllegalStateException("등록된 매장 정보가 없는 사장님 계정입니다. User ID: " + seller.getId()));

        if (!order.getStore().getId().equals(sellerStore.getId())) {
            throw new IllegalArgumentException("본인 매장에 들어온 주문에 대해서만 추가금을 설정할 수 있습니다.");
        }

        // 추가금 업데이트 및 총액 재계산 (Dirty Checking 적용)
        order.updateExtraFee(request.getExtraFee(), request.getReason());

        return ExtraFeeResponse.from(order);
    }

    /**
     * 2. [손님 / 사장님] 주문의 추가금 상세 내역 조회
     */
    public ExtraFeeResponse getExtraFee(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 주문입니다. ID: " + orderId));

        // 주문을 한 손님이거나, 해당 주문 매장의 사장님인지 권한 검증
        boolean isCustomer = order.getUser() != null && order.getUser().getId().equals(user.getId());
        boolean isSeller = storeRepository.findByUserId(user.getId())
                .map(store -> store.getId().equals(order.getStore().getId()))
                .orElse(false);

        if (!isCustomer && !isSeller) {
            throw new IllegalArgumentException("해당 주문의 추가금 정보 조회의 권한이 없습니다.");
        }

        return ExtraFeeResponse.from(order);
    }
}

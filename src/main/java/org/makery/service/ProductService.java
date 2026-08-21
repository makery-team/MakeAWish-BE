package org.makery.service;

import lombok.RequiredArgsConstructor;
import org.makery.domain.Product;
import org.makery.domain.Store;
import org.makery.domain.User;
import org.makery.dto.ProductRequest;
import org.makery.dto.ProductResponse;
import org.makery.repository.ProductRepository;
import org.makery.repository.StoreRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public ProductResponse createProduct(Long storeId, ProductRequest request, User currentUser) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        if (store.getSellerProfile() == null || !store.getSellerProfile().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("본인 매장의 상품만 생성할 수 있습니다.");
        }

        // 기본 주문서 양식 (표준 5대 질문 자동 세팅)
        Map<String, Object> defaultSchema = org.makery.util.OrderSchemaUtil.createDefaultOrderSchema();

        Product product = Product.builder()
                .name(request.name())
                .price(request.price() != null ? request.price() : 0)
                .description(request.description())
                .isAvailable(true)
                .store(store)
                .orderSchema(defaultSchema)
                .build();

        store.getProducts().add(product);
        Product saved = productRepository.save(product);
        return ProductResponse.from(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long storeId, Long productId, ProductRequest request, User currentUser) {
        Product product = validateStoreAndProduct(storeId, productId, currentUser);
        
        product.setName(request.name());
        product.setPrice(request.price() != null ? request.price() : 0);
        product.setDescription(request.description());

        return ProductResponse.from(product);
    }

    @Transactional
    public void deleteProduct(Long storeId, Long productId, User currentUser) {
        Product product = validateStoreAndProduct(storeId, productId, currentUser);
        product.getStore().getProducts().remove(product);
        productRepository.delete(product);
    }

    private Product validateStoreAndProduct(Long storeId, Long productId, User currentUser) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        if (store.getSellerProfile() == null || !store.getSellerProfile().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("본인 매장의 상품만 수정/삭제할 수 있습니다.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!product.getStore().getId().equals(storeId)) {
            throw new IllegalArgumentException("해당 매장의 상품이 아닙니다.");
        }

        return product;
    }
}

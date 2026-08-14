package org.makery.controller;

import lombok.RequiredArgsConstructor;
import org.makery.domain.PrincipalDetails;
import org.makery.dto.ProductRequest;
import org.makery.dto.ProductResponse;
import org.makery.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stores/{storeId}/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @PathVariable Long storeId,
            @RequestBody ProductRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return ResponseEntity.ok(productService.createProduct(storeId, request, principalDetails.user()));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @RequestBody ProductRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return ResponseEntity.ok(productService.updateProduct(storeId, productId, request, principalDetails.user()));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        productService.deleteProduct(storeId, productId, principalDetails.user());
        return ResponseEntity.ok().build();
    }
}

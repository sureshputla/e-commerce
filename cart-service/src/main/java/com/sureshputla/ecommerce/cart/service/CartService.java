package com.sureshputla.ecommerce.cart.service;

import com.sureshputla.ecommerce.cart.client.ProductServiceClient;
import com.sureshputla.ecommerce.cart.dto.CartItemDto;
import com.sureshputla.ecommerce.cart.dto.CartResponse;
import com.sureshputla.ecommerce.cart.dto.ProductDto;
import com.sureshputla.ecommerce.cart.model.CartItem;
import com.sureshputla.ecommerce.cart.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private static final String DEFAULT_USER_ID = "user1";

    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;

    @Transactional(readOnly = true)
    public CartResponse getCart() {
        return buildCartResponse(DEFAULT_USER_ID);
    }

    @Transactional
    public CartResponse addToCart(Integer productId) {
        ResponseEntity<ProductDto> response = productServiceClient.getProduct(productId);
        ProductDto product = response.getBody();

        if (product == null || product.getPrice() == 0) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        cartItemRepository.findByUserIdAndProductId(DEFAULT_USER_ID, productId)
                .ifPresentOrElse(
                        existing -> {
                            existing.setQuantity(existing.getQuantity() + 1);
                            cartItemRepository.save(existing);
                            log.debug("Incremented quantity for productId={}", productId);
                        },
                        () -> {
                            CartItem newItem = CartItem.builder()
                                    .userId(DEFAULT_USER_ID)
                                    .productId(product.getId())
                                    .productName(product.getName())
                                    .category(product.getCategory())
                                    .brand(product.getBrand())
                                    .unitPrice(product.getPrice())
                                    .quantity(1)
                                    .build();
                            cartItemRepository.save(newItem);
                            log.debug("Added new cart item for productId={}", productId);
                        }
                );

        return buildCartResponse(DEFAULT_USER_ID);
    }

    @Transactional
    public CartResponse removeFromCart(Integer productId) {
        cartItemRepository.findByUserIdAndProductId(DEFAULT_USER_ID, productId)
                .ifPresent(cartItemRepository::delete);
        log.debug("Removed productId={} from cart", productId);
        return buildCartResponse(DEFAULT_USER_ID);
    }

    @Transactional
    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
        log.info("Cart cleared for userId={}", userId);
    }

    private CartResponse buildCartResponse(String userId) {
        List<CartItem> items = cartItemRepository.findByUserIdOrderByProductIdAsc(userId);

        List<CartItemDto> itemDtos = items.stream().map(item -> {
            ProductDto productDto = ProductDto.builder()
                    .id(item.getProductId())
                    .name(item.getProductName())
                    .category(item.getCategory())
                    .brand(item.getBrand())
                    .price(item.getUnitPrice())
                    .build();
            int subtotal = item.getUnitPrice() * item.getQuantity();
            return CartItemDto.builder()
                    .product(productDto)
                    .quantity(item.getQuantity())
                    .subtotal(subtotal)
                    .build();
        }).toList();

        int total = itemDtos.stream().mapToInt(CartItemDto::getSubtotal).sum();
        return CartResponse.builder().items(itemDtos).total(total).build();
    }
}


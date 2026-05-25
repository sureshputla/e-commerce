package com.sureshputla.ecommerce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StoreServiceTest {
    private StoreService storeService;

    @BeforeEach
    void setUp() {
        storeService = new StoreService(new ProductCatalogService());
    }

    @Test
    void shouldAddToWishlistAndCartAndCheckout() {
        assertEquals(1, storeService.addToWishlist(1).size());

        storeService.addToCart(1);
        storeService.addToCart(1);

        StoreService.CartState cartState = storeService.getCartState();
        assertEquals(1, cartState.items().size());
        assertEquals(1598, cartState.total());

        StoreService.PaymentState paymentState = storeService.checkout("card");
        assertEquals("Payment successful via Card for ₹1598.", paymentState.message());
        assertEquals(0, storeService.getCartState().items().size());
    }

    @Test
    void shouldRejectCheckoutWhenCartIsEmpty() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> storeService.checkout("upi"));
        assertEquals("Add products to the cart before payment.", exception.getMessage());
    }

    @Test
    void shouldRejectUnknownProduct() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> storeService.addToCart(999));
        assertEquals("Product not found", exception.getMessage());
    }
}

package com.sureshputla.ecommerce.service;

import com.sureshputla.ecommerce.model.CartItem;
import com.sureshputla.ecommerce.model.Product;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StoreService {
    private static final Map<String, String> PAYMENT_LABELS = Map.of(
            "card", "Card",
            "upi", "UPI",
            "cod", "Cash on Delivery"
    );

    private final ProductCatalogService productCatalogService;
    private final Map<Integer, Integer> cart = new ConcurrentHashMap<>();
    private final Set<Integer> wishlist = ConcurrentHashMap.newKeySet();

    public StoreService(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    public List<Product> getWishlistProducts() {
        return productCatalogService.getProductsByIds(wishlist);
    }

    public List<Product> addToWishlist(int productId) {
        ensureProductExists(productId);
        wishlist.add(productId);
        return getWishlistProducts();
    }

    public List<Product> removeFromWishlist(int productId) {
        wishlist.remove(productId);
        return getWishlistProducts();
    }

    public List<CartItem> getCartItems() {
        return cart.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Product product = productCatalogService.findById(entry.getKey())
                            .orElseThrow(() -> new IllegalArgumentException("Product not found"));
                    int quantity = entry.getValue();
                    return new CartItem(product, quantity, quantity * product.price());
                })
                .toList();
    }

    public CartState addToCart(int productId) {
        ensureProductExists(productId);
        cart.merge(productId, 1, Integer::sum);
        return getCartState();
    }

    public CartState removeFromCart(int productId) {
        cart.remove(productId);
        return getCartState();
    }

    public CartState getCartState() {
        List<CartItem> items = getCartItems();
        int total = items.stream().mapToInt(CartItem::subtotal).sum();
        return new CartState(items, total);
    }

    public PaymentState checkout(String paymentMethod) {
        if (cart.isEmpty()) {
            throw new IllegalStateException("Add products to the cart before payment.");
        }

        String normalizedMethod = paymentMethod == null ? "card" : paymentMethod.toLowerCase(Locale.ROOT).trim();
        String methodLabel = PAYMENT_LABELS.get(normalizedMethod);
        if (methodLabel == null) {
            throw new IllegalArgumentException("Unsupported payment method.");
        }

        int total = getCartState().total();
        cart.clear();
        return new PaymentState(String.format("Payment successful via %s for ₹%d.", methodLabel, total));
    }

    private void ensureProductExists(int productId) {
        productCatalogService.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    public record CartState(List<CartItem> items, int total) {
    }

    public record PaymentState(String message) {
    }
}

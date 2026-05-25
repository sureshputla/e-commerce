package com.sureshputla.ecommerce.model;

public record CartItem(Product product, int quantity, int subtotal) {
}

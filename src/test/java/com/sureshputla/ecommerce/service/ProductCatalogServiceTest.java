package com.sureshputla.ecommerce.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductCatalogServiceTest {

    private final ProductCatalogService productCatalogService = new ProductCatalogService();

    @Test
    void shouldFilterProductsByCategoryBrandPriceAndSearch() {
        assertEquals(1, productCatalogService.getProducts("Diapers", "TinyCare", 800, "Dry").size());
    }

    @Test
    void shouldReturnAllProductsWhenFiltersAreAll() {
        assertEquals(8, productCatalogService.getProducts("all", "all", null, null).size());
    }
}

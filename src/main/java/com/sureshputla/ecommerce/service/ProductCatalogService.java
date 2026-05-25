package com.sureshputla.ecommerce.service;

import com.sureshputla.ecommerce.model.Product;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ProductCatalogService {
    private static final List<Product> PRODUCTS = List.of(
            new Product(1, "Soft Dry Diapers - M", "Diapers", "TinyCare", 799),
            new Product(2, "Anti-Colic Feeding Bottle", "Bottles", "MilkNest", 349),
            new Product(3, "Stacking Ring Toy", "Toys", "PlayBud", 499),
            new Product(4, "Battery Jeep Ride-On", "Vehicle Toys", "ZoomKid", 8999),
            new Product(5, "Rocking Cradle", "Cradles", "SleepLeaf", 4999),
            new Product(6, "Cotton Baby Cloth Set", "Clothes", "PureWrap", 699),
            new Product(7, "Training Pants Diapers", "Diapers", "PureWrap", 649),
            new Product(8, "Sipper Bottle", "Bottles", "TinyCare", 279)
    );

    public List<Product> getProducts(String category, String brand, Integer maxPrice, String search) {
        String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        return PRODUCTS.stream()
                .filter(product -> isAll(category) || product.category().equals(category))
                .filter(product -> isAll(brand) || product.brand().equals(brand))
                .filter(product -> maxPrice == null || maxPrice < 0 || product.price() <= maxPrice)
                .filter(product -> normalizedSearch.isBlank() || product.name().toLowerCase(Locale.ROOT).contains(normalizedSearch))
                .toList();
    }

    public List<String> getCategories() {
        return PRODUCTS.stream().map(Product::category).distinct().sorted().toList();
    }

    public List<String> getBrands() {
        return PRODUCTS.stream().map(Product::brand).distinct().sorted().toList();
    }

    public Optional<Product> findById(int id) {
        return PRODUCTS.stream().filter(product -> product.id() == id).findFirst();
    }

    public List<Product> getProductsByIds(Iterable<Integer> ids) {
        return PRODUCTS.stream()
                .filter(product -> {
                    for (Integer id : ids) {
                        if (product.id() == id) {
                            return true;
                        }
                    }
                    return false;
                })
                .sorted(Comparator.comparingInt(Product::id))
                .toList();
    }

    private boolean isAll(String value) {
        return value == null || value.isBlank() || "all".equalsIgnoreCase(value);
    }
}

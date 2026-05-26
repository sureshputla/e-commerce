package com.sureshputla.ecommerce.product.controller;

import com.sureshputla.ecommerce.product.dto.ProductDto;
import com.sureshputla.ecommerce.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Product Catalog", description = "Browse and search products")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/filters")
    @Operation(summary = "Get all filter options (categories and brands)")
    public FilterResponse getFilters() {
        return new FilterResponse(productService.getCategories(), productService.getBrands());
    }

    @GetMapping("/products")
    @Operation(summary = "List products with optional filters")
    public List<ProductDto> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String search) {
        return productService.getProducts(category, brand, maxPrice, search);
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Get a product by ID")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Integer id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public record FilterResponse(List<String> categories, List<String> brands) {}
}


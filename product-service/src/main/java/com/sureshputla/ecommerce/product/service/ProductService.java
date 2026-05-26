package com.sureshputla.ecommerce.product.service;

import com.sureshputla.ecommerce.product.dto.ProductDto;
import com.sureshputla.ecommerce.product.mapper.ProductMapper;
import com.sureshputla.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public List<ProductDto> getProducts(String category, String brand, Integer maxPrice, String search) {
        String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        log.debug("Fetching products – category={}, brand={}, maxPrice={}, search={}", category, brand, maxPrice, search);

        return productRepository.findAll().stream()
                .filter(p -> isAll(category) || p.getCategory().equals(category))
                .filter(p -> isAll(brand) || p.getBrand().equals(brand))
                .filter(p -> maxPrice == null || maxPrice < 0 || p.getPrice() <= maxPrice)
                .filter(p -> normalizedSearch.isBlank()
                        || p.getName().toLowerCase(Locale.ROOT).contains(normalizedSearch))
                .map(productMapper::toDto)
                .toList();
    }

    public Optional<ProductDto> getProductById(Integer id) {
        log.debug("Fetching product by id={}", id);
        return productRepository.findById(id).map(productMapper::toDto);
    }

    public List<String> getCategories() {
        return productRepository.findAll().stream()
                .map(p -> p.getCategory())
                .distinct()
                .sorted()
                .toList();
    }

    public List<String> getBrands() {
        return productRepository.findAll().stream()
                .map(p -> p.getBrand())
                .distinct()
                .sorted()
                .toList();
    }

    private boolean isAll(String value) {
        return value == null || value.isBlank() || "all".equalsIgnoreCase(value);
    }
}


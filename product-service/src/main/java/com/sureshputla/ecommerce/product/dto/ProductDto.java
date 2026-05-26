package com.sureshputla.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product information")
public class ProductDto {

    @Schema(description = "Unique product identifier", example = "1")
    private Integer id;

    @Schema(description = "Product name", example = "Soft Dry Diapers - M")
    private String name;

    @Schema(description = "Product category", example = "Diapers")
    private String category;

    @Schema(description = "Brand name", example = "TinyCare")
    private String brand;

    @Schema(description = "Price in INR", example = "799")
    private Integer price;

    @Schema(description = "Available stock quantity", example = "100")
    private Integer stockQuantity;
}


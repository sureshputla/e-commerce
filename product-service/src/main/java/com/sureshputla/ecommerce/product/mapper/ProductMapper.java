package com.sureshputla.ecommerce.product.mapper;

import com.sureshputla.ecommerce.product.dto.ProductDto;
import com.sureshputla.ecommerce.product.model.Product;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper – zero-boilerplate entity ↔ DTO conversion.
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductDto toDto(Product product);

    List<ProductDto> toDtoList(List<Product> products);

    Product toEntity(ProductDto dto);
}


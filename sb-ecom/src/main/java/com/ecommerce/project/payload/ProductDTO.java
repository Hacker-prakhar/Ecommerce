package com.ecommerce.project.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long productId;
    private String productName;
    private String image;
    @NotBlank(message = "product description can not be blank")
    @Size(min = 6, message = "Product description must contain atleast 6 characters")
    private String description;
    private Integer quantity;
    private Double price;
    private Double discount;
    private Double specialPrice;
}

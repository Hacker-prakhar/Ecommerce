package com.ecommerce.project.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    @Schema(description = "Category ID for a particular category",example = "101")
    private Long categoryId; // if you want to hide categoryId just do not use this field in dto

    @NotBlank(message = "category can not be blank")
    @Size(min = 5, message = "Category name must contain atleast 5 characters")
    private String categoryName;
}


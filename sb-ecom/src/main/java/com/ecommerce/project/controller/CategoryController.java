package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
@Tag(name="Category APIs",description="APIs for managing categories")
    @GetMapping("/public/categories")
    public ResponseEntity<CategoryResponse> getAllCategories(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_CATEGORIES_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        CategoryResponse categoryResponse = categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(categoryResponse, HttpStatus.OK);
    }
    @GetMapping("/echo")
    public ResponseEntity<String> echoMessage(@RequestParam(name = "message", required = false) String message){
        //public ResponseEntity<String> echoMessage(@RequestParam(name = "message", defaultValue = "Hello World!") String message){
        return new ResponseEntity<>("Echoed message: " + message, HttpStatus.OK);
    }
    @Tag(name="Category APIs",description="APIs for managing categories")
    @Operation(summary = "create category" ,description = "API to create category")
    @PostMapping("/public/categories")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully")
    })
    //@RequestMapping(value = "/public/categories", method = RequestMethod.POST)
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO){
        CategoryDTO savedCategory = categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable Long categoryId){

        CategoryDTO categoryDTO = categoryService.deleteCategory(categoryId);
        return new ResponseEntity<>(categoryDTO, HttpStatus.OK);
    }


    @PutMapping("/public/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@Valid @RequestBody CategoryDTO categoryDTO,
                                                 @PathVariable Long categoryId){
        CategoryDTO savedCategory = categoryService.updateCategory( categoryDTO, categoryId);
        return new ResponseEntity<>(savedCategory, HttpStatus.OK);
    }
}

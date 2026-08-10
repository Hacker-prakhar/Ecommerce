package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findByCategory(Category category, Pageable pageable);

    Page<Product> findByProductNameContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Product> findByUser(User user, Pageable pageable);
    Page<Product> findByUser_UserId(Long userId, Pageable pageable);

    @Query("SELECT p FROM Product p INNER JOIN p.category c WHERE c.categoryId = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);

    boolean existsByCategory_CategoryId(Long categoryId);

}

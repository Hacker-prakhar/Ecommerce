package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Long> {
}
// @Query("SELECT P FROM PRODUCT P WHERE P.category=?1")
//    List<Product> findByCategoryId(Long categoryId);
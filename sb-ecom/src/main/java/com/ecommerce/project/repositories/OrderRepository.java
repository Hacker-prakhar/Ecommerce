package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
@Query("SELECT COALESCE(SUM(o.totalAmount) ,0 ) FROM Order o")
Double getTotalRevenue() ;
    List<Order> findByAddress_AddressId(Long addressId);

    @Query(
            value = """
                    select distinct o
                    from Order o
                    join o.orderItems oi
                    join oi.product p
                    where p.user.userId = :sellerId
                    """,
            countQuery = """
                    select count(distinct o)
                    from Order o
                    join o.orderItems oi
                    join oi.product p
                    where p.user.userId = :sellerId
                    """
    )
    Page<Order> findSellerOrders(@Param("sellerId") Long sellerId, Pageable pageable);
}

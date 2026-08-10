package com.ecommerce.project.service;

import com.ecommerce.project.payload.AnalyticsResponse;
import com.ecommerce.project.repositories.OrderRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public AnalyticsResponse getAnalyticsData() {
        long productCount = productRepository.count();
        long totalOrders = orderRepository.count();
        Double totalRevenue = orderRepository.getTotalRevenue();

        AnalyticsResponse response = new AnalyticsResponse();
        response.setProductCount(String.valueOf(productCount));
        response.setTotalOrders(String.valueOf(totalOrders));
        response.setRevenue(String.valueOf(totalRevenue != null ? totalRevenue : 0));
        return response;
    }
}

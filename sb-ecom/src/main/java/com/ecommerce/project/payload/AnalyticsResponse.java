package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsResponse {
    private String productCount ;
    private String revenue ;
private String totalOrders ;

    public void setProductCount(String productCount) {
        this.productCount = productCount;
    }

    public String getTotalOrders() {
        return totalOrders;
    }

    public String getRevenue() {
        return revenue;
    }

    public void setTotalOrders(String totalOrders) {
        this.totalOrders = totalOrders;
    }

    public void setRevenue(String revenue) {
        this.revenue = revenue;
    }

    public String getProductCount() {
        return productCount;
    }
}

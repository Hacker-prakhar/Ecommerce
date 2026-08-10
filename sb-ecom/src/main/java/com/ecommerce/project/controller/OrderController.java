package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.*;
import com.ecommerce.project.service.OrderService;
import com.ecommerce.project.service.StripeService;
import com.ecommerce.project.util.AuthUtil;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {
    @Autowired
    AuthUtil authUtil;
    @Autowired
    OrderService orderService ;
    @Autowired
    StripeService stripeService ;
    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderDTO> orderProducts(@PathVariable String paymentMethod, @RequestBody OrderRequestDTO orderRequestDTO) {
        String emailId = authUtil.loggedInEmail();
        OrderDTO order = orderService.placeOrder(
                emailId,
                orderRequestDTO.getAddressId(),
                paymentMethod,
                orderRequestDTO.getPgName(),
                orderRequestDTO.getPgPaymentId(),
                orderRequestDTO.getPgStatus(),
                orderRequestDTO.getPgResponseMessage()
        );
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }
@PostMapping("/order/stripe-client-secret")
    public ResponseEntity<String> stripeClientSecret(@RequestBody StripePaymentDTO stripePaymentDTO) {
        try {
            PaymentIntent paymentIntent = stripeService.paymentIntent(stripePaymentDTO);
            return new ResponseEntity<>(paymentIntent.getClientSecret(), HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }
    @GetMapping("/admin/orders")
    public ResponseEntity<OrderResponse> getAllOrders(
            @RequestParam(name="keyword" , required=false) String keyword ,
            @RequestParam(name="category" , required=false) String category ,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_ORDERS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ){
        OrderResponse orderResponse = orderService.getAllOrders(pageNumber,pageSize,sortBy,sortOrder) ;
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }
    @PutMapping("/admin/orders/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody OrderStatusUpdateDTO orderStatusUpdateDto
    ) {
        System.out.println(orderStatusUpdateDto.getOrderStatus());
        OrderDTO order = orderService.updateOrder(orderId, orderStatusUpdateDto.getOrderStatus());
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @GetMapping("/seller/orders")
    public ResponseEntity<OrderResponse> getAllSellerOrders(
            @RequestParam(name="keyword" , required=false) String keyword ,
            @RequestParam(name="category" , required=false) String category ,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_ORDERS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ){
        OrderResponse orderResponse = orderService.getAllSellerOrders(pageNumber,pageSize,sortBy,sortOrder) ;
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }
}

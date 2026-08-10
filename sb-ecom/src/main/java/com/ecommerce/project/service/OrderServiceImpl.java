package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderItemDTO;
import com.ecommerce.project.payload.OrderResponse;
import com.ecommerce.project.repositories.*;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    CartService cartService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    ProductRepository productRepository;
@Autowired
AuthUtil authUtil;
    @Override
    @Transactional
    public OrderDTO placeOrder(String emailId, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage) {
        Cart cart = cartRepository.findCartByEmail(emailId);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "email", emailId);
        }

        List<CartItem> cartItems = new ArrayList<>(cart.getCartItems());
        if (cartItems.isEmpty()) {
            throw new APIException("Cart is empty");
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        Order order = new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted !");
        order.setAddress(address);

        Payment payment = new Payment(paymentMethod, pgPaymentId, pgStatus, pgResponseMessage, pgName);
        payment.setOrder(order);
        payment = paymentRepository.save(payment);
        order.setPayment(payment);

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItem);
        }

        orderItems = orderItemRepository.saveAll(orderItems);
        savedOrder.setOrderItems(orderItems);

        cartItems.forEach(item -> {
            // Remove items from cart
            cartService.deleteProductFromCart(cart.getCartId(), item.getProduct().getProductId());
        });

        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
        orderDTO.setOrderdate(savedOrder.getOrderDate());
        orderDTO.setOrderItems(new ArrayList<>());
        orderItems.forEach(item -> {
            OrderItemDTO orderItemDTO = modelMapper.map(item, OrderItemDTO.class);
            orderItemDTO.setFinalPrice(item.getOrderedProductPrice() * item.getQuantity());
            orderDTO.getOrderItems().add(orderItemDTO);
        });

        orderDTO.setAddressId(addressId);

        return orderDTO;
    }
    @Override
    @Transactional
    public OrderResponse getAllOrders(Integer pageNumber, Integer pageSize , String sortBy , String sortOrder){
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Order> pageOrders = orderRepository.findAll(pageDetails);
        List<OrderDTO> orderDTOs = pageOrders.getContent().stream()
                .map(this::mapToOrderDTO)
                .toList();
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setContent(orderDTOs);
        orderResponse.setTotalPages(pageOrders.getTotalPages());
        orderResponse.setTotalElements(pageOrders.getTotalElements());
        orderResponse.setPageNumber(pageNumber);
        orderResponse.setPageSize(pageSize);
        orderResponse.setLastPage(pageOrders.isLast());
        return orderResponse;
    }

    private OrderDTO mapToOrderDTO(Order order) {
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
        orderDTO.setOrderdate(order.getOrderDate());
        orderDTO.setOrderItems(order.getOrderItems().stream().map(orderItem -> {
            OrderItemDTO orderItemDTO = modelMapper.map(orderItem, OrderItemDTO.class);
            orderItemDTO.setFinalPrice(orderItem.getOrderedProductPrice() * orderItem.getQuantity());
            return orderItemDTO;
        }).toList());
        return orderDTO;
    }
    @Override
    public OrderDTO updateOrder( Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                        .orElseThrow(() -> new APIException("Order not found"+orderId) );
        order.setOrderStatus(status);
        order = orderRepository.save(order);
        return modelMapper.map(order, OrderDTO.class);

    }
    @Override
    public OrderResponse getAllSellerOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        User seller = authUtil.loggedInUser();

        Page<Order> pageOrders = orderRepository.findSellerOrders(seller.getUserId(), pageDetails);

        List<OrderDTO> orderDTOs = pageOrders.getContent().stream()
                .map(this::mapToOrderDTO)
                .toList();
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setContent(orderDTOs);
        orderResponse.setPageNumber(pageOrders.getNumber());
        orderResponse.setPageSize(pageOrders.getSize());
        orderResponse.setTotalElements(pageOrders.getTotalElements());
        orderResponse.setTotalPages(pageOrders.getTotalPages());
        orderResponse.setLastPage(pageOrders.isLast());
        return orderResponse;
    }

}


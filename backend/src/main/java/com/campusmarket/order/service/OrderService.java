package com.campusmarket.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campusmarket.order.dto.CreateOrderRequest;
import com.campusmarket.order.dto.OrderVO;
import com.campusmarket.order.entity.Order;

public interface OrderService extends IService<Order> {

    OrderVO createOrder(CreateOrderRequest request, Long buyerId);

    OrderVO payOrder(Long orderId, Long buyerId);

    OrderVO shipOrder(Long orderId, Long sellerId);

    OrderVO completeOrder(Long orderId, Long buyerId);

    void cancelOrder(Long orderId, Long buyerId);

    Page<OrderVO> listBuyerOrders(int page, int size, Long buyerId);

    Page<OrderVO> listSellerOrders(int page, int size, Long sellerId);

    OrderVO getOrderDetail(Long orderId, Long userId);
}

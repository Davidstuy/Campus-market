package com.campusmarket.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusmarket.common.response.ApiResult;
import com.campusmarket.common.response.PageResult;
import com.campusmarket.order.dto.CreateOrderRequest;
import com.campusmarket.order.dto.OrderVO;
import com.campusmarket.order.service.OrderService;
import com.campusmarket.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResult<OrderVO> create(@Valid @RequestBody CreateOrderRequest request,
                                     HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        return ApiResult.success(orderService.createOrder(request, userId));
    }

    @PutMapping("/{id}/pay")
    public ApiResult<OrderVO> pay(@PathVariable Long id,
                                   HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        return ApiResult.success(orderService.payOrder(id, userId));
    }

    @PutMapping("/{id}/ship")
    public ApiResult<OrderVO> ship(@PathVariable Long id,
                                    HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        return ApiResult.success(orderService.shipOrder(id, userId));
    }

    @PutMapping("/{id}/complete")
    public ApiResult<OrderVO> complete(@PathVariable Long id,
                                        HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        return ApiResult.success(orderService.completeOrder(id, userId));
    }

    @PutMapping("/{id}/cancel")
    public ApiResult<Void> cancel(@PathVariable Long id,
                                   HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        orderService.cancelOrder(id, userId);
        return ApiResult.success(null);
    }

    @GetMapping("/buy")
    public ApiResult<PageResult<OrderVO>> listBuy(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Page<OrderVO> result = orderService.listBuyerOrders(page, size, userId);
        return ApiResult.success(PageResult.of(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @GetMapping("/sell")
    public ApiResult<PageResult<OrderVO>> listSell(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Page<OrderVO> result = orderService.listSellerOrders(page, size, userId);
        return ApiResult.success(PageResult.of(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResult<OrderVO> detail(@PathVariable Long id,
                                      HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        return ApiResult.success(orderService.getOrderDetail(id, userId));
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return user.getId();
    }
}

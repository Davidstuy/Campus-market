package com.campusmarket.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.common.exception.BusinessException;
import com.campusmarket.order.dto.CreateOrderRequest;
import com.campusmarket.order.dto.OrderVO;
import com.campusmarket.order.entity.Order;
import com.campusmarket.order.mapper.OrderMapper;
import com.campusmarket.order.service.OrderService;
import com.campusmarket.product.entity.Product;
import com.campusmarket.product.mapper.ProductMapper;
import com.campusmarket.user.entity.User;
import com.campusmarket.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public OrderVO createOrder(CreateOrderRequest request, Long buyerId) {
        Product product = productMapper.selectById(request.getProductId());
        if (product == null || !"ACTIVE".equals(product.getStatus())) {
            throw new BusinessException(404, "商品不存在或已下架");
        }
        if (product.getSellerId().equals(buyerId)) {
            throw new BusinessException(400, "不能购买自己的商品");
        }

        // 检查是否已有未支付订单
        long pendingCount = this.count(new LambdaQueryWrapper<Order>()
                .eq(Order::getProductId, request.getProductId())
                .eq(Order::getBuyerId, buyerId)
                .eq(Order::getStatus, "PENDING"));
        if (pendingCount > 0) {
            throw new BusinessException(400, "已有待支付订单，请完成或取消后再下单");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setBuyerId(buyerId);
        order.setSellerId(product.getSellerId());
        order.setProductId(product.getId());
        order.setProductTitle(product.getTitle());
        order.setProductPrice(product.getPrice());
        order.setProductCover(product.getCoverImage());
        order.setStatus("PENDING");
        order.setBuyerRemark(request.getBuyerRemark());

        this.save(order);

        return buildOrderVO(order);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"products", "product_detail"}, allEntries = true)
    public OrderVO payOrder(Long orderId, Long buyerId) {
        Order order = getAndCheckOwnership(orderId, buyerId, true);
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException(400, "订单状态不允许支付");
        }

        order.setStatus("PAID");
        order.setPaidAt(LocalDateTime.now());
        this.updateById(order);

        // 支付成功 → 商品自动售出
        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            product.setStatus("SOLD");
            productMapper.updateById(product);
        }

        return buildOrderVO(order);
    }

    @Override
    @Transactional
    public OrderVO shipOrder(Long orderId, Long sellerId) {
        Order order = getAndCheckOwnership(orderId, sellerId, false);
        // shipOrder is for seller, so check sellerId
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException(403, "只能操作自己的订单");
        }
        if (!"PAID".equals(order.getStatus())) {
            throw new BusinessException(400, "只有已付款的订单才能发货");
        }

        order.setStatus("SHIPPED");
        order.setShippedAt(LocalDateTime.now());
        this.updateById(order);

        return buildOrderVO(order);
    }

    @Override
    @Transactional
    public OrderVO completeOrder(Long orderId, Long buyerId) {
        Order order = getAndCheckOwnership(orderId, buyerId, true);
        if (!"SHIPPED".equals(order.getStatus())) {
            throw new BusinessException(400, "只有已发货的订单才能确认收货");
        }

        order.setStatus("COMPLETED");
        order.setCompletedAt(LocalDateTime.now());
        this.updateById(order);

        return buildOrderVO(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long buyerId) {
        Order order = getAndCheckOwnership(orderId, buyerId, true);
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException(400, "只有待付款的订单才能取消");
        }

        order.setStatus("CANCELLED");
        order.setCancelledAt(LocalDateTime.now());
        this.updateById(order);
    }

    @Override
    public Page<OrderVO> listBuyerOrders(int pageNum, int size, Long buyerId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getBuyerId, buyerId);
        wrapper.orderByDesc(Order::getCreatedAt);

        return queryOrderPage(pageNum, size, wrapper, true);
    }

    @Override
    public Page<OrderVO> listSellerOrders(int pageNum, int size, Long sellerId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getSellerId, sellerId);
        wrapper.orderByDesc(Order::getCreatedAt);

        return queryOrderPage(pageNum, size, wrapper, false);
    }

    @Override
    public OrderVO getOrderDetail(Long orderId, Long userId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException(403, "无权查看此订单");
        }

        return buildOrderVO(order);
    }

    // ===== 内部方法 =====

    private Order getAndCheckOwnership(Long orderId, Long userId, boolean isBuyer) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        Long expectedOwner = isBuyer ? order.getBuyerId() : order.getSellerId();
        if (!expectedOwner.equals(userId)) {
            throw new BusinessException(403, "无权操作此订单");
        }
        return order;
    }

    private Page<OrderVO> queryOrderPage(int pageNum, int size,
                                         LambdaQueryWrapper<Order> wrapper, boolean fetchSeller) {
        long total = this.count(wrapper);
        int offset = (pageNum - 1) * size;
        wrapper.last("LIMIT " + offset + ", " + size);

        List<Order> records = this.list(wrapper);

        if (records.isEmpty()) {
            Page<OrderVO> empty = new Page<>(pageNum, size, 0);
            empty.setRecords(List.of());
            return empty;
        }

        // 批量查询关联用户
        List<Long> userIds = fetchSeller
                ? records.stream().map(Order::getSellerId).distinct().toList()
                : records.stream().map(Order::getBuyerId).distinct().toList();

        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .peek(u -> u.setPassword(null))
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<OrderVO> voList = records.stream().map(order -> {
            OrderVO vo = new OrderVO();
            copyFields(order, vo);
            Long userId = fetchSeller ? order.getSellerId() : order.getBuyerId();
            if (fetchSeller) {
                vo.setSeller(userMap.get(userId));
            } else {
                vo.setBuyer(userMap.get(userId));
            }
            return vo;
        }).toList();

        Page<OrderVO> voPage = new Page<>(pageNum, size, total);
        voPage.setRecords(voList);
        return voPage;
    }

    private OrderVO buildOrderVO(Order order) {
        OrderVO vo = new OrderVO();
        copyFields(order, vo);

        User buyer = userMapper.selectById(order.getBuyerId());
        if (buyer != null) buyer.setPassword(null);
        vo.setBuyer(buyer);

        User seller = userMapper.selectById(order.getSellerId());
        if (seller != null) seller.setPassword(null);
        vo.setSeller(seller);

        return vo;
    }

    private void copyFields(Order src, OrderVO dst) {
        dst.setId(src.getId());
        dst.setOrderNo(src.getOrderNo());
        dst.setBuyerId(src.getBuyerId());
        dst.setSellerId(src.getSellerId());
        dst.setProductId(src.getProductId());
        dst.setProductTitle(src.getProductTitle());
        dst.setProductPrice(src.getProductPrice());
        dst.setProductCover(src.getProductCover());
        dst.setStatus(src.getStatus());
        dst.setBuyerRemark(src.getBuyerRemark());
        dst.setPaidAt(src.getPaidAt());
        dst.setShippedAt(src.getShippedAt());
        dst.setCompletedAt(src.getCompletedAt());
        dst.setCancelledAt(src.getCancelledAt());
        dst.setCreatedAt(src.getCreatedAt());
        dst.setUpdatedAt(src.getUpdatedAt());
    }

    private String generateOrderNo() {
        String ts = String.valueOf(System.currentTimeMillis());
        String rand = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "ORD" + ts + rand;
    }
}

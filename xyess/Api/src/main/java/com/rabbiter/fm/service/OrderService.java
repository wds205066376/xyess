package com.rabbiter.fm.service;

import com.rabbiter.fm.model.OrderModel;
import com.rabbiter.fm.vo.PageVo;

import java.util.List;

public interface OrderService {

    /**
     * 新增订单
     * @param orderModel
     * @return
     */
    boolean addOrder(OrderModel orderModel);

    /**
     * 获取订单信息
     * @param id
     * @return
     */
    OrderModel getOrder(Long id);

    /**
     * 更新订单信息
     * @param orderModel
     * @return
     */
    boolean updateOrder(OrderModel orderModel);

    /**
     * 获取某个用户买到的闲置的订单列表
     * @param userId
     * @return
     */
    List<OrderModel> getMyOrder(Long userId);

    /**
     * 获取某个用户卖出的闲置的订单信息
     * @param userId
     * @return
     */
    List<OrderModel> getMySoldIdle(Long userId);

    PageVo<OrderModel> getAllOrder(int page, int nums);

    boolean deleteOrder(long id);
}

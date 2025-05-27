package com.rabbiter.fm.controller;

import com.rabbiter.fm.common.enums.ErrorMsg;
import com.rabbiter.fm.model.OrderModel;
import com.rabbiter.fm.model.OrderAddressModel;
import com.rabbiter.fm.service.OrderService;
import com.rabbiter.fm.service.OrderAddressService;
import com.rabbiter.fm.common.utils.IdFactoryUtil;
import com.rabbiter.fm.common.utils.OrderTaskHandler;
import com.rabbiter.fm.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@CrossOrigin
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderAddressService orderAddressService;

    @PostMapping("/add")
    public ResultVo addOrder(@RequestBody OrderModel orderModel) {
        if (OrderTaskHandler.orderService == null) {
            OrderTaskHandler.orderService = orderService;
        }
        orderModel.setOrderNumber(IdFactoryUtil.getOrderId());
        orderModel.setCreateTime(new Date());
        orderModel.setOrderStatus((byte) 0);
        orderModel.setPaymentStatus((byte) 0);
        if (orderService.addOrder(orderModel)) {
            return ResultVo.success(orderModel);
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @GetMapping("/info")
    public ResultVo getOrderInfo(@RequestParam Long id, @RequestParam Long userId) {
        OrderModel orderModel = orderService.getOrder(id);
        if (orderModel.getUserId().equals(userId) ||
                orderModel.getIdleItem().getUserId().equals(userId)) {
            // 获取订单地址信息
            OrderAddressModel address = orderAddressService.getOrderAddress(id);
            if (address != null) {
                orderModel.setAddress(address);
            }
            return ResultVo.success(orderModel);
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @PostMapping("/update")
    public ResultVo updateOrder(@RequestBody OrderModel orderModel) {
        if (orderModel.getPaymentStatus() != null && orderModel.getPaymentStatus().equals((byte) 1)) {
            orderModel.setPaymentTime(new Date());
        }
        if (orderService.updateOrder(orderModel)) {
            return ResultVo.success(orderModel);
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @GetMapping("/my")
    public ResultVo getMyOrder(@RequestParam Long userId) {
        return ResultVo.success(orderService.getMyOrder(userId));
    }

    @GetMapping("/my-sold")
    public ResultVo getMySoldIdle(@RequestParam Long userId) {
        return ResultVo.success(orderService.getMySoldIdle(userId));
    }

    @PostMapping("/delete")
    public ResultVo deleteOrder(@RequestParam Long id) {
        if (orderService.deleteOrder(id)) {
            return ResultVo.success();
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }
}

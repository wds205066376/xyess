package com.rabbiter.fm.controller;

import com.rabbiter.fm.service.IdleItemService;
import com.rabbiter.fm.common.enums.ErrorMsg;
import com.rabbiter.fm.model.IdleItemModel;
import com.rabbiter.fm.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Date;


@CrossOrigin
@RestController
@RequestMapping("idle")
public class IdleItemController {

    @Autowired
    private IdleItemService idleItemService;

    @PostMapping("add")
    public ResultVo addIdleItem(@RequestBody IdleItemModel idleItem) {
        try {
            if (idleItem.getUserId() == null) {
                return ResultVo.fail(ErrorMsg.PARAM_ERROR);
            }

            // 设置创建时间和状态
            idleItem.setReleaseTime(new Date());
            idleItem.setIdleStatus((byte) 1);

            // 保存商品信息
            if (idleItemService.addIdleItem(idleItem)) {
                return ResultVo.success(idleItem);
            }
            return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
        }
    }

    @GetMapping("info")
    public ResultVo getIdleItem(@RequestParam Long id) {
        return ResultVo.success(idleItemService.getIdleItem(id));
    }

    @GetMapping("all")
    public ResultVo getAllIdleItem(
            @RequestParam Long userId,
            @RequestParam(required = false) Byte idleStatus) {
        return ResultVo.success(idleItemService.getAllIdelItem(userId, idleStatus));
    }

    @GetMapping("find")
    public ResultVo findIdleItem(@RequestParam(value = "findValue", required = false) String findValue,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "nums", required = false) Integer nums) {
        if (null == findValue) {
            findValue = "";
        }
        int p = 1;
        int n = 8;
        if (null != page) {
            p = page > 0 ? page : 1;
        }
        if (null != nums) {
            n = nums > 0 ? nums : 8;
        }
        return ResultVo.success(idleItemService.findIdleItem(findValue, p, n));
    }

    @GetMapping("lable")
    public ResultVo findIdleItemByLable(@RequestParam(value = "idleLabel", required = true) Integer idleLabel,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "nums", required = false) Integer nums) {
        int p = 1;
        int n = 8;
        if (null != page) {
            p = page > 0 ? page : 1;
        }
        if (null != nums) {
            n = nums > 0 ? nums : 8;
        }
        return ResultVo.success(idleItemService.findIdleItemByLable(idleLabel, p, n));
    }

    @PostMapping("update")
    public ResultVo updateIdleItem(@RequestBody IdleItemModel idleItemModel) {
        try {
            if (idleItemModel.getId() == null) {
                return ResultVo.fail(ErrorMsg.PARAM_ERROR);
            }

            if (idleItemService.updateIdleItem(idleItemModel)) {
                return ResultVo.success();
            }
            return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
        }
    }
}

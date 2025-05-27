package com.rabbiter.fm.controller;

import com.rabbiter.fm.service.MessageService;
import com.rabbiter.fm.service.IdleItemService;
import com.rabbiter.fm.common.enums.ErrorMsg;
import com.rabbiter.fm.model.MessageModel;
import com.rabbiter.fm.model.IdleItemModel;
import com.rabbiter.fm.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@CrossOrigin
@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private IdleItemService idleItemService;

    @PostMapping("/send")
    public ResultVo sendMessage(@RequestBody MessageModel messageModel) {
        messageModel.setCreateTime(new Date());
        if (messageModel.getToMessage() == null) {
            messageModel.setToMessage(0L);
        }
        messageModel.setIsRead(false);
        if (messageService.addMessage(messageModel)) {
            return ResultVo.success(messageModel);
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @GetMapping("/info")
    public ResultVo getMessage(@RequestParam Long id) {
        return ResultVo.success(messageService.getMessage(id));
    }

    @GetMapping("/idle")
    public ResultVo getAllIdleMessage(
            @RequestParam Long idleId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        try {
            // 获取总数
            List<MessageModel> allMessages = messageService.getAllIdleMessage(idleId);
            int total = allMessages.size();

            // 计算分页
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, total);

            // 获取当前页的数据
            List<MessageModel> pageMessages = allMessages.subList(start, end);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageMessages);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);

            return ResultVo.success(result);
        } catch (Exception e) {
            return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
        }
    }

    @GetMapping("/my")
    public ResultVo getAllMyMessage(@RequestParam Long userId) {
        return ResultVo.success(messageService.getAllMyMessage(userId));
    }

    @GetMapping("/delete")
    public ResultVo deleteMessage(
            @RequestParam Long id,
            @RequestParam Long userId) {
        try {
            // 获取留言信息
            MessageModel message = messageService.getMessage(id);
            if (message == null) {
                return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
            }

            // 验证权限（留言发送者或商品发布者可以删除）
            IdleItemModel idleItem = idleItemService.getIdleItem(message.getIdleId());
            if (!message.getUserId().equals(userId) && !idleItem.getUserId().equals(userId)) {
                return ResultVo.fail(ErrorMsg.PARAM_ERROR, "没有权限删除此消息");
            }

            // 执行删除
            if (messageService.deleteMessage(id)) {
                return ResultVo.success();
            }
            return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
        }
    }

    @PostMapping("/read")
    public ResultVo updateMessageStatus(@RequestParam Long messageId) {
        try {
            MessageModel message = messageService.getMessage(messageId);
            if (message == null) {
                return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
            }
            message.setIsRead(true);
            if (messageService.addMessage(message)) {
                return ResultVo.success();
            }
            return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
        } catch (Exception e) {
            return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
        }
    }

    @GetMapping("/unread-count")
    public ResultVo getUnreadMessageCount(@RequestParam Long userId) {
        try {
            List<MessageModel> messages = messageService.getAllMyMessage(userId);
            long count = messages.stream()
                    .filter(msg -> msg.getIsRead() == null || !msg.getIsRead())
                    .count();
            return ResultVo.success((int) count);
        } catch (Exception e) {
            return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
        }
    }
}

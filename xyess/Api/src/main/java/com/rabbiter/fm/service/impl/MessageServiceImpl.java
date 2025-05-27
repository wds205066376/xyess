package com.rabbiter.fm.service.impl;

import com.rabbiter.fm.service.MessageService;
import com.rabbiter.fm.dao.IdleItemDao;
import com.rabbiter.fm.dao.MessageDao;
import com.rabbiter.fm.dao.UserDao;
import com.rabbiter.fm.model.IdleItemModel;
import com.rabbiter.fm.model.MessageModel;
import com.rabbiter.fm.model.UserModel;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageServiceImpl implements MessageService {

    @Resource
    private MessageDao messageDao;

    @Resource
    private UserDao userDao;

    @Resource
    private IdleItemDao idleItemDao;

    /**
     * 增加一条留言
     * 
     * @param messageModel
     * @return
     */
    public boolean addMessage(MessageModel messageModel) {
        return messageDao.insert(messageModel) == 1;
    }

    /**
     * 删除一条留言，未做用户身份验证
     * 
     * @param id
     * @return
     */
    public boolean deleteMessage(Long id) {
        return messageDao.deleteByPrimaryKey(id) == 1;
    }

    /**
     * 获取一条留言
     * 
     * @param id
     * @return
     */
    public MessageModel getMessage(Long id) {
        return messageDao.selectByPrimaryKey(id);
    }

    /**
     * 获取一个用户收到的所有留言，未做分页查询
     * 同时查询出用户的信息和闲置的信息
     * userId建索引
     * 
     * @param userId
     * @return
     */
    public List<MessageModel> getAllMyMessage(Long userId) {
        List<MessageModel> list = messageDao.getMyMessage(userId);
        if (list.size() > 0) {
            List<Long> idList = new ArrayList<>();
            for (MessageModel i : list) {
                idList.add(i.getUserId());
            }
            List<UserModel> userList = userDao.findUserByList(idList);
            Map<Long, UserModel> map = new HashMap<>();
            for (UserModel user : userList) {
                map.put(user.getId(), user);
            }
            for (MessageModel i : list) {
                i.setFromU(map.get(i.getUserId()));
            }

            List<Long> idleIdList = new ArrayList<>();
            for (MessageModel i : list) {
                idleIdList.add(i.getIdleId());
            }
            List<IdleItemModel> idleList = idleItemDao.findIdleByList(idleIdList);
            Map<Long, IdleItemModel> idleMap = new HashMap<>();
            for (IdleItemModel idle : idleList) {
                idleMap.put(idle.getId(), idle);
            }
            for (MessageModel i : list) {
                i.setIdle(idleMap.get(i.getIdleId()));
            }
        }
        return list;
    }

    /**
     * 查询一个闲置下的所有留言，未做分页
     * 同时查出发送者和接收者的信息
     * idleId建索引
     * 
     * @param idleId
     * @return
     */
    public List<MessageModel> getAllIdleMessage(Long idleId) {
        List<MessageModel> list = messageDao.getIdleMessage(idleId);
        if (list != null && list.size() > 0) {
            // 获取所有用户ID
            List<Long> idList = new ArrayList<>();
            for (MessageModel i : list) {
                idList.add(i.getUserId());
                if (i.getToUser() != null) {
                    idList.add(i.getToUser());
                }
            }

            // 获取用户信息
            List<UserModel> userList = userDao.findUserByList(idList);
            Map<Long, UserModel> userMap = new HashMap<>();
            for (UserModel user : userList) {
                userMap.put(user.getId(), user);
            }

            // 建立消息ID到消息的映射
            Map<Long, MessageModel> mesMap = new HashMap<>();
            for (MessageModel i : list) {
                mesMap.put(i.getId(), i);
            }

            // 处理每条消息
            for (MessageModel i : list) {
                // 设置发送者信息
                UserModel fromUser = userMap.get(i.getUserId());
                if (fromUser != null) {
                    i.setFromU(fromUser);
                } else {
                    i.setFromU(new UserModel());
                }

                // 只有在回复消息时才设置回复相关信息
                if (i.getToMessage() != null && i.getToMessage() != 0) {
                    // 初始化回复相关信息
                    MessageModel toM = new MessageModel();
                    UserModel toU = new UserModel();

                    // 设置被回复消息的内容和用户信息
                    MessageModel replyTo = mesMap.get(i.getToMessage());
                    if (replyTo != null) {
                        toM.setContent(replyTo.getContent());
                    }

                    UserModel replyToUser = userMap.get(i.getToUser());
                    if (replyToUser != null) {
                        toU.setNickname(replyToUser.getNickname());
                    }

                    i.setToM(toM);
                    i.setToU(toU);
                } else {
                    // 如果不是回复消息，则不设置回复相关信息
                    i.setToM(null);
                    i.setToU(null);
                }
            }
        }
        return list;
    }

    @Override
    public boolean updateMessageStatus(Long messageId) {
        MessageModel message = getMessage(messageId);
        if (message != null) {
            message.setIsRead(true);
            return messageDao.updateByPrimaryKey(message) == 1;
        }
        return false;
    }

    @Override
    public int getUnreadMessageCount(Long userId) {
        List<MessageModel> messages = getAllMyMessage(userId);
        return (int) messages.stream()
                .filter(msg -> msg.getIsRead() == null || !msg.getIsRead())
                .count();
    }
}

package com.rabbiter.fm.service.impl;

import com.rabbiter.fm.dao.SearchHistoryDao;
import com.rabbiter.fm.model.SearchHistoryModel;
import com.rabbiter.fm.service.SearchHistoryService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

@Service
public class SearchHistoryServiceImpl implements SearchHistoryService {

    @Resource
    private SearchHistoryDao searchHistoryDao;

    @Override
    public void addHistory(Long userId, String keyword) {
        // 先检查是否存在相同的搜索记录
        SearchHistoryModel existingHistory = searchHistoryDao.checkExist(userId, keyword);
        if (existingHistory == null) {
            // 不存在才添加新记录
            SearchHistoryModel history = new SearchHistoryModel();
            history.setUserId(userId);
            history.setKeyword(keyword);
            searchHistoryDao.insert(history);
        }
    }

    @Override
    public List<SearchHistoryModel> getRecentHistory(Long userId, Integer limit) {
        return searchHistoryDao.getRecentHistory(userId, limit);
    }

    @Override
    public boolean deleteHistory(Long userId, Long id) {
        return searchHistoryDao.deleteHistory(userId, id) > 0;
    }

    @Override
    public boolean clearHistory(Long userId) {
        return searchHistoryDao.clearHistory(userId) > 0;
    }
}
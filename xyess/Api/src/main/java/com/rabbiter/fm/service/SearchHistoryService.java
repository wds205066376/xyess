package com.rabbiter.fm.service;

import com.rabbiter.fm.model.SearchHistoryModel;
import java.util.List;

public interface SearchHistoryService {
    void addHistory(Long userId, String keyword);

    List<SearchHistoryModel> getRecentHistory(Long userId, Integer limit);

    boolean deleteHistory(Long userId, Long id);

    boolean clearHistory(Long userId);
}
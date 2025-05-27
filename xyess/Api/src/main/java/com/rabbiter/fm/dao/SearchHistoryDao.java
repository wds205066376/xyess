package com.rabbiter.fm.dao;

import com.rabbiter.fm.model.SearchHistoryModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SearchHistoryDao {
    int insert(SearchHistoryModel history);

    List<SearchHistoryModel> getRecentHistory(@Param("userId") Long userId, @Param("limit") Integer limit);

    int deleteHistory(@Param("userId") Long userId, @Param("id") Long id);

    int clearHistory(@Param("userId") Long userId);

    SearchHistoryModel checkExist(@Param("userId") Long userId, @Param("keyword") String keyword);
}
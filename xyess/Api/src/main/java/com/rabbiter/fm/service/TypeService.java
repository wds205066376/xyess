package com.rabbiter.fm.service;

import com.rabbiter.fm.model.TypeModel;

import java.util.List;

public interface TypeService {

    List<TypeModel> listByCondition(int begin, int nums);

    TypeModel getById(Long id);

    boolean deleteById(Long id);

    boolean addType(TypeModel typeModel);

    boolean updateType(TypeModel typeModel);

    /**
     * 获取分类总数
     * 
     * @return 总数
     */
    int getTotal();
}

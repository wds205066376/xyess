package com.rabbiter.fm.service.impl;

import com.rabbiter.fm.dao.IdleItemDao;
import com.rabbiter.fm.dao.TypeDao;
import com.rabbiter.fm.model.TypeModel;
import com.rabbiter.fm.service.TypeService;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class TypeServiceImpl implements TypeService {
    @Resource
    private TypeDao typeDao;

    @Resource
    private IdleItemDao idleItemDao;

    @Override
    public List<TypeModel> listByCondition(int begin, int nums) {
        return typeDao.listByCondition(begin, nums);
    }

    @Override
    public TypeModel getById(Long id) {
        if (ObjectUtils.isEmpty(id)) {
            return null;
        }
        return typeDao.selectByPrimaryKey(id);
    }

    @Override
    public boolean deleteById(Long id) {
        if (ObjectUtils.isEmpty(id)) {
            return false;
        }
        return typeDao.deleteByPrimaryKey(id) > 0;
    }

    @Override
    public boolean addType(TypeModel typeModel) {
        if (ObjectUtils.isEmpty(typeModel) || ObjectUtils.isEmpty(typeModel.getName())) {
            return false;
        }
        typeModel.setCreateTime(new Date());
        return typeDao.insert(typeModel) > 0;
    }

    @Override
    public boolean updateType(TypeModel typeModel) {
        if (ObjectUtils.isEmpty(typeModel) || ObjectUtils.isEmpty(typeModel.getId())) {
            return false;
        }
        return typeDao.updateByPrimaryKey(typeModel) > 0;
    }

    @Override
    public int getTotal() {
        return typeDao.getTotal();
    }
}

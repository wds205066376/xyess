package com.rabbiter.fm.controller;

import com.rabbiter.fm.model.TypeModel;
import com.rabbiter.fm.service.TypeService;
import com.rabbiter.fm.vo.ResultVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品类型管理控制器
 * 提供商品分类相关的核心功能接口：
 * 1. 分类列表查询
 * 2. 分类详情获取
 * 3. 分类添加
 * 4. 分类更新
 * 5. 分类删除
 * 
 * 特点：
 * - RESTful API设计
 * - 分页查询支持
 * - 参数验证
 * - 统一响应格式
 */
@RestController
@RequestMapping("/type")
public class TypeController {
    @Resource
    private TypeService typeService;

    /**
     * 获取分类列表接口
     * 功能：
     * 1. 支持分页查询
     * 2. 返回分类列表
     * 3. 包含分页信息
     * 
     * @param begin 起始位置
     * @param size  每页大小
     * @return 分类列表
     */
    @GetMapping("listByCondition")
    public ResultVo listByCondition(Integer begin, Integer size) {
        return ResultVo.success(typeService.listByCondition(begin, size));
    }

    /**
     * 获取分类详情接口
     * 功能：
     * 1. 根据ID获取分类信息
     * 2. 返回分类详细信息
     * 
     * @param id 分类ID
     * @return 分类详情
     */
    @GetMapping("getById")
    public ResultVo getById(Long id) {
        return ResultVo.success(typeService.getById(id));
    }

    /**
     * 添加分类接口
     * 功能：
     * 1. 验证分类信息
     * 2. 创建新分类
     * 3. 返回创建结果
     * 
     * @param typeModel 分类信息
     * @return 创建结果
     */
    @PostMapping("add")
    public ResultVo addType(@RequestBody TypeModel typeModel) {
        return ResultVo.success(typeService.addType(typeModel));
    }

    /**
     * 更新分类接口
     * 功能：
     * 1. 验证分类信息
     * 2. 更新分类数据
     * 3. 返回更新结果
     * 
     * @param typeModel 分类信息
     * @return 更新结果
     */
    @PutMapping("update")
    public ResultVo updateType(@RequestBody TypeModel typeModel) {
        return ResultVo.success(typeService.updateType(typeModel));
    }

    /**
     * 删除分类接口
     * 功能：
     * 1. 验证分类ID
     * 2. 删除指定分类
     * 3. 返回删除结果
     * 
     * 注意事项：
     * - 检查分类是否被使用
     * - 处理关联数据
     * 
     * @param id 分类ID
     * @return 删除结果
     */
    @DeleteMapping("delete")
    public ResultVo deleteType(Long id) {
        return ResultVo.success(typeService.deleteById(id));
    }
}

package com.rabbiter.fm.dao;

import com.rabbiter.fm.model.IdleItemModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface IdleItemDao {
        int deleteByPrimaryKey(Long id);

        int insert(IdleItemModel record);

        int insertSelective(IdleItemModel record);

        IdleItemModel selectByPrimaryKey(Long id);

        List<IdleItemModel> getAllIdleItem(Long userId, Byte idleStatus);

        int countIdleItem(String findValue);

        int countIdleItemByLable(int idleLabel);

        int countIdleItemByStatus(int status);

        List<IdleItemModel> findIdleItem(String findValue, int begin, int nums);

        List<IdleItemModel> findIdleItemByLable(int idleLabel, int begin, int nums);

        List<IdleItemModel> getIdleItemByStatus(int status, int begin, int nums);

        int updateByPrimaryKeySelective(IdleItemModel record);

        int updateByPrimaryKey(IdleItemModel record);

        List<IdleItemModel> findIdleByList(List<Long> idList);

        @Select("SELECT * FROM sh_idle_item WHERE id = #{id}")
        @ResultMap("BaseResultMap")
        IdleItemModel selectById(Integer id);

        /**
         * 获取热门商品
         * 通过浏览次数进行排序，获取最受欢迎的商品
         * 只选择状态为上架(1)的商品
         */
        @Select("SELECT i.* FROM sh_idle_item i " +
                        "LEFT JOIN sh_user_behavior b ON i.id = b.idle_id " +
                        "WHERE i.idle_status = 1 " +
                        "GROUP BY i.id " +
                        "ORDER BY COUNT(b.id) DESC, i.release_time DESC " +
                        "LIMIT #{limit}")
        @ResultMap("BaseResultMap")
        List<IdleItemModel> selectPopularItems(int limit);

        /**
         * 获取最新上架的商品
         * 如果没有足够的热门商品，可以使用此方法补充
         */
        @Select("SELECT * FROM sh_idle_item " +
                        "WHERE idle_status = 1 " +
                        "ORDER BY release_time DESC " +
                        "LIMIT #{limit}")
        @ResultMap("BaseResultMap")
        List<IdleItemModel> selectLatestItems(int limit);

        /**
         * 获取随机商品
         * 返回指定数量的随机商品（状态为上架的）
         */
        @Select("SELECT * FROM sh_idle_item " +
                        "WHERE idle_status = 1 " +
                        "ORDER BY RAND() " +
                        "LIMIT #{limit}")
        @ResultMap("BaseResultMap")
        List<IdleItemModel> selectRandomItems(int limit);
}
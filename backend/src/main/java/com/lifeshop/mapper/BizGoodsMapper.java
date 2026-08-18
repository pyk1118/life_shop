package com.lifeshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifeshop.entity.BizGoods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BizGoodsMapper extends BaseMapper<BizGoods> {

    @Update("UPDATE biz_goods SET stock = stock - #{num} WHERE goods_id = #{goodsId} AND stock >= #{num} AND is_deleted = 0")
    int deductStock(@Param("goodsId") Long goodsId, @Param("num") Integer num);

    @Update("UPDATE biz_goods SET sales_count = sales_count + #{num} WHERE goods_id = #{goodsId} AND is_deleted = 0")
    int incrementSalesCount(@Param("goodsId") Long goodsId, @Param("num") Integer num);
}

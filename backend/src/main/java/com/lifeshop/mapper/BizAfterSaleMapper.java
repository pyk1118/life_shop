package com.lifeshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifeshop.entity.BizAfterSale;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BizAfterSaleMapper extends BaseMapper<BizAfterSale> {

    @Update("UPDATE biz_after_sale SET is_deleted = 0 WHERE is_deleted IS NULL")
    int fixNullIsDeleted();

    @Update("UPDATE biz_after_sale SET escalated = 0 WHERE escalated IS NULL")
    int fixNullEscalated();
}

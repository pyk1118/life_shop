package com.lifeshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeshop.entity.BizShop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BizShopMapper extends BaseMapper<BizShop> {

    @Select("<script>" +
            "SELECT s.* FROM biz_shop s " +
            "INNER JOIN biz_merchant m ON s.merchant_id = m.merchant_id " +
            "WHERE m.audit_status = 1 AND s.is_deleted = 0 " +
            "<if test='shopName != null and shopName != \"\"'> AND s.shop_name LIKE CONCAT('%', #{shopName}, '%')</if>" +
            "<if test='shopStatus != null'> AND s.shop_status = #{shopStatus}</if>" +
            " ORDER BY s.shop_id ASC" +
            "</script>")
    List<BizShop> selectApprovedShopPage(@Param("shopName") String shopName,
                                          @Param("shopStatus") Integer shopStatus);

    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_shop s " +
            "INNER JOIN biz_merchant m ON s.merchant_id = m.merchant_id " +
            "WHERE m.audit_status = 1 AND s.is_deleted = 0 " +
            "<if test='shopName != null and shopName != \"\"'> AND s.shop_name LIKE CONCAT('%', #{shopName}, '%')</if>" +
            "<if test='shopStatus != null'> AND s.shop_status = #{shopStatus}</if>" +
            "</script>")
    Long countApprovedShopPage(@Param("shopName") String shopName,
                                @Param("shopStatus") Integer shopStatus);
}

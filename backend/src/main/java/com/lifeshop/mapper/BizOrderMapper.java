package com.lifeshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifeshop.entity.BizOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BizOrderMapper extends BaseMapper<BizOrder> {

    @Select("SELECT * FROM biz_order WHERE is_deleted = 0 AND merchant_id = #{merchantId} ORDER BY order_id DESC")
    List<BizOrder> selectAllByMerchantId(@Param("merchantId") Long merchantId);

    @Select("SELECT COUNT(*) FROM biz_order WHERE is_deleted = 0 AND merchant_id = #{merchantId} AND order_status = 3")
    Long countAllByMerchantId(@Param("merchantId") Long merchantId);

    @Select("SELECT COUNT(*) FROM biz_order WHERE is_deleted = 0 AND merchant_id = #{merchantId} AND order_status = #{status}")
    Long countByMerchantIdAndStatus(@Param("merchantId") Long merchantId, @Param("status") Integer status);

    @Select("SELECT * FROM biz_order WHERE is_deleted = 0 AND merchant_id = #{merchantId} AND order_status IN (3, 6)")
    List<BizOrder> selectFinishedByMerchantId(@Param("merchantId") Long merchantId);

    @Select("SELECT * FROM biz_order WHERE is_deleted = 0 AND merchant_id = #{merchantId} AND order_status = 3 ORDER BY create_time DESC")
    List<BizOrder> selectPaidOrdersByMerchantId(@Param("merchantId") Long merchantId);

    @Select("SELECT * FROM biz_order WHERE is_deleted = 0 ORDER BY order_id ASC LIMIT #{offset}, #{limit}")
    List<BizOrder> selectAllForAdmin(@Param("offset") long offset, @Param("limit") long limit);

    @Select("SELECT COUNT(*) FROM biz_order WHERE is_deleted = 0 AND order_status = 3")
    Long countAllForAdmin();

    @Select("<script>" +
            "SELECT * FROM biz_order WHERE is_deleted = 0 " +
            "<if test='orderNo != null and orderNo != \"\"'> AND order_no LIKE CONCAT('%', #{orderNo}, '%')</if>" +
            "<if test='userId != null'> AND user_id = #{userId}</if>" +
            "<if test='shopId != null'> AND shop_id = #{shopId}</if>" +
            "<if test='status != null'> AND order_status = #{status}</if>" +
            " ORDER BY order_id ASC LIMIT #{offset}, #{limit}" +
            "</script>")
    List<BizOrder> selectAllForAdminWithFilter(@Param("offset") long offset, @Param("limit") long limit,
            @Param("orderNo") String orderNo, @Param("userId") Long userId,
            @Param("shopId") Long shopId, @Param("status") Integer status);

    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_order WHERE is_deleted = 0 " +
            "<if test='orderNo != null and orderNo != \"\"'> AND order_no LIKE CONCAT('%', #{orderNo}, '%')</if>" +
            "<if test='userId != null'> AND user_id = #{userId}</if>" +
            "<if test='shopId != null'> AND shop_id = #{shopId}</if>" +
            "<if test='status != null'> AND order_status = #{status}</if>" +
            "</script>")
    Long countAllForAdminWithFilter(@Param("orderNo") String orderNo, @Param("userId") Long userId,
            @Param("shopId") Long shopId, @Param("status") Integer status);

    @Select("SELECT COUNT(*) FROM biz_order WHERE is_deleted = 0 AND order_status = #{status}")
    Long countByStatus(@Param("status") Integer status);

    @Select("SELECT * FROM biz_order WHERE is_deleted = 0 AND order_status = 3")
    List<BizOrder> selectPaidOrdersForAdmin();

    @Select("SELECT * FROM biz_order WHERE is_deleted = 0 AND order_status = 3 AND pay_time >= #{startTime} AND pay_time <= #{endTime}")
    List<BizOrder> selectPaidOrdersByPayTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("SELECT * FROM biz_order WHERE order_id = #{orderId}")
    BizOrder selectByIdIgnoreDelete(@Param("orderId") Long orderId);
}

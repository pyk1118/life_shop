package com.lifeshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_after_sale")
public class BizAfterSale {
    @TableId(value = "after_sale_id", type = IdType.AUTO)
    private Long afterSaleId;
    private Long orderId;
    private Long itemId;
    private Long userId;
    private Long merchantId;
    private BigDecimal refundAmount;
    private String refundReason;
    private String refundVoucher;
    private Integer auditStatus;
    private String auditRemark;
    private String adminRemark;
    private Integer escalated;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
    @TableField(exist = false)
    private String orderNo;
}

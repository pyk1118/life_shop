package com.lifeshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_order")
public class BizOrder {
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long merchantId;
    private Long shopId;
    private BigDecimal totalAmount;
    private String payType;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private Integer orderStatus;
    private String logisticsNo;
    private LocalDateTime payTime;
    private LocalDateTime deliverTime;
    private LocalDateTime receiveTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}

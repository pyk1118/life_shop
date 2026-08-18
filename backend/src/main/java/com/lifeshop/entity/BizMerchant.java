package com.lifeshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_merchant")
public class BizMerchant {
    @TableId(value = "merchant_id", type = IdType.AUTO)
    private Long merchantId;
    private Long userId;
    private String merchantAccount;
    private String merchantPassword;
    private Long shopId;
    private Integer auditStatus;
    private String auditRemark;
    private Integer merchantStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}

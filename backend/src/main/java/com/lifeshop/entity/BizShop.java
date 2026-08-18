package com.lifeshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_shop")
public class BizShop {
    @TableId(value = "shop_id", type = IdType.AUTO)
    private Long shopId;
    private Long merchantId;
    private String shopName;
    private String shopLogo;
    private String shopDesc;
    private String businessHours;
    private Integer shopStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}

package com.lifeshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_user_address")
public class BizUserAddress {
    @TableId(value = "address_id", type = IdType.AUTO)
    private Long addressId;
    private Long userId;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private Integer isDefault;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}

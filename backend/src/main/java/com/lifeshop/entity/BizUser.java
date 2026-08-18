package com.lifeshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_user")
public class BizUser {
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;
    private String userAccount;
    private String userPassword;
    private String userAvatar;
    private String realName;
    private Integer gender;
    private String phone;
    private String email;
    private String address;
    private Integer isMerchant;
    private Long merchantId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}

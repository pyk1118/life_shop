package com.lifeshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("biz_cart")
public class BizCart {
    @TableId(value = "cart_id", type = IdType.AUTO)
    private Long cartId;
    private Long userId;
    private Long goodsId;
    private Integer goodsNum;
}

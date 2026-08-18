package com.lifeshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("biz_order_item")
public class BizOrderItem {
    @TableId(value = "item_id", type = IdType.AUTO)
    private Long itemId;
    private Long orderId;
    private Long goodsId;
    private String goodsName;
    private String goodsImage;
    private BigDecimal goodsPrice;
    private Integer goodsNum;
    private BigDecimal totalPrice;
}

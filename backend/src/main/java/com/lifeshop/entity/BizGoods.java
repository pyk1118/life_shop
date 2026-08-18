package com.lifeshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_goods")
public class BizGoods {
    @TableId(value = "goods_id", type = IdType.AUTO)
    private Long goodsId;
    private String goodsName;
    private Long categoryId;
    @TableField(exist = false)
    private String categoryName;
    private Long merchantId;
    private Long shopId;
    private String goodsImage;
    private String goodsDesc;
    private BigDecimal marketPrice;
    private BigDecimal salePrice;
    private Integer stock;
    private Integer salesCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}

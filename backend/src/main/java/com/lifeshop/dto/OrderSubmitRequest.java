package com.lifeshop.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderSubmitRequest {
    @NotEmpty(message = "请选择要结算的购物车商品")
    private List<Long> cartIds;
    @NotNull(message = "收货地址不能为空")
    private Long addressId;
    private String payType;
}

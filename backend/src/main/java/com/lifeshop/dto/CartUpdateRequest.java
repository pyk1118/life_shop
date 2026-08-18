package com.lifeshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartUpdateRequest {
    @NotNull
    private Long cartId;
    @NotNull
    @Min(value = 1, message = "商品数量最小为1")
    private Integer goodsNum;
}

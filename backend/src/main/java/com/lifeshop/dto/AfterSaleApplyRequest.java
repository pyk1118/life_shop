package com.lifeshop.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AfterSaleApplyRequest {
    @NotNull
    private Long orderId;
    @NotNull
    private Long itemId;
    @NotNull
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    private BigDecimal refundAmount;
    @NotBlank
    private String refundReason;
    private String refundVoucher;
}

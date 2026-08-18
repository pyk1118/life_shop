package com.lifeshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MerchantApplyRequest {
    @NotBlank(message = "店铺名称不能为空")
    @Size(max = 100, message = "店铺名称长度不能超过100")
    private String shopName;
    private String shopLogo;
    private String shopDesc;
    private String businessHours;
    @NotBlank(message = "商家独立密码不能为空")
    @Size(min = 6, max = 20, message = "商家密码长度应为6-20")
    private String merchantPassword;
}

package com.lifeshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddressSaveRequest {
    private Long addressId;
    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;
    @NotBlank(message = "收货人手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式错误")
    private String receiverPhone;
    @NotBlank(message = "收货地址不能为空")
    private String receiverAddress;
    private Integer isDefault;
}

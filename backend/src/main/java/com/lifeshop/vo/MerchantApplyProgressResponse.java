package com.lifeshop.vo;

import lombok.Data;

@Data
public class MerchantApplyProgressResponse {
    private Integer isMerchant;
    private String merchantAccount;
    private Integer auditStatus;
    private String auditRemark;
    private String realName;
    private String phone;
    private String email;
    private String address;
    private String shopName;
    private String shopLogo;
    private String shopDesc;
    private String businessHours;
}

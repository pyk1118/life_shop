package com.lifeshop.service;

import com.lifeshop.dto.MerchantApplyRequest;
import com.lifeshop.vo.MerchantApplyProgressResponse;

public interface MerchantApplyService {
    void apply(Long userId, MerchantApplyRequest request);

    MerchantApplyProgressResponse progress(Long userId);
}

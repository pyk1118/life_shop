package com.lifeshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifeshop.common.exception.BusinessException;
import com.lifeshop.common.result.ResultCode;
import com.lifeshop.dto.MerchantApplyRequest;
import com.lifeshop.entity.BizMerchant;
import com.lifeshop.entity.BizShop;
import com.lifeshop.entity.BizUser;
import com.lifeshop.mapper.BizMerchantMapper;
import com.lifeshop.mapper.BizShopMapper;
import com.lifeshop.mapper.BizUserMapper;
import com.lifeshop.service.MerchantApplyService;
import com.lifeshop.vo.MerchantApplyProgressResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MerchantApplyServiceImpl implements MerchantApplyService {

    private final BizUserMapper bizUserMapper;
    private final BizMerchantMapper bizMerchantMapper;
    private final BizShopMapper bizShopMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apply(Long userId, MerchantApplyRequest request) {
        BizUser user = bizUserMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户不存在或已禁用");
        }
        if (user.getIsMerchant() != null && user.getIsMerchant() == 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前用户已是商家，禁止重复申请");
        }

        BizMerchant exist = bizMerchantMapper.selectOne(new LambdaQueryWrapper<BizMerchant>()
                .eq(BizMerchant::getUserId, userId));
        if (exist != null) {
            if (exist.getAuditStatus() != null && exist.getAuditStatus() == 2) {
                exist.setMerchantPassword(request.getMerchantPassword());
                exist.setAuditStatus(0);
                exist.setAuditRemark("");
                exist.setMerchantStatus(1);
                bizMerchantMapper.updateById(exist);
                if (exist.getShopId() != null) {
                    BizShop shop = bizShopMapper.selectById(exist.getShopId());
                    if (shop != null) {
                        shop.setShopName(request.getShopName());
                        shop.setShopLogo(request.getShopLogo() == null ? "" : request.getShopLogo());
                        shop.setShopDesc(request.getShopDesc() == null ? "" : request.getShopDesc());
                        shop.setBusinessHours(request.getBusinessHours() == null ? "" : request.getBusinessHours());
                        shop.setShopStatus(0);
                        bizShopMapper.updateById(shop);
                    }
                }
                return;
            }
            throw new BusinessException(ResultCode.BAD_REQUEST, "已提交入驻申请，请勿重复提交");
        }

        BizMerchant merchant = new BizMerchant();
        merchant.setUserId(userId);
        merchant.setMerchantAccount("M" + userId + System.currentTimeMillis() % 10000);
        merchant.setMerchantPassword(request.getMerchantPassword());
        merchant.setAuditStatus(0);
        merchant.setAuditRemark("");
        merchant.setMerchantStatus(1);
        bizMerchantMapper.insert(merchant);

        BizShop shop = new BizShop();
        shop.setMerchantId(merchant.getMerchantId());
        shop.setShopName(request.getShopName());
        shop.setShopLogo(request.getShopLogo() == null ? "" : request.getShopLogo());
        shop.setShopDesc(request.getShopDesc() == null ? "" : request.getShopDesc());
        shop.setBusinessHours(request.getBusinessHours() == null ? "" : request.getBusinessHours());
        shop.setShopStatus(0);
        bizShopMapper.insert(shop);

        merchant.setShopId(shop.getShopId());
        bizMerchantMapper.updateById(merchant);

        user.setMerchantId(merchant.getMerchantId());
        bizUserMapper.updateById(user);
    }

    @Override
    public MerchantApplyProgressResponse progress(Long userId) {
        BizUser user = bizUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户不存在");
        }
        MerchantApplyProgressResponse response = new MerchantApplyProgressResponse();
        response.setIsMerchant(user.getIsMerchant());
        response.setRealName(user.getRealName());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setAddress(user.getAddress());
        if (user.getMerchantId() == null) {
            response.setAuditStatus(-1);
            response.setAuditRemark("未提交入驻申请");
            return response;
        }
        BizMerchant merchant = bizMerchantMapper.selectById(user.getMerchantId());
        if (merchant == null) {
            response.setAuditStatus(-1);
            response.setAuditRemark("未找到入驻记录");
            return response;
        }
        response.setMerchantAccount(merchant.getMerchantAccount());
        response.setAuditStatus(merchant.getAuditStatus());
        response.setAuditRemark(merchant.getAuditRemark());
        if (merchant.getShopId() != null) {
            BizShop shop = bizShopMapper.selectById(merchant.getShopId());
            if (shop != null) {
                response.setShopName(shop.getShopName());
                response.setShopLogo(shop.getShopLogo());
                response.setShopDesc(shop.getShopDesc());
                response.setBusinessHours(shop.getBusinessHours());
            }
        }
        return response;
    }
}

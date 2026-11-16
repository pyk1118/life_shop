package com.lifeshop.config;

import com.lifeshop.mapper.BizAfterSaleMapper;
import com.lifeshop.mapper.BizShopMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataMigrationConfig {

    private final BizAfterSaleMapper bizAfterSaleMapper;
    private final JdbcTemplate jdbcTemplate;

    //test01
    @Bean
    public CommandLineRunner fixAfterSaleData() {
        return args -> {
            try {
                int fixed1 = bizAfterSaleMapper.fixNullIsDeleted();
                int fixed2 = bizAfterSaleMapper.fixNullEscalated();
                if (fixed1 > 0 || fixed2 > 0) {
                    log.info("修复售后表数据: is_deleted修复{}条, escalated修复{}条", fixed1, fixed2);
                }
            } catch (Exception e) {
                log.warn("修复售后表数据失败: {}", e.getMessage());
            }

            try {
                int fixed = jdbcTemplate.update(
                        "UPDATE biz_shop s JOIN biz_merchant m ON s.merchant_id = m.merchant_id " +
                        "SET s.shop_status = 0 WHERE m.audit_status != 1 AND s.shop_status = 1");
                if (fixed > 0) {
                    log.info("修复店铺状态数据: {}条", fixed);
                }
            } catch (Exception e) {
                log.warn("修复店铺状态数据失败: {}", e.getMessage());
            }
        };
    }
}

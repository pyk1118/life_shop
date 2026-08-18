package com.lifeshop.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class DashboardVO {
  private Long userCount;
  private Long merchantCount;
  private Long shopCount;
  private Long goodsCount;
  private Long orderCount;
  private BigDecimal totalSales;
  private Long messageCount;
  private Long pendingAuditCount;
  private Long abnormalOrderCount;
  private Long unrepliedMessageCount;
  private List<Map<String, Object>> monthlySales;
  private List<Map<String, Object>> dailySales;
}

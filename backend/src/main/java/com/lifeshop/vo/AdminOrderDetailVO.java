package com.lifeshop.vo;

import lombok.Data;

import java.util.List;

@Data
public class AdminOrderDetailVO {
  private Object orderInfo;
  private List<Object> orderItems;
  private Object userInfo;
  private Object shopInfo;
}

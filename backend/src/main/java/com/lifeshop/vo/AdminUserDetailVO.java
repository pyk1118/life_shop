package com.lifeshop.vo;

import lombok.Data;

import java.util.List;

@Data
public class AdminUserDetailVO {
  private Object userInfo;
  private List<Object> orderList;
  private List<Object> addressList;
}

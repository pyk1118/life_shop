package com.lifeshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategorySaveRequest {
  private Long categoryId;

  @NotBlank(message = "分类名称不能为空")
  @Size(max = 50, message = "分类名称最多50字符")
  private String categoryName;

  @Size(max = 255, message = "分类描述最多255字符")
  private String categoryDesc;

  private String categoryIcon;

  private Integer sort;

  private Integer status;
}

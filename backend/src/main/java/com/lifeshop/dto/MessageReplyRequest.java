package com.lifeshop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageReplyRequest {
  private Long messageId;

  @NotBlank(message = "回复内容不能为空")
  private String replyContent;
}

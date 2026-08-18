package com.lifeshop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LeaveMessageRequest {
    @NotBlank
    private String messageTitle;
    @NotBlank
    private String messageContent;
}

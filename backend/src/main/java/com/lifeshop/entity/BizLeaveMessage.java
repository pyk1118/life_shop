package com.lifeshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_leave_message")
public class BizLeaveMessage {
    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;
    private Long userId;
    private String userAccount;
    private String userAvatar;
    private String messageTitle;
    private String messageContent;
    private String replyContent;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;

    @TableField(exist = false)
    private Integer commentCount;
}

package com.lifeshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_message_comment")
public class BizMessageComment {
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;
    private Long messageId;
    private Long userId;
    private String userAccount;
    private String userAvatar;
    private String commentContent;
    private Integer isTop;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
    @TableField(exist = false)
    private String messageTitle;
    @TableField(exist = false)
    private String messageContent;
}

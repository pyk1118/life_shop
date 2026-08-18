package com.lifeshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifeshop.entity.BizMessageComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BizMessageCommentMapper extends BaseMapper<BizMessageComment> {

    @Update("UPDATE biz_message_comment SET user_avatar = #{avatar} WHERE user_id = #{userId} AND is_deleted = 0")
    int updateAvatarByUserId(@Param("userId") Long userId, @Param("avatar") String avatar);

    @Select("SELECT * FROM biz_message_comment WHERE message_id = #{messageId} AND is_deleted = 0 ORDER BY CASE is_top WHEN 1 THEN 0 WHEN 2 THEN 1 ELSE 2 END, comment_id DESC")
    List<BizMessageComment> selectCommentsSorted(@Param("messageId") Long messageId);
}

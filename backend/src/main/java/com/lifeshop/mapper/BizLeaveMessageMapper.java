package com.lifeshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifeshop.entity.BizLeaveMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BizLeaveMessageMapper extends BaseMapper<BizLeaveMessage> {

    @Update("UPDATE biz_leave_message SET user_avatar = #{avatar} WHERE user_id = #{userId} AND is_deleted = 0")
    int updateAvatarByUserId(@Param("userId") Long userId, @Param("avatar") String avatar);

    @Select("SELECT COUNT(*) FROM biz_leave_message WHERE is_deleted = 0 AND (IFNULL(reply_content, '') = '') AND (message_title LIKE '%@开发者%' OR message_title NOT LIKE '%@%')")
    Long countUnrepliedForAdmin();

    @Select("SELECT COUNT(*) FROM biz_leave_message WHERE is_deleted = 0 AND (IFNULL(reply_content, '') = '') AND message_title LIKE CONCAT('%@', #{shopName}, '%')")
    Long countUnrepliedByShopName(@Param("shopName") String shopName);
}

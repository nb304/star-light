package org.king2.sl.sso.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.king2.sl.common.pojo.SlMessage;

import java.util.List;

/**
 * 本地的用户信息Mapper
 */
public interface LocalSlUserMessageMapper {

    /**
     * 发送消息
     *
     * @param slMessage
     * @param userIds
     */
    @Insert("<script>" +
            "INSERT INTO sl_message (`sl_message_content` , `sl_user_id` , `sl_read_state` , " +
            "`create_time` , `sl_send_user_id` ) VALUES " +
            "<foreach collection='list' item='item'  separator=','>" +
            "(#{msg.slMessageContent},#{item},#{msg.slReadState},#{msg.createTime},#{msg.slSendUserId})" +
            "</foreach>" +
            "</script>")
    void sendMsg(@Param("msg") SlMessage slMessage, @Param("list") List<Integer> userIds);
}

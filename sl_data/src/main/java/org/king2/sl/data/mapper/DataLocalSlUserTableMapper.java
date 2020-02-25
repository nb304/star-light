package org.king2.sl.data.mapper;

import org.apache.ibatis.annotations.Update;
import org.king2.sl.common.pojo.SlUserTableMapper;

/**
 * 本地的用户表
 */
public interface DataLocalSlUserTableMapper extends SlUserTableMapper {

    /**
     * 更新用户的同意规则
     *
     * @param userName
     */
    @Update("UPDATE sl_user_table SET sl_consent = 2 WHERE sl_user_name = #{userName} ")
    void updateUserConsent(String userName);
}

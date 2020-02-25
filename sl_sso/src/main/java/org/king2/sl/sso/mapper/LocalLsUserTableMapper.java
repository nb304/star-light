package org.king2.sl.sso.mapper;

import org.apache.ibatis.annotations.*;
import org.king2.sl.common.pojo.SlUserTable;
import org.king2.sl.common.pojo.SlUserTableMapper;

import java.util.Date;
import java.util.List;

/**
 * 本地的用户表Mapper
 */
public interface LocalLsUserTableMapper extends SlUserTableMapper {

    /**
     * 通过用户名和密码查询用户是否存在
     *
     * @param userName 用户名
     * @param passWord 密码
     * @return 返回用户信息
     */
    @Select("SELECT * FROM sl_user_table WHERE sl_user_name = #{userName} AND sl_user_pass = #{passWord}")
    SlUserTable get(String userName, String passWord);

    /**
     * 查询用户名或者邮箱是否存在
     *
     * @param userName 用户名
     * @return 返回是否存在
     */
    @Select("SELECT * FROM sl_user_table WHERE sl_user_name = #{userName}")
    SlUserTable getUserName(String userName);

    @Select("SELECT * FROM sl_user_table WHERE sl_user_email = #{email} AND sl_user_state = 1")
    SlUserTable getEmail(String email);

    /**
     * 通过用户Id查询用户信息
     *
     * @param userId 用户Id
     * @return 返回是否存在
     */
    @Select("SELECT * FROM sl_user_table WHERE sl_user_id = #{userId}")
    SlUserTable getUserById(Integer userId);

    /**
     * 添加用户信息
     *
     * @param table Table
     */
    @Insert("INSERT INTO sl_user_table(`sl_user_name` , `sl_user_pass` , `sl_user_image` " +
            ", `sl_user_email` , `sl_create_time`) VALUES (#{table.slUserName} , #{table.slUserPass}" +
            ", #{table.slUserImage}, #{table.slUserEmail}, #{table.slCreateTime})")
    void insertValue(@Param("table") SlUserTable table);

    /**
     * 激活用户账号
     *
     * @param uName
     */
    @Update("UPDATE sl_user_table SET sl_user_state = 1 WHERE sl_user_name = #{uName}")
    void active(String uName);

    /**
     * 删除没有激活的账号
     *
     * @param email
     */
    @Delete("DELETE FROM sl_user_table WHERE sl_user_email = #{email} AND sl_user_state = 2")
    void deleteNoActive(String email);

    /**
     * 查询出所有没有激活的账户
     *
     * @return
     */
    @Select("SELECT * FROM sl_user_table WHERE sl_user_state = 2")
    List<SlUserTable> getAllNotActive();

    /**
     * 删除所有没有激活的账户
     */
    @Delete("DELETE FROM sl_user_table WHERE  sl_user_state = 2")
    void deleteNotActive();

    /**
     * 修改用户最后登录的时间
     *
     * @param time
     * @param id
     */
    @Update("UPDATE sl_user_table SET sl_update_time = #{time} WHERE sl_user_id=#{id}")
    void updateUserLastLoginTime(@Param("time") Date time, @Param("id") Integer id);


    /**
     * 更新用户的同意规则
     *
     * @param userName
     */
    @Update("UPDATE sl_user_table SET sl_consent = 2 WHERE sl_user_name = #{userName} ")
    void updateUserConsent(String userName);

}

package org.king2.sl.notify.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.king2.sl.common.pojo.SlBalance;

/**
 * 本地余额Mapper
 */
public interface LocalBalanceMapper {

    /**
     * 根据用户id查询余额信息
     *
     * @param userId
     * @return
     */
    @Select("SELECT * FROM sl_balance WHERE sl_user_id = #{userId}")
    SlBalance getBalanceByUserId(@Param("userId") Integer userId);

    /**
     * 新增余额
     *
     * @param balanceByUserId
     */
    @Insert("INSERT INTO sl_balance (`sl_balance` , `sl_user_id` , `sl_update_time`) " +
            " VALUES " +
            " (#{sb.slBalance}, #{sb.slUserId} , #{sb.slUpdateTime}) ")
    void insertBalance(@Param("sb") SlBalance balanceByUserId);

    /**
     * 更新余额信息
     *
     * @param balanceByUserId
     */
    @Update("UPDATE sl_balance SET sl_balance = #{sb.slBalance} AND sl_update_time = #{sb.slUpdateTime} WHERE " +
            "sl_balance_id = #{sb.slBalanceId}")
    void updateBalance(@Param("sb") SlBalance balanceByUserId);
}

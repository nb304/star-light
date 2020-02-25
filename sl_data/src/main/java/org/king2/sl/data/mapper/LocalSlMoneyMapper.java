package org.king2.sl.data.mapper;

import org.apache.ibatis.annotations.Select;
import org.king2.sl.common.pojo.SlMoney;
import org.king2.sl.common.pojo.SlRecharge;

import java.util.List;

/**
 * 本地的金钱Mapper
 */
public interface LocalSlMoneyMapper {

    /**
     * 查询金额的所有类型
     *
     * @return
     */
    @Select("SELECT * FROM sl_money")
    List<SlMoney> getAllMoney();

    /**
     * 根据用户id查询用户的余额信息
     *
     * @param userId
     * @return
     */
    @Select("SELECT sl_balance FROM sl_balance WHERE sl_user_id = #{userId}")
    Long getUserBalanceByUserId(Integer userId);

    /**
     * 查询用户最近九次的充值记录
     *
     * @param userId
     * @return
     */
    @Select("SELECT * FROM sl_recharge WHERE sl_recharge_user = #{userId} AND sl_recharge_state = 1 ORDER BY sl_recharge_create_time DESC LIMIT 0 , 9")
    List<SlRecharge> getUserLastNineDataByUserId(Integer userId);
}

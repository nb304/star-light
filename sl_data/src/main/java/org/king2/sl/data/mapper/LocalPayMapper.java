package org.king2.sl.data.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.king2.sl.common.pojo.SlRecharge;

/**
 * 本地的支付Mapper
 */
public interface LocalPayMapper {


    /**
     * 将订单信息插入SQL
     *
     * @param recharge
     */
    @Insert("INSERT INTO sl_recharge (`sl_money_id`,`sl_recharge_user` , `sl_recharge_state` , " +
            "`sl_recharge_create_time` , `sl_recharge_order` , `sl_money_name`) VALUES (" +
            "#{r.slMoneyId} , #{r.slRechargeUser}, #{r.slRechargeState}, #{r.slRechargeCreateTime}" +
            ", #{r.slRechargeOrder}, #{r.slMoneyName})")
    void inset(@Param("r") SlRecharge recharge);
}

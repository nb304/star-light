package org.king2.sl.notify.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.king2.sl.common.pojo.SlRecharge;

/**
 * 本地的订单Mapper
 */
public interface LocalOrderMapper {

    /**
     * 根据订单号查询订单信息
     *
     * @param order
     * @return
     */
    @Select("SELECT * FROM sl_recharge WHERE sl_recharge_order = #{order}")
    SlRecharge getRechargeByOrder(@Param("order") String order);

    /**
     * 修改订单的状态
     *
     * @param slRechargeId
     * @param state
     */
    @Update("UPDATE sl_recharge SET sl_recharge_state = #{state} WHERE sl_recharge_id = #{id}")
    void updateRechargeStateByOrderId(@Param("id") Integer slRechargeId, @Param("state") Integer state);
}

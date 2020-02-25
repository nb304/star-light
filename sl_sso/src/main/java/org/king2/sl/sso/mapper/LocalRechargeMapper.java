package org.king2.sl.sso.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.king2.sl.common.pojo.SlRecharge;

import java.util.List;

/**
 * 本地的订单Mapper
 */
public interface LocalRechargeMapper {

    /**
     * 查询出所有没有支付成功的订单
     *
     * @return
     */
    @Select("SELECT * FROM sl_recharge WHERE sl_recharge_state != 1")
    List<SlRecharge> getAllNotPaySuccess();

    /**
     * 修改订单的状态
     *
     * @param state
     * @param ids
     */
    @Update("<script>" +
            "UPDATE sl_recharge SET sl_tell = #{state} WHERE sl_recharge_id IN (" +
            "<foreach collection='list' item='id'  separator=','>" +
            " #{id} " +
            "</foreach>" +
            ")" +
            "</script>")
    void updateRechargeState(@Param("state") String state, @Param("list") List<Integer> ids);

    /**
     * 将第一次通知的订单进行修改状态
     *
     * @param yesDelOrderIds
     */
    @Delete("<script>" +
            "DELETE FROM sl_recharge WHERE sl_recharge_id IN ( " +
            "<foreach collection='list' item='id'  separator=','>" +
            "#{id}" +
            "</foreach> " +
            " )" +
            "</script>")
    void deleteSuccessTellOrder(@Param("list") List<Integer> yesDelOrderIds);
}

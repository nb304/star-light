package org.king2.sl.common.pojo;

import lombok.Data;

import java.util.Date;


@Data
public class SlRecharge {
    private Integer slRechargeId;

    private Integer slMoneyId;

    private Integer slRechargeUser;

    private Integer slRechargeState;

    private Date slRechargeCreateTime;

    private String slRechargeOrder;

    private Integer slMoneyName;

    private String slTell;

}
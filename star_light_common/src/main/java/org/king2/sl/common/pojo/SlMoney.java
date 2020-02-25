package org.king2.sl.common.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class SlMoney {
    private Integer slMoneyId;

    private Integer slMoneyName;

    private Integer slGiveMoney;

    private Date slCreateTime;

    private Integer slRmb;

}
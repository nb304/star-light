package org.king2.sl.common.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class SlBalance {
    private Integer slBalanceId;
    private Integer slBalance;
    private Integer slUserId;
    private Date slUpdateTime;
}

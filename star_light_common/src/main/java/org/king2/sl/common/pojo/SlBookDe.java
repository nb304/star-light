package org.king2.sl.common.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class SlBookDe {
    private Integer slBookDeId;

    private String slBookDeName;

    private String slBootDeBody;

    private Integer slParentId;

    private Integer slCost;

    private Integer slOrder;

    private Date slCreateTime;

}
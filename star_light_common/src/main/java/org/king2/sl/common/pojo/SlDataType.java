package org.king2.sl.common.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class SlDataType {
    private Integer slDataId;

    private String slDataName;

    private Integer slDataState;

    private Date slDataCreateTime;

}
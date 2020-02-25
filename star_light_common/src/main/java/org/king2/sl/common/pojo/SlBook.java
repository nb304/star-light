package org.king2.sl.common.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class SlBook {
    private Integer slBookId;

    private String slBookName;

    private Integer userId;

    private Integer slDataTypeId;

    private Integer slIsFree;

    private String slContent;

    private String slCoverImage;

    private Integer slMaxOrder;

    private Date slUpdateTime;

    private int slLikeSize;
}
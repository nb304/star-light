package org.king2.sl.common.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class SlLike {

    private int slLikeId;
    private int slBookId;
    private int slUserId;
    private Date createTime;
}

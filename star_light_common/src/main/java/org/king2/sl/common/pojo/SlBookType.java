package org.king2.sl.common.pojo;

import java.util.Date;

public class SlBookType {
    private Integer slTypeId;

    private String slTypeName;

    private Integer slTypeState;

    private Date slTypeCreateTime;

    public Integer getSlTypeId() {
        return slTypeId;
    }

    public void setSlTypeId(Integer slTypeId) {
        this.slTypeId = slTypeId;
    }

    public String getSlTypeName() {
        return slTypeName;
    }

    public void setSlTypeName(String slTypeName) {
        this.slTypeName = slTypeName;
    }

    public Integer getSlTypeState() {
        return slTypeState;
    }

    public void setSlTypeState(Integer slTypeState) {
        this.slTypeState = slTypeState;
    }

    public Date getSlTypeCreateTime() {
        return slTypeCreateTime;
    }

    public void setSlTypeCreateTime(Date slTypeCreateTime) {
        this.slTypeCreateTime = slTypeCreateTime;
    }
}
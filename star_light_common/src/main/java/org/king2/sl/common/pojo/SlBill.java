package org.king2.sl.common.pojo;

import java.util.Date;

public class SlBill {
    private Integer slBillId;

    private Integer slBillUser;

    private Integer slBillState;

    private Integer slBillMoney;

    private Integer slBillRemainMoney;

    private String slBillOrder;

    private Date slBillCreateTime;

    private Date slBillPayTime;

    public Integer getSlBillId() {
        return slBillId;
    }

    public void setSlBillId(Integer slBillId) {
        this.slBillId = slBillId;
    }

    public Integer getSlBillUser() {
        return slBillUser;
    }

    public void setSlBillUser(Integer slBillUser) {
        this.slBillUser = slBillUser;
    }

    public Integer getSlBillState() {
        return slBillState;
    }

    public void setSlBillState(Integer slBillState) {
        this.slBillState = slBillState;
    }

    public Integer getSlBillMoney() {
        return slBillMoney;
    }

    public void setSlBillMoney(Integer slBillMoney) {
        this.slBillMoney = slBillMoney;
    }

    public Integer getSlBillRemainMoney() {
        return slBillRemainMoney;
    }

    public void setSlBillRemainMoney(Integer slBillRemainMoney) {
        this.slBillRemainMoney = slBillRemainMoney;
    }

    public String getSlBillOrder() {
        return slBillOrder;
    }

    public void setSlBillOrder(String slBillOrder) {
        this.slBillOrder = slBillOrder;
    }

    public Date getSlBillCreateTime() {
        return slBillCreateTime;
    }

    public void setSlBillCreateTime(Date slBillCreateTime) {
        this.slBillCreateTime = slBillCreateTime;
    }

    public Date getSlBillPayTime() {
        return slBillPayTime;
    }

    public void setSlBillPayTime(Date slBillPayTime) {
        this.slBillPayTime = slBillPayTime;
    }
}
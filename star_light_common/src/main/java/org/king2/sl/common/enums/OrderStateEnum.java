package org.king2.sl.common.enums;

/**
 * 金额状态的枚举类
 */
public enum OrderStateEnum {
    SUCCESS(1),// 支付成功
    UNPAID(2), // 待支付
    ERROR(3); // 异常
    private Integer id;

    OrderStateEnum(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return this.id + "";
    }

}

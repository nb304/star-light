package org.king2.sl.common.state;

/**
 * 用户的状态信息
 */
public enum UserState {
    NORMAL("正常", 1),
    NO_ACTIVE("没有激活", 2),
    EXCEPTION("异常", 3);

    private String key;
    private Integer value;

    UserState(String key, Integer value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value + "";
    }
}

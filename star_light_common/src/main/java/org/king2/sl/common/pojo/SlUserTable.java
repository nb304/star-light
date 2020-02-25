package org.king2.sl.common.pojo;

import java.util.Date;

public class SlUserTable implements Comparable<SlUserTable> {

    @Override
    public int compareTo(SlUserTable o) {

        return Integer.parseInt(this.getSlCreateTime().getTime() - o.getSlCreateTime().getTime() + "");
    }

    private Integer slUserId;

    private String slUserName;

    private String slUserPass;

    private String slUserImage;

    private String slUserEmail;

    private Integer slUserState;

    private Date slCreateTime;

    private Date slUpdateTime;

    private Integer slConsent;

    public Integer getSlConsent() {
        return slConsent;
    }

    public void setSlConsent(Integer slConsent) {
        this.slConsent = slConsent;
    }

    public Integer getSlUserId() {
        return slUserId;
    }

    public void setSlUserId(Integer slUserId) {
        this.slUserId = slUserId;
    }

    public String getSlUserName() {
        return slUserName;
    }

    public void setSlUserName(String slUserName) {
        this.slUserName = slUserName;
    }

    public String getSlUserPass() {
        return slUserPass;
    }

    public void setSlUserPass(String slUserPass) {
        this.slUserPass = slUserPass;
    }

    public String getSlUserImage() {
        return slUserImage;
    }

    public void setSlUserImage(String slUserImage) {
        this.slUserImage = slUserImage;
    }

    public String getSlUserEmail() {
        return slUserEmail;
    }

    public void setSlUserEmail(String slUserEmail) {
        this.slUserEmail = slUserEmail;
    }

    public Integer getSlUserState() {
        return slUserState;
    }

    public void setSlUserState(Integer slUserState) {
        this.slUserState = slUserState;
    }

    public Date getSlCreateTime() {
        return slCreateTime;
    }

    public void setSlCreateTime(Date slCreateTime) {
        this.slCreateTime = slCreateTime;
    }

    public Date getSlUpdateTime() {
        return slUpdateTime;
    }

    public void setSlUpdateTime(Date slUpdateTime) {
        this.slUpdateTime = slUpdateTime;
    }

    public SlUserTable(String slUserName, String slUserPass, String slUserImage, String slUserEmail, Date slCreateTime) {
        this.slUserName = slUserName;
        this.slUserPass = slUserPass;
        this.slUserImage = slUserImage;
        this.slUserEmail = slUserEmail;
        this.slCreateTime = slCreateTime;
    }

    @Override
    public String toString() {
        return "SlUserTable{" +
                "slUserId=" + slUserId +
                ", slUserName='" + slUserName + '\'' +
                ", slUserPass='" + slUserPass + '\'' +
                ", slUserImage='" + slUserImage + '\'' +
                ", slUserEmail='" + slUserEmail + '\'' +
                ", slUserState=" + slUserState +
                ", slCreateTime=" + slCreateTime +
                ", slUpdateTime=" + slUpdateTime +
                ", slConsent=" + slConsent +
                '}';
    }

    public SlUserTable() {
    }
}
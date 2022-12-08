package cn.penidea.alice.entity;

import cn.hutool.core.date.DateUtil;

/**
 * 功能:返回数据对象
 * 作者:Mr.Fokers
 * 日期：2022年09月15日 19:21
 */

public class RetrunData {
    private int code;
    private String type;
    private String msgFrom;
    private Object data;
    private String receiveTime = DateUtil.now();

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMsgFrom() {
        return msgFrom;
    }

    public void setMsgFrom(String msgFrom) {
        this.msgFrom = msgFrom;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(String receiveTime) {
        this.receiveTime = receiveTime;
    }
}

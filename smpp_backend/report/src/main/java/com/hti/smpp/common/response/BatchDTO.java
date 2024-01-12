package com.hti.smpp.common.response;

public class BatchDTO extends ReportBaseDTO {

    private int msgCount;
    private int numberCount;
    private String reqType;

    public BatchDTO() {
    }

    public BatchDTO(String reqType, int numberCount) {
        this.reqType = reqType;
        this.numberCount = numberCount;
    }

    public BatchDTO(String username, String sender, String date, String time, double cost, String content, int msgCount, int numberCount, String reqType) {
        setUsername(username);
        setSender(sender);
        setDate(date);
        setTime(time);
        setCost(cost);
        setContent(content);
        this.msgCount = msgCount;
        this.numberCount = numberCount;
        this.reqType = reqType;
    }

    public int getMsgCount() {
        return msgCount;
    }

    public void setMsgCount(int msgCount) {
        this.msgCount = msgCount;
    }

    public int getNumberCount() {
        return numberCount;
    }

    public void setNumberCount(int numberCount) {
        this.numberCount = numberCount;
    }

    public String getReqType() {
        return reqType;
    }

    public void setReqType(String reqType) {
        this.reqType = reqType;
    }

}

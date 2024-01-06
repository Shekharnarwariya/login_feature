package com.hti.smpp.common.database;


import java.io.Serializable;

public class DBMessage implements Serializable {

    private String htiMsgId;

    // ------------- For Summary Report -----------------
    private String clientId;
    private String sender;
    private String Content;
    private int totalSmsSend;
    private String cost;
    private String date;
    private String time;
    private int totalNumbers;
    private String usermode;
    private String msgType;
    private String reqType;
    // ------------- For Summary Report -----------------

    private String smscId;
    private String smscMsgId;
    private String destination;
    private String timestamp;
    private String status;
    private int delivered;
    private int nonDelivered;
    private int inProcess;
    private int addToSmsc;
    private String HostMsgId;
    private String Route_to_SMSC;
    private String Dest_No;
    private String Rec_Time;
    private String Sub_Time;

    private String paidSms;
    private String unpaidSms;
    private String amountPaid;

    private String filename;

    private String responseId;
    private String route;
    private String doneTime;
    private String country;
    private String operator;
	//private ArrayList reportList=new ArrayList();

    public DBMessage() {
    }
    
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
    
    

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public int getTotalNumbers() {
        return totalNumbers;
    }

    public void setTotalNumbers(int totalNumbers) {
        this.totalNumbers = totalNumbers;
    }

    public String getUsermode() {
        return usermode;
    }

    public void setUsermode(String usermode) {
        this.usermode = usermode;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getReqType() {
        return reqType;
    }

    public void setReqType(String reqType) {
        this.reqType = reqType;
    }

    public void setHtiMsgId(String htiMsgId) {
        this.htiMsgId = htiMsgId;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public void setSmscId(String smscId) {
        this.smscId = smscId;
    }

    public void setSmscMsgId(String smscMsgId) {
        this.smscMsgId = smscMsgId;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public void setDoneTime(String doneTime) {
        this.doneTime = doneTime;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDelivered(int delivered) {
        this.delivered = delivered;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setNonDelivered(int nonDelivered) {
        this.nonDelivered = nonDelivered;
    }

    public void setInProcess(int inProcess) {
        this.inProcess = inProcess;
    }

    public void setAddToSmsc(int addToSmsc) {
        this.addToSmsc = addToSmsc;
    }

    public void setHostMsgId(String HostMsgId) {
        this.HostMsgId = HostMsgId;
    }

    public void setRoute_to_SMSC(String Route_to_SMSC) {
        this.Route_to_SMSC = Route_to_SMSC;
    }

    public void setContent(String Content) {
        this.Content = Content;
    }

    public void setDest_No(String Dest_No) {
        this.Dest_No = Dest_No;
    }

    public void setRec_Time(String Rec_Time) {
        this.Rec_Time = Rec_Time;
    }

    public void setSub_Time(String Sub_Time) {
        this.Sub_Time = Sub_Time;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTotalSmsSend(int totalSmsSend) {
        this.totalSmsSend = totalSmsSend;
    }

    public void setPaidSms(String paidSms) {
        this.paidSms = paidSms;
    }

    public void setUnpaidSms(String unpaidSms) {
        this.unpaidSms = unpaidSms;
    }

    public void setAmountPaid(String amountPaid) {
        this.amountPaid = amountPaid;
    }
//--------------------------------------------------

    public int getTotalSmsSend() {
        return totalSmsSend;
    }

    public String getPaidSms() {
        return paidSms;
    }

    public String getUnpaidSms() {
        return unpaidSms;
    }

    public String getAmountPaid() {
        return amountPaid;
    }

    public String getFilename() {
        return filename;
    }

    public String getCost() {
        return cost;
    }

    public String getHtiMsgId() {
        return htiMsgId;
    }

    

    public String getSmscId() {
        return smscId;
    }

    public String getSmscMsgId() {
        return smscMsgId;
    }

    public String getSender() {
        return sender;
    }

    public String getDestination() {
        return destination;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }

    public int getDelivered() {
        return delivered;
    }

    public String getDate() {
        return date;
    }

    public int getNonDelivered() {
        return nonDelivered;
    }

    public int getInProcess() {
        return inProcess;
    }

    public int getAddToSmsc() {
        return addToSmsc;
    }

    public String getResponseId() {

        return responseId;
    }

    public String getRoute() {

        return route;
    }

    public String getDoneTime() {

        return doneTime;
    }

    public String getHostMsgId() {
        return HostMsgId;
    }

    public String getRoute_to_SMSC() {
        return Route_to_SMSC;
    }

    public String getContent() {
        return Content;
    }

    public String getDest_No() {
        return Dest_No;
    }

    public String getRec_Time() {
        return Rec_Time;
    }

    public String getSub_Time() {
        return Sub_Time;

    }

}

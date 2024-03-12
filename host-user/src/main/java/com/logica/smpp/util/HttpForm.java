//////////////////by raj//////////////////////////
package com.logica.smpp.util;

import java.util.*;

public class HttpForm {

    public String header;
    public String client_name;
    public String password;
    public String destination;
    public String sender_id;
    public String message;
    public String dcs;
    public String esm;
    public String msgType;
    public String route;
    public String english;
    public String concat_english;
    public String unicode;
    public String concat_unicode;
    public String ringtone;
    public String concat_ringtone;
    public String variableOne;
    public String variableTwo;
    public String variableThree;
    public String variableFour;
    public String variableFive;
    public String udh; // Added by abhishek 101009
/*
    //@@ SAMEER BHASIN for HTTPSMSC
    public String senderIDFlag;
    public String senderID;
    public String senderID_List;
     */

    public void setHeader(String header) {
        this.header = header;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDcs(String dcs) {
        this.dcs = dcs;
    }

    public void setEsm(String esm) {
        this.esm = esm;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public void setConcat_english(String concat_english) {
        this.concat_english = concat_english;
    }

    public void setUnicode(String unicode) {
        this.unicode = unicode;
    }

    public void setConcat_unicode(String concat_unicode) {
        this.concat_unicode = concat_unicode;

    }

    public void setRingtone(String ringtone) {
        this.ringtone = ringtone;
    }

    public void setConcat_ringtone(String concat_ringtone) {
        this.concat_ringtone = concat_ringtone;
    }

    public String getHeader() {
        return header;
    }

    public String getClient_name() {
        return client_name;
    }

    public String getPassword() {
        return password;
    }

    public String getDestination() {
        return destination;
    }

    public String getSender_id() {
        return sender_id;
    }

    public String getMessage() {
        return message;
    }

    public String getDcs() {
        return dcs;
    }

    public String getEsm() {
        return esm;
    }

    public String getMsgType() {
        return msgType;
    }

    public String getRoute() {
        return route;
    }

    public String getEnglish() {
        return english;
    }

    public String getConcat_english() {
        return concat_english;
    }

    public String getUnicode() {
        return unicode;
    }

    public String getConcat_unicode() {
        return concat_unicode;
    }

    public String getRingtone() {
        return ringtone;
    }

    public String getConcat_ringtone() {
        return concat_ringtone;
    }
    /*
    public void setSenderIDFlag(String flag){
    senderIDFlag = flag;
    }

    public String getSenderIDFlag(){
    return senderIDFlag;
    }

    public void setSenderID(String sender_ID){
    senderID = sender_ID;
    }

    public String getSenderID(){
    return senderID;
    }

    public void setSenderID_List(String senderIDList){
    senderID_List = senderIDList;
    }

    public String getSenderIDList(){
    return senderID_List;
    }
     */
///@@@ ADDITION BY SAMEER BHASIN on 020-03-2007
///for addiing more parameters in httpsmsc

    public void setVariableOne(String variable_one) {
        variableOne = variable_one;
    }

    public String getVariableOne() {
        return variableOne;
    }

    public void setVariableTwo(String variable_Two) {
        variableTwo = variable_Two;
    }

    public String getVariableTwo() {
        return variableTwo;
    }

    public void setVariableThree(String variable_Three) {
        variableThree = variable_Three;
    }

    public String getVariableThree() {
        return variableThree;
    }

    public void setVariableFour(String variable_Four) {
        variableFour = variable_Four;
    }

    public String getVariableFour() {
        return variableFour;
    }

    public void setVariableFive(String variable_Five) {
        variableFive = variable_Five;
    }

    public String getVariableFive() {
        return variableFive;
    }

    public void setUdh(String UDH) {
        udh = UDH;
    }

    public String getUdh() {
        return udh;
    }
///@@@@
}
//Implemented By rajeev dated 24/06/05
package com.hti.objects;

import java.io.Serializable;

public class DeliverBackup implements Serializable {

    String client_name = null;
    byte s_ton;
    byte s_npi;
    String source = null;
    byte d_ton;
    byte d_npi;
    String destination = null;
    String shortmessage = null;
    String message_to_deliver = null;    
    private String msg_id;
    public DeliverBackup() {
    }

    public DeliverBackup(String name, byte ton, byte npi, byte dton, byte dnpi, String source, String dest, String msg) {
        this.client_name = name;
        this.s_ton = ton;
        this.s_npi = npi;
        this.d_ton = dton;
        this.d_npi = dnpi;
        this.source = source;
        this.destination = dest;
        this.shortmessage = "abcd";
        this.message_to_deliver = msg;
    }

    public void setClient(String name) {
        this.client_name = name;
    }

    public String getClient() {
        return client_name;
    }

    public void setSTon(byte ton) {
        this.s_ton = ton;
    }

    public void setSNpi(byte npi) {
        this.s_npi = npi;
    }

    public void setDTon(byte ton) {
        this.d_ton = ton;
    }

    public void setDNpi(byte npi) {
        this.d_npi = npi;
    }

    public void setMessage_toDeliver(String msg) {
        this.message_to_deliver = msg;
    }

    public String getMessage_toDeliver() {
        return message_to_deliver;
    }

    public void setMessage(String msg) {
        this.shortmessage = msg;
    }

    public String getMessage() {
        return shortmessage;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setDestination(String dest) {
        this.destination = dest;
    }

    public String getDestination() {
        return destination;
    }

    public byte getSTon() {
        return s_ton;
    }

    public byte getSNpi() {
        return s_npi;
    }

    public byte getDTon() {
        return d_ton;
    }

    public byte getDNpi() {
        return d_npi;
    }

    /**
     * @return the msg_id
     */
    public String getMsg_id() {
        return msg_id;
    }

    /**
     * @param msg_id the msg_id to set
     */
    public void setMsg_id(String msg_id) {
        this.msg_id = msg_id;
    }

}

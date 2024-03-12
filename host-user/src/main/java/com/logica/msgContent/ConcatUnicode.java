/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logica.msgContent;

import com.logica.smpp.util.ByteBuffer;
import java.io.Serializable;

/**
 *
 * @author Raj
 */
public class ConcatUnicode implements Serializable{

    String msg;
    ByteBuffer byteBuffer;
    public void setMsg(String message) {
        this.msg = message;
    }
    public String getMsg(){
        return this.msg;
    }

    public void setByteBuffer(ByteBuffer buffer){
        this.byteBuffer= buffer;
    }
    public ByteBuffer getByteBuffer(){
        return this.byteBuffer;
    }
}

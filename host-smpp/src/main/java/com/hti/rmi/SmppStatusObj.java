/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.rmi;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public class SmppStatusObj implements Serializable{
    
    private Map smscQueue;
    private int downCount;
    private Map downUser;
    private Map downSmsc;
    private int nrCount;
    private Map nrUser;
    private Map nrSmsc;
    private Map respError;
    private Map respWaiting;
    private Map dlrWaiting;
    private Map userSession;
    private Map lastDlrRecieve;

    public Map getSmscQueue() {
        return smscQueue;
    }

    public void setSmscQueue(Map smscQueue) {
        this.smscQueue = smscQueue;
    }

    public int getDownCount() {
        return downCount;
    }

    public void setDownCount(int downCount) {
        this.downCount = downCount;
    }

    public Map getDownUser() {
        return downUser;
    }

    public void setDownUser(Map downUser) {
        this.downUser = downUser;
    }

    public Map getDownSmsc() {
        return downSmsc;
    }

    public void setDownSmsc(Map downSmsc) {
        this.downSmsc = downSmsc;
    }

    public int getNrCount() {
        return nrCount;
    }

    public void setNrCount(int nrCount) {
        this.nrCount = nrCount;
    }

    public Map getNrUser() {
        return nrUser;
    }

    public void setNrUser(Map nrUser) {
        this.nrUser = nrUser;
    }

    public Map getNrSmsc() {
        return nrSmsc;
    }

    public void setNrSmsc(Map nrSmsc) {
        this.nrSmsc = nrSmsc;
    }

    public Map getRespError() {
        return respError;
    }

    public void setRespError(Map respError) {
        this.respError = respError;
    }

    public Map getRespWaiting() {
        return respWaiting;
    }

    public void setRespWaiting(Map respWaiting) {
        this.respWaiting = respWaiting;
    }

    public Map getDlrWaiting() {
        return dlrWaiting;
    }

    public void setDlrWaiting(Map dlrWaiting) {
        this.dlrWaiting = dlrWaiting;
    }

    public Map getUserSession() {
        return userSession;
    }

    public void setUserSession(Map userSession) {
        this.userSession = userSession;
    }

    public Map getLastDlrRecieve() {
        return lastDlrRecieve;
    }

    public void setLastDlrRecieve(Map lastDlrRecieve) {
        this.lastDlrRecieve = lastDlrRecieve;
    }
    
    
    
}

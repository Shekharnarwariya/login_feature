package com.hti.smpp.common.messages.dto;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class BulkListInfo {

    private int total;
    private int validCount;
    private int invalidCount;
    private int duplicate;
    private Map errors;

    public void setTotal(int total) {
        this.total = total;
    }

    public void setDuplicate(int duplicate) {
        this.duplicate = duplicate;
    }

    public void setValidCount(int validCount) {
        this.validCount = validCount;
    }

    public void setInvalidCount(int invalidCount) {
        this.invalidCount = invalidCount;
    }

    public int getTotal() {
        return total;
    }

    public int getDuplicate() {
        return duplicate;
    }

    public int getValidCount() {
        return validCount;
    }

    public int getInvalidCount() {
        return invalidCount;
    }

    public Map getErrors() {
        return errors;
    }

    public void setErrors(Map errors) {
        this.errors = errors;
    }
    
    
}

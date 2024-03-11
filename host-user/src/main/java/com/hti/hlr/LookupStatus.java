/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.hlr;

/**
 *
 * @author Administrator
 */
public class LookupStatus {

    public static final String NO_ERROR = "000";
    public static final String INVALID_LOGIN = "001";
    public static final String INVALID_REQUEST = "002";
    public static final String ACCOUNT_EXPIRED = "003";
    public static final String INSUF_BALANCE = "004";
    public static final String INVALID_DEST_ADDR = "005";
    public static final String NO_COVERAGE = "006";
    public static final String SYSTEM_ERROR = "007";
    // -------------------------------------------------
    public static final String ABSENT_SUBSCRIBER = "011";
    public static final String UNKNOWN_SUBSCRIBER = "012";
    public static final String UNIDENTIFIED_SUBSCRIBER = "013";
    public static final String TIMEOUT = "014";
    public static final String PROVIDER_GENERAL_ERROR = "015";
    public static final String NO_RESPONSE = "016";
    public static final String SYSTEM_FAILURE = "017";
    public static final String INVALID_MSISDN = "018";
    public static final String CALL_BARRED = "019";
    public static final String ILLEGAL_SUBSCRIBER = "020";
    public static final String TELESERVICE_NOT_PROVISIONED = "021";
    public static final String FACILITY_NOT_SUPPORTED = "022";
    public static final String SUBSCRIBER_BUSY_FOR_MT_SMS = "023";
    public static final String UNEXPECTED_DATA_VALUE = "024";
    public static final String ABSENT_SUBSCRIBER_TMP = "025"; // Absent but not permanent (due to phone turned off or out of coverage or low signal)
    public static final String DELIVERY_FAILURE = "026";

    public static String getErrorName(String error_code) {
        String error = null;
        if (error_code.equalsIgnoreCase(LookupStatus.NO_ERROR)) {
            error = "NO_ERROR";
        } else if (error_code.equalsIgnoreCase(LookupStatus.ABSENT_SUBSCRIBER)) {
            error = "ABSENT_SUBSCRIBER";
        } else if (error_code.equalsIgnoreCase(LookupStatus.UNKNOWN_SUBSCRIBER)) {
            error = "UNKNOWN_SUBSCRIBER";
        } else if (error_code.equalsIgnoreCase(LookupStatus.UNIDENTIFIED_SUBSCRIBER)) {
            error = "UNIDENTIFIED_SUBSCRIBER";
        } else if (error_code.equalsIgnoreCase(LookupStatus.TIMEOUT)) {
            error = "TIMEOUT";
        } else if (error_code.equalsIgnoreCase(LookupStatus.PROVIDER_GENERAL_ERROR)) {
            error = "PROVIDER_GENERAL_ERROR";
        } else if (error_code.equalsIgnoreCase(LookupStatus.NO_RESPONSE)) {
            error = "NO_RESPONSE";
        } else if (error_code.equalsIgnoreCase(LookupStatus.SYSTEM_FAILURE)) {
            error = "SYSTEM_FAILURE";
        } else if (error_code.equalsIgnoreCase(LookupStatus.CALL_BARRED)) {
            error = "CALL_BARRED";
        } else if (error_code.equalsIgnoreCase(LookupStatus.ILLEGAL_SUBSCRIBER)) {
            error = "ILLEGAL_SUBSCRIBER";
        } else if (error_code.equalsIgnoreCase(LookupStatus.TELESERVICE_NOT_PROVISIONED)) {
            error = "TELESERVICE_NOT_PROVISIONED";
        } else if (error_code.equalsIgnoreCase(LookupStatus.FACILITY_NOT_SUPPORTED)) {
            error = "FACILITY_NOT_SUPPORTED";
        } else if (error_code.equalsIgnoreCase(LookupStatus.SUBSCRIBER_BUSY_FOR_MT_SMS)) {
            error = "SUBSCRIBER_BUSY_FOR_MT_SMS";
        } else if (error_code.equalsIgnoreCase(LookupStatus.UNEXPECTED_DATA_VALUE)) {
            error = "UNEXPECTED_DATA_VALUE";
        } else if (error_code.equalsIgnoreCase(LookupStatus.ABSENT_SUBSCRIBER_TMP)) {
            error = "ABSENT_SUBSCRIBER_TMP";
        }else if (error_code.equalsIgnoreCase(LookupStatus.DELIVERY_FAILURE)) {
            error = "DELIVERY_FAILURE";
        }
        return error;
    }
}

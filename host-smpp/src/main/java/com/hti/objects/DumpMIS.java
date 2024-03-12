/*
 * DumpMIS.java
 *
 * Created on August 6, 2008, 12:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.hti.objects;

/**
 *
 * @author XP
 */
public class DumpMIS {

    private String msg_id;
    private String time;
    private String status;
    private String errorcode;
    private String response_id;
    private String route_to_smsc;
    private String source_id;
    private String destination_no;

    /** Creates a new instance of DumpMIS */
    public DumpMIS(String getmsg_id, String gettime, String getStatus, String geterrorcode, String respns_id, String smsc_name,String source_no,String dest_no) {

        msg_id = getmsg_id;
        time = gettime;
        status = getStatus;
        errorcode = geterrorcode;
        response_id = respns_id;
        route_to_smsc = smsc_name;
        source_id = source_no;
        destination_no = dest_no;
    }

    public String getmsg_id() {


        return msg_id;
    }

    public String gettime() {


        return time;
    }

    public String getStatus() {

        return status;
    }

    public String geterrorcode() {

        return errorcode;
    }

    public String getResponseId() {

        return response_id;
    }

    public String getSmscName() {

        return route_to_smsc;
    }
    public String getSourceId(){

        return source_id;
   }
   public String getDestionationNo(){

       return destination_no;
   }

}

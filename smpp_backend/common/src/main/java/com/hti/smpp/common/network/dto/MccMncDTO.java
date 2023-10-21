package com.hti.smpp.common.network.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

//import org.apache.struts.upload.FormFile;
//import org.springframework.web.multipart.MultipartFile;  

@Entity
@Table(name="mccmnc")
public class MccMncDTO {


    @Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String country;
    private String operator;
    private String cc;
    private String mcc;
    private String mnc;
    private String prefix;
    //private MultipartFile listfile;
   // private FormFile listfile;
    private String checkCountry;
    private String checkMcc;
    private String checkMnc;

    /* public MultipartFile getListfile() {
        return listfile;
    }

    public void setListfile(MultipartFile listfile) {
        this.listfile = listfile;
    } */

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getCheckCountry() {
        return checkCountry;
    }

    public void setCheckCountry(String checkCountry) {
        this.checkCountry = checkCountry;
    }

    public String getCheckMcc() {
        return checkMcc;
    }

    public void setCheckMcc(String checkMcc) {
        this.checkMcc = checkMcc;
    }

    public String getCheckMnc() {
        return checkMnc;
    }

    public void setCheckMnc(String checkMnc) {
        this.checkMnc = checkMnc;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getMnc() {
        return mnc;
    }

    public void setMnc(String mnc) {
        this.mnc = mnc;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }


}

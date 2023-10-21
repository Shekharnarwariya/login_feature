
package com.hti.smpp.common.url.dto;

import org.springframework.stereotype.Component;

@Component
public class SmsUrlDTO 
{
	private String user;
	private String pass;
	private String sid;
	private String mno;
	private String text;
	private String type;
	private String esm;
	private String dcs;
	private String ston;
	private String snpi;
	private String dton;
	private String dnpi;
    private String header;
	private String respMesg;

	public void setUser(String user){
		this.user = user;
	}

	public void setPass(String pass){
		this.pass = pass;
	}

	public void setSid(String sid){
		this.sid = sid;
	}

	public void setMno(String mno){
		this.mno = mno;
	}

	public void setText(String text){
		this.text = text;
	}

	public void setType(String type){
		this.type =type;
	}

	public void setEsm(String esm){
		this.esm = esm;
	}

	public void setDcs(String dcs){
		this.dcs= dcs;
	}

	public void setSton(String ston){
	
		this.ston = ston;
	}

	public void setSnpi(String Snpi){
		this.snpi = snpi;
	}

	public void setDton(String dton){
		this.dton = dton;
	}

	public void setDnpi(String dnpi){
		this.dnpi = dnpi;
	}

public void setHeader(String header){
		this.header = header;
	}

	public void setRespMesg(String respMesg){
		this.respMesg = respMesg;
	}


/////////////////////////////////////////////////////////////////
/////////////////// GETTER METHODS ///////////////////////////////
	public String getRespMesg(){
		return respMesg;
	}

	public String getUser(){
		return user;
	}

	public String getPass(){
		return pass;
	}

	public String getSid(){
		return sid;
	}

	public String getMno(){
		return mno;
	}

	public String getText(){
		return text;
	}

	public String getType(){
		return type;
	}

	public String getEsm(){
		return esm;
	}

	public String getDcs(){
		return dcs;
	}

	public String getSton(){
		return ston;
	}

	public String getSnpi(){
		return snpi;
	}

	public String getDton(){
		return dton;
	}
	
	public String getDnpi(){
		return dnpi;
	}
public String getHeader(){
		return header;
	}

}

/*
 * DatabaseDumpObject.java
 *
 * Created on 25 March 2004, 13:23
 */
package com.hti.objects;

import com.logica.msgContent.ConcatUnicode;

/**
 *
 * @author administrator
 */
public class DatabaseDumpObject {
	private String content;
	private com.logica.smpp.pdu.Address from;
	private com.logica.smpp.pdu.Address destination_no;
	private int seq_no;
	private int dest_TON;
	private int dest_NPI;
	private int source_TON;
	private int source_NPI;
	private byte registered;
	private byte ESM;// rajniprabha03122007
	private byte DCS;// rajniprabha03122007
	private RoutePDU routePdu;
	// Added by Amit_Vish
	private String msg_type;
	private ConcatUnicode concate;
	private String operatorCountry;
	private String s_flag;
	private boolean temp;

	public DatabaseDumpObject(String getcontent, com.logica.smpp.pdu.Address getfrom,
			com.logica.smpp.pdu.Address getdestination_no, int getseq_no, byte isRegitered, byte esm, byte dcs,
			RoutePDU route) {
		content = getcontent;
		source_TON = getfrom.getTon();
		source_NPI = getfrom.getNpi();
		from = getfrom;
		dest_TON = getdestination_no.getTon();
		dest_NPI = getdestination_no.getNpi();
		destination_no = getdestination_no;
		registered = isRegitered;
		seq_no = getseq_no;
		ESM = esm;
		DCS = dcs;
		routePdu = route;
	}

	public String getContent() {
		return content;
	}

	public String getFrom() {
		return from.getAddress();
	}

	public String getDestinationNo() {
		return destination_no.getAddress();
	}

	public int getSeqNo() {
		return seq_no;
	}

	/*
	 * public String getSMSCID() { return SMSCID; }
	 */
	public int getDestinationTON() {
		return dest_TON;
	}

	public int getDestinationNPI() {
		int NPI = dest_NPI & 0xff;// Done by rajniprabha
		return NPI;
	}

	public int getSource_TON() {
		return source_TON;
	}

	public int getSource_NPI() {
		int NPI = source_NPI & 0xff;// Done by rajniprabha
		return NPI;
	}

	public byte isRegistered() {
		return registered;
	}

	public int getDcs() {
		int datacoding = DCS & 0xff;
		return datacoding;
	}

	public int getEsm() {
		int esm = ESM & 0xff;
		return ESM;
	}
	// ********************************************rajni23may2007

	public RoutePDU getRoute() {
		return routePdu;
	}

	public ConcatUnicode getConcate() {
		return concate;
	}

	public void setConcate(ConcatUnicode concate) {
		this.concate = concate;
	}

	public String getMsg_type() {
		return msg_type;
	}

	public void setMsg_type(String msg_type) {
		this.msg_type = msg_type;
	}

	public String getOperatorCountry() {
		return operatorCountry;
	}

	public void setOperatorCountry(String operatorCountry) {
		this.operatorCountry = operatorCountry;
	}

	public String getS_flag() {
		return s_flag;
	}

	public void setS_flag(String s_flag) {
		this.s_flag = s_flag;
	}

	public boolean isTemp() {
		return temp;
	}

	public void setTemp(boolean temp) {
		this.temp = temp;
	}
	/*
	 * public String getMultiDestinationNo(){
	 * 
	 * com.logica.smpp.pdu.Address address=multi_destination_no.getAddress(); return address.getAddress(); }
	 */
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.smsc;

/**
 *
 * @author Gopi Krishna Iyer
 */
public class SpecialSMSCSetting {
	private String smsc;
	private int length;
	private int l_ston;
	private int l_snpi;
	private int g_ston;
	private int g_snpi;

	public String getSmsc() {
		return smsc;
	}

	public void setSmsc(String smsc) {
		this.smsc = smsc;
	}

	public int getL_ston() {
		return l_ston;
	}

	public void setL_ston(int l_ston) {
		this.l_ston = l_ston;
	}

	public int getL_snpi() {
		return l_snpi;
	}

	public void setL_snpi(int l_snpi) {
		this.l_snpi = l_snpi;
	}

	public int getG_ston() {
		return g_ston;
	}

	public void setG_ston(int g_ston) {
		this.g_ston = g_ston;
	}

	public int getG_snpi() {
		return g_snpi;
	}

	public void setG_snpi(int g_snpi) {
		this.g_snpi = g_snpi;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String toString() {
		return "SmscSpecialSetting: smsc=" + smsc + ",length=" + length;
	}
}

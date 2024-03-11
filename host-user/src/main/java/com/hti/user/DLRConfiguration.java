package com.hti.user;

public class DLRConfiguration {
	private String gmt;
	private boolean reverse;
	private boolean nnc;
	private boolean fixedCode;
	private boolean DLRthroughWEB;
	private String webURL;
	private boolean accepted;

	public DLRConfiguration() {
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	public void setGmt(String gmt) {
		this.gmt = gmt;
	}

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}

	public void setNnc(boolean nnc) {
		this.nnc = nnc;
	}

	public void setFixedCode(boolean fixedCode) {
		this.fixedCode = fixedCode;
	}

	public void setDLRthroughWEB(boolean dLRthroughWEB) {
		DLRthroughWEB = dLRthroughWEB;
	}

	public void setWebURL(String webURL) {
		this.webURL = webURL;
	}

	public boolean isDLRthroughWEB() {
		return DLRthroughWEB;
	}

	public String getGmt() {
		return gmt;
	}

	public boolean isReverse() {
		return reverse;
	}

	public boolean isNnc() {
		return nnc;
	}

	public boolean isFixedCode() {
		return fixedCode;
	}

	public String getWebURL() {
		return webURL;
	}
}

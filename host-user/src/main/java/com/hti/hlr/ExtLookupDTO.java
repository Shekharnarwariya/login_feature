package com.hti.hlr;

public class ExtLookupDTO {
	private String status;
	private String nnc;
	private String errorCode;
	private String error;
	private boolean ported;
	private String portedNNC;
	private boolean roaming;
	private String roamingNNC;

	public ExtLookupDTO() {
	}

	public ExtLookupDTO(String status) {
		this.status = status;
	}

	public ExtLookupDTO(String status, String errorCode, String error) {
		this.status = status;
		this.errorCode = errorCode;
		this.error = error;
	}

	public ExtLookupDTO(String status, boolean ported, String portedNNC) {
		this.status = status;
		this.ported = ported;
		this.portedNNC = portedNNC;
	}

	public ExtLookupDTO(String status, String nnc, boolean ported, String portedNNC, boolean roaming, String roamingNNC,
			String errorCode, String error) {
		this.status = status;
		this.nnc = nnc;
		this.ported = ported;
		this.portedNNC = portedNNC;
		this.roaming = roaming;
		this.roamingNNC = roamingNNC;
		this.errorCode = errorCode;
		this.error = error;
	}

	public String getStatus() {
		return status;
	}

	public String getPortedNNC() {
		return portedNNC;
	}

	public String getRoamingNNC() {
		return roamingNNC;
	}

	public boolean isPorted() {
		return ported;
	}

	public boolean isRoaming() {
		return roaming;
	}

	public String getNnc() {
		return nnc;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getError() {
		return error;
	}
}

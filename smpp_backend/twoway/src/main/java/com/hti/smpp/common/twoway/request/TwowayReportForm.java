package com.hti.smpp.common.twoway.request;
/**
 * The `TwowayReportForm` class represents the form for generating a Two-Way Report.
 */
public class TwowayReportForm {
	
	private int[] userId;
	private String shortCode;
	private String keyword;
	private String startTime;
	private String endTime;
	private String[] type;
	
	public TwowayReportForm() {
		super();
	}
/**
 *  Parameterized constructor for creating a `TwowayReportForm` instance with provided values.
 * @param userId
 * @param shortCode
 * @param keyword
 * @param startTime
 * @param endTime
 * @param type
 */
	public TwowayReportForm(int[] userId, String shortCode, String keyword, String startTime, String endTime,
			String[] type) {
		super();
		this.userId = userId;
		this.shortCode = shortCode;
		this.keyword = keyword;
		this.startTime = startTime;
		this.endTime = endTime;
		this.type = type;
	}

//Getter and Setter
	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public int[] getUserId() {
		return userId;
	}

	public void setUserId(int[] userId) {
		this.userId = userId;
	}

	public String[] getType() {
		return type;
	}

	public void setType(String[] type) {
		this.type = type;
	}

}

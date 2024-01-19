/**
 * 
 */
package com.hti.smpp.common.response;

/**
 * 
 */
public class MessageIdentiryResponse {

	private String message;

	private String messageType; // Encoding

	private int smscount;

	private int charCount; // Char Count

	private int charLimit; // Char limit per sms

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the messageType
	 */
	public String getMessageType() {
		return messageType;
	}

	/**
	 * @param messageType the messageType to set
	 */
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	/**
	 * @return the smscount
	 */
	public int getSmscount() {
		return smscount;
	}

	/**
	 * @param smscount the smscount to set
	 */
	public void setSmscount(int smscount) {
		this.smscount = smscount;
	}

	/**
	 * @return the charCount
	 */
	public int getCharCount() {
		return charCount;
	}

	/**
	 * @param charCount the charCount to set
	 */
	public void setCharCount(int charCount) {
		this.charCount = charCount;
	}

	/**
	 * @return the charLimit
	 */
	public int getCharLimit() {
		return charLimit;
	}

	/**
	 * @param charLimit the charLimit to set
	 */
	public void setCharLimit(int charLimit) {
		this.charLimit = charLimit;
	}

}

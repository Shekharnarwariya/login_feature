package com.hti.smpp.common.messages.dto;

public class BulkContentEntry {

	private int id;

	private long destination;

	private String content;

	private String flag;

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the destination
	 */
	public long getDestination() {
		return destination;
	}

	/**
	 * @param destination the destination to set
	 */
	public void setDestination(long destination) {
		this.destination = destination;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the flag
	 */
	public String getFlag() {
		return flag;
	}

	/**
	 * @param flag the flag to set
	 */
	public void setFlag(String flag) {
		this.flag = flag;
	}

	/**
	 * 
	 */
	public BulkContentEntry() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param id
	 * @param destination
	 * @param content
	 * @param flag
	 */
	public BulkContentEntry(long destination, String content, String flag) {
		super();
		this.destination = destination;
		this.content = content;
		this.flag = flag;
	}

	@Override
	public String toString() {
		return "BulkContentEntry [id=" + id + ", destination=" + destination + ", content=" + content + ", flag=" + flag
				+ "]";
	}

}

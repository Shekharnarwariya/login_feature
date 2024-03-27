package com.hti.smpp.common.response;

public class UserNumbersResponse {

	private String destinationNumber;
	private int totalNumbers;

	public UserNumbersResponse(String destinationNumber, int totalNumbers) {
		this.destinationNumber = destinationNumber;
		this.totalNumbers = totalNumbers;
	}

	public String getDestinationNumber() {
		return destinationNumber;
	}

	public void setDestinationNumber(String destinationNumber) {
		this.destinationNumber = destinationNumber;
	}

	public int getTotalNumbers() {
		return totalNumbers;
	}

	public void setTotalNumbers(int totalNumbers) {
		this.totalNumbers = totalNumbers;
	}
}

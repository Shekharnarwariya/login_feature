package com.hti.smpp.common.request;
/**
 * Represents a request to update the user profile information in the application.
 */
public class ProfileUpdateRequest {

	private String email;

	public String getContact() {
		return Contact;
	}

	public void setContact(String contact) {
		Contact = contact;
	}

	private String firstName;

	private String LastName;

	private String Contact;
//Getter and Setter 
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return LastName;
	}

	public void setLastName(String lastName) {
		LastName = lastName;
	}

}

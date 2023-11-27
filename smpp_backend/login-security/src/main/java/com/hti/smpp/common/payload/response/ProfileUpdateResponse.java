package com.hti.smpp.common.payload.response;

public class ProfileUpdateResponse {
	private String email;

	private String Base64Password;

	private String firstName;

	private String LastName;

	private String language;

		public String getContact() {
		return Contact;
	}

	public void setContact(String contact) {
		Contact = contact;
	}

		private String Contact ;
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getBase64Password() {
		return Base64Password;
	}

	public void setBase64Password(String base64Password) {
		Base64Password = base64Password;
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

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	
}

package com.hti.smpp.common.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Represents a request to update the user profile information in the
 * application.
 */
public class ProfileUpdateRequest {

	@NotBlank(message = "Please enter your email.")
	@Email(message = "Please enter a valid email address.")
	private String email;

	@NotBlank(message = "Please enter your first name.")
	@Size(max = 50, message = "First name cannot exceed 50 characters.")
	private String firstName;

	@NotBlank(message = "Please enter your last name.")
	@Size(max = 50, message = "Last name cannot exceed 50 characters.")
	private String lastName;

	@NotBlank(message = "Please enter your contact number.")
	@Pattern(regexp = "\\d{10}", message = "Contact number must be 10 digits.")
	private String contact;

	public String getEmail() {
		return email;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
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
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

}

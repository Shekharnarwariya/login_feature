package com.hti.smpp.common.sms.util;

import jakarta.servlet.http.HttpSession;

public class ProgressEvent {
	private HttpSession session;

	public ProgressEvent(HttpSession session) {
		this.session = session;
		this.session.setAttribute("upload_percent", String.valueOf(0));
	}

	public void updateProgress(int percent) {
		session.setAttribute("upload_percent", String.valueOf(percent));
	}

}

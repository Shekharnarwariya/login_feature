package com.hti.smpp.common.session;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.hti.smpp.common.user.dto.UserSessionObject;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Component
public class SessionHelper {
	public static boolean valid = false;

	public static boolean hasValidSession(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			return (UserSessionObject) session.getAttribute("userSessionObject") != null;
		}
		return false;
	}

	public static Object getSessionObject(String attributeName) {
		Object obj = null;
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		if (attr != null) {
			HttpSession session = attr.getRequest().getSession(false);
			if (session != null) {
				obj = session.getAttribute(attributeName);
			}
		}
		return obj;
	}

	public static void storeInSession1(HttpServletRequest request, HttpServletRequest response, String attributeName,
			Object attribute) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.setAttribute(attributeName, attribute);
		}
	}
}

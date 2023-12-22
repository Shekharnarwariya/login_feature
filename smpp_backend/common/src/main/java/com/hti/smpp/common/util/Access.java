package com.hti.smpp.common.util;

import java.lang.reflect.Method;

public class Access {
	public enum ERole {
		ADMIN, SUPERADMIN, SYSTEM, USER
	}

	public static boolean isAuthorized(String role, String methodName) {
		try {
			ERole userRole = ERole.valueOf(role.toUpperCase());
			Method targetMethod = Access.class.getDeclaredMethod(methodName, ERole.class);
			return (boolean) targetMethod.invoke(null, userRole);
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isAuthorizedAll(ERole role) {
		return role == ERole.SUPERADMIN || role == ERole.ADMIN || role == ERole.SYSTEM || role == ERole.USER;
	}

	public static boolean isAuthorizedSuperAdminAndAdmin(ERole role) {
		return role == ERole.SUPERADMIN || role == ERole.ADMIN;
	}

	public static boolean isAuthorizedAdminAndUser(ERole role) {
		return role == ERole.ADMIN || role == ERole.USER;
	}

	public static boolean isAuthorizedSuperAdminAndUser(ERole role) {
		return role == ERole.SUPERADMIN || role == ERole.USER;
	}

	public static boolean isAuthorizedSuperAdminAndSystem(ERole role) {
		return role == ERole.SUPERADMIN || role == ERole.SYSTEM;
	}

	public static boolean isAuthorizedSuperAdminAndSystemAndAdmin(ERole role) {
		return role == ERole.SUPERADMIN || role == ERole.SYSTEM || role == ERole.ADMIN;
	}

	public static boolean isAuthorizedAdmin(ERole role) {
		return role == ERole.ADMIN;
	}

	public static boolean isAuthorizedUser(ERole role) {
		return role == ERole.USER;
	}

	public static boolean isAuthorizedSystem(ERole role) {
		return role == ERole.SYSTEM;
	}
}

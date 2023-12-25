package com.hti.smpp.common.util;

import java.lang.reflect.Method;
/**
 * Utility class for checking user authorization based on roles.
 */
public class Access {
	public enum ERole {
		ADMIN, SUPERADMIN, SYSTEM, USER
	}

	/**
     * Enumeration representing user roles.
     */
	
	/**
     * Check if the specified role is authorized to access the method with the given name.
     *
     * @param role       The user role to check.
     * @param methodName The name of the method to check authorization for.
     * @return True if authorized, false otherwise.
     */
	
	public static boolean isAuthorized(String role, String methodName) {
		try {
			ERole userRole = ERole.valueOf(role.toUpperCase());
			Method targetMethod = Access.class.getDeclaredMethod(methodName, ERole.class);
			return (boolean) targetMethod.invoke(null, userRole);
		} catch (Exception e) {
			return false;
		}
	}

	/**
     * Check if the specified role is authorized for all access levels.
     */
	
	public static boolean isAuthorizedAll(ERole role) {
		return role == ERole.SUPERADMIN || role == ERole.ADMIN || role == ERole.SYSTEM || role == ERole.USER;
	}

	/**
     * Check if the specified role is authorized for SuperAdmin and Admin access levels.
     */
	
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

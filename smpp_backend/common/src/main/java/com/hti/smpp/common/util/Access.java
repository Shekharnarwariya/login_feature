package com.hti.smpp.common.util;

import java.util.Set;

import com.hti.smpp.common.login.dto.ERole;
import com.hti.smpp.common.login.dto.Role;

public class Access {

	public static boolean isAuthorizedAll(Set<Role> roles) {
		return roles.stream().anyMatch(
				role -> role.getName().equals(ERole.ROLE_SUPERADMIN) || role.getName().equals(ERole.ROLE_ADMIN)
						|| role.getName().equals(ERole.ROLE_SYSTEM) || role.getName().equals(ERole.ROLE_USER));
	}

	public static boolean isAuthorizedSuperAdminAndAdmin(Set<Role> roles) {
		return roles.stream().anyMatch(
				role -> role.getName().equals(ERole.ROLE_SUPERADMIN) || role.getName().equals(ERole.ROLE_ADMIN));
	}

	public static boolean isAuthorizedAdminAndUser(Set<Role> roles) {
		return roles.stream()
				.anyMatch(role -> role.getName().equals(ERole.ROLE_ADMIN) || role.getName().equals(ERole.ROLE_USER));
	}

	public static boolean isAuthorizedSuperAdminAndUser(Set<Role> roles) {
		return roles.stream().anyMatch(
				role -> role.getName().equals(ERole.ROLE_SUPERADMIN) || role.getName().equals(ERole.ROLE_USER));
	}

}

package com.skillsync.auth.exception;

public class RoleNotFoundException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RoleNotFoundException(String role) {
        super("Role not found: " + role);
    }
}
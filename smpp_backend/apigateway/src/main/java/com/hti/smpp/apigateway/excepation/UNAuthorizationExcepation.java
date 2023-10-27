package com.hti.smpp.apigateway.excepation;

public class UNAuthorizationExcepation extends RuntimeException {

	private static final long serialVersionUID = 1106671598754207461L;

	public UNAuthorizationExcepation(String msg) {
		super(msg);
	}
}
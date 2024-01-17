package com.hti.smpp.apigateway.jwt;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RouteValidator {

	public static final List<String> openApiEndpoints = List.of("/login/jwt", "/eureka", "/login/sendOTP",
			"/login/validateOtp","/login/forgotPassword");
	public Predicate<ServerHttpRequest> isSecured = request -> openApiEndpoints.stream()
			.anyMatch(uri -> request.getURI().getPath().contains(uri));

}
package com.hti.smpp.apigateway.jwt;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RouteValidator {
	 // List of open API endpoints that do not require authentication
	public static final List<String> openApiEndpoints = List.of("/auth/login", "/auth/signup", "/auth/validate",
			"/eureka", "/auth/send/otp", "/auth/password/forgot", "/auth/otp/validate");
	// Predicate to check if a given request is secured (requires authentication)
	 // Check if the request path contains any of the open API endpoints
	public Predicate<ServerHttpRequest> isSecured = request -> openApiEndpoints.stream()
			.noneMatch(uri -> request.getURI().getPath().contains(uri));
}

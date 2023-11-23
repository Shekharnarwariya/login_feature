package com.hti.smpp.apigateway.jwt;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;

@Component
public class RouteValidator {

	public static final List<String> openApiEndpoints = List.of("/auth/login", "/auth/signup", "/auth/validate",
			"/eureka", "/auth/send/otp", "/auth/password/forgot", "/auth/otp/validate");

	public Predicate<ServerHttpRequest> isSecured = request -> openApiEndpoints.stream()
			.noneMatch(uri -> request.getURI().getPath().contains(uri));

}
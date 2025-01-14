package com.hti.smpp.apigateway.jwt;

import java.nio.charset.StandardCharsets;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
	public AuthenticationFilter() {
		super(Config.class);
	}

	@Override
	public GatewayFilter apply(Config config) {
		// Check if the request needs authentication based on the RouteValidator
		return ((exchange, chain) -> {
			RouteValidator validator = new RouteValidator();
			if (!validator.isSecured.test(exchange.getRequest())) {
				System.out.println("url not found .." + !validator.isSecured.test(exchange.getRequest()));
				System.out.println(exchange.getRequest().getURI());
				// header contains token or not
				try {
					// Check if the authorization header is missing
					if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
						// Handle unauthorized request with a message

						return handleUnauthorized(exchange, "missing authorization header...!");

					}
					// Extract the token from the authorization header

					String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
					if (authHeader != null && authHeader.startsWith("Bearer ")) {
						authHeader = authHeader.substring(7);
					}

					JwtUtil jwtUtil = new JwtUtil();

					jwtUtil.validateToken(authHeader);
					exchange.getRequest().mutate().header("username", jwtUtil.getUserNameFromJwtToken(authHeader))
							.build();

				} catch (Exception e) {
					return handleUnauthorized(exchange, e.getMessage());
				}
			}
			System.out.println("url not found .." + !validator.isSecured.test(exchange.getRequest()));
			System.out.println(exchange.getRequest().getURI());
			return chain.filter(exchange.mutate().build());
		});
	}

	// Method to handle unauthorized requests
	private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String errorMessage) {
		exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
		exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		String errorResponse = "{\"error\": \"Unauthorized\", \"message\": \"" + errorMessage + "\"}";
		DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes(StandardCharsets.UTF_8));
		return exchange.getResponse().writeWith(Mono.just(buffer));
	}

	public static class Config {

	}
}

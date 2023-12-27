package com.hti.smpp.apigateway.jwt;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

	  // Autowired dependencies
	@Autowired
	private RouteValidator validator;

	@Autowired
	private JwtUtil jwtUtil;
	 // Default constructor for the filter factory
	public AuthenticationFilter() {
		super(Config.class);
	}
	  // ServerHttpRequest to be modified
	ServerHttpRequest request;
	// Method responsible for applying the authentication filter logic
	@Override
	public GatewayFilter apply(Config config) {
		 // Check if the request needs authentication based on the RouteValidator
		return ((exchange, chain) -> {
			if (validator.isSecured.test(exchange.getRequest())) {
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
					   // Validate the JWT token
					jwtUtil.validateToken(authHeader);
					  // Set "username" header in the request
					request = exchange.getRequest().mutate()
							.header("username", jwtUtil.getUserNameFromJwtToken(authHeader)).build();
					 // Handle unauthorized request with an error message
				} catch (Exception e) {
					return handleUnauthorized(exchange, e.getMessage());
				}
			}
	         // Continue the filter chain with the modified or original request
			return chain.filter(exchange.mutate().request(request).build());
		});
	}
	// Method to handle unauthorized requests
	private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String errorMessage) {
		exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
		exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		// Set the response status code to UNAUTHORIZED
		 // Set the content type to JSON
		  // Create an error response message
		String errorResponse = "{\"error\": \"Unauthorized\", \"message\": \"" + errorMessage + "\"}";
		DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes(StandardCharsets.UTF_8));
		  // Wrap the response message in a DataBuffer
		  // Write the response message to the exchange
		return exchange.getResponse().writeWith(Mono.just(buffer));
	}
    // Configuration class for the filter
	public static class Config {

	}
}

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

	@Autowired
	private RouteValidator validator;

	@Autowired
	private JwtUtil jwtUtil;

	public AuthenticationFilter() {
		super(Config.class);
	}

	ServerHttpRequest request;

	@Override
	public GatewayFilter apply(Config config) {

		return ((exchange, chain) -> {
			if (validator.isSecured.test(exchange.getRequest())) {
				// header contains token or not
				try {
					if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
						return handleUnauthorized(exchange, "missing authorization header...!");

					}

					String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
					if (authHeader != null && authHeader.startsWith("Bearer ")) {
						authHeader = authHeader.substring(7);
					}

					jwtUtil.validateToken(authHeader);

					request = exchange.getRequest().mutate()
							.header("username", jwtUtil.getUserNameFromJwtToken(authHeader)).build();

				} catch (Exception e) {
					return handleUnauthorized(exchange, e.getMessage());
				}
			}
			return chain.filter(exchange.mutate().request(request).build());
		});
	}

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

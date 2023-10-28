package com.hti.smpp.apigateway.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

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
				if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
					System.out.println("missing authorization header...!");
					throw new RuntimeException("missing authorization header");

				}

				String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
				if (authHeader != null && authHeader.startsWith("Bearer ")) {
					authHeader = authHeader.substring(7);
				}
				try {
					jwtUtil.validateToken(authHeader);

					request = exchange.getRequest().mutate()
							.header("username", jwtUtil.getUserNameFromJwtToken(authHeader)).build();

				} catch (Exception e) {
					System.out.println("unauthorized access to application...!");
					throw new RuntimeException("un authorized access to application");
				}
			}
			return chain.filter(exchange.mutate().request(request).build());
		});
	}

	public static class Config {

	}
}
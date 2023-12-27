package com.hti.smpp.apigateway.config;

import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hti.smpp.apigateway.jwt.AuthenticationFilter;

@Configuration
@ServletComponentScan
public class SpringCloudConfig {

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("LOGIN-SERVICE",
						r -> r.path("/login/**")
								.filters(f -> f.rewritePath("./login/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://LOGIN-SERVICE:8082"))
				.build();
	}
}

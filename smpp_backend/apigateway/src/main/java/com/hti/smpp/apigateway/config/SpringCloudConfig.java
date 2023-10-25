package com.hti.smpp.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringCloudConfig {

	@Bean
	public RouteLocator gatewayRoutes(RouteLocatorBuilder routeLocatorBuilder) {
		return routeLocatorBuilder.routes()
				.route("personal-service",
						rt -> rt.path("/personal/**").filters(f -> f.rewritePath("/personal(.*)", "/$1"))
								.uri("lb://personal-service:8081/"))
				.route("TEMPLATE-SERVICE",
						rt -> rt.path("/template/**").filters(f -> f.rewritePath("/template(.*)", "/$1"))
								.uri("lb://TEMPLATE-SERVICE:8081/"))
				.route("front-end", rt -> rt.path("/**").uri("http://localhost:8100/")).build();

	}

}

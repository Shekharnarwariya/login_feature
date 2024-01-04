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

	// Define a custom RouteLocator bean for configuring routes
	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes()

				// Route for TEMPLATE-SERVICE
				.route("TEMPLATE-SERVICE",
						r -> r.path("/templates/**")
								.filters(f -> f.rewritePath("./templates/(?<segment>.*)", "/${segment}")
										// Apply authentication filter
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://TEMPLATE-SERVICE:8081"))

				// Route for LOGIN-SERVICE
				.route("LOGIN-SERVICE", r -> r.path("/login/**") // Path pattern for the route
						.filters(f -> f.rewritePath("./login/(?<segment>.*)", "/${segment}") // Rewrite the path
								.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
						.uri("lb://LOGIN-SERVICE:8082")) // Target URI for the route

				// Route for SMS-SERVICE
				.route("SMS-SERVICE",
						r -> r.path("/sms/**")
								.filters(f -> f.rewritePath("./sms/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://SMS-SERVICE:8083"))

				// Route for HLR-SMSC-SERVICE
				.route("HLR-SMSC-SERVICE",
						r -> r.path("/hlr/**")
								.filters(f -> f.rewritePath("/hlr/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://HLR-SMSC-SERVICE:8084"))

				// Route for SMSC-SERVICE
				.route("SMSC-SERVICE",
						r -> r.path("/smsc/**")
								.filters(f -> f.rewritePath("/smsc/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://SMSC-SERVICE:8085"))

				// Route for ROUTE-SERVICE
				.route("ROUTE-SERVICE",
						r -> r.path("/routes/**")
								.filters(f -> f.rewritePath("/routes/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://ROUTE-SERVICE:8086"))

				// Route for SALES-SERVICE
				.route("SALES-SERVICE",
						r -> r.path("/sales/**")
								.filters(f -> f.rewritePath("/sales/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://SALES-SERVICE:8087"))

				// Route for ADDRESS-BOOK-SERVICE
				.route("ADDRESS-BOOK-SERVICE",
						r -> r.path("/addressbook/**")
								.filters(f -> f.rewritePath("./addressbook/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://ADDRESS-BOOK-SERVICE:8088"))

				// Route for BSFM-SERVICE
				.route("BSFM-SERVICE",
						r -> r.path("/bsfm/**")
								.filters(f -> f.rewritePath("./bsfm/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://BSFM-SERVICE:8089"))

				// Route for BSFM-SERVICE
				.route("TWO-WAY-SERVICE",
						r -> r.path("/twoway/**")
								.filters(f -> f.rewritePath("/twoway/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://TWO-WAY-SERVICE:8090"))

				// Route for BSFM-SERVICE
				.route("SUBSCRIPTION-SERVICE",
						r -> r.path("/subscription/**")
								.filters(f -> f.rewritePath("/subscription/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://SUBSCRIPTION-SERVICE:8090"))

				.build(); // Build the routes
	}
}

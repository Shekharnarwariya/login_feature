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
				.route("template-service",
						r -> r.path("/templates/**")
								.filters(f -> f.rewritePath("./templates/(?<segment>.*)", "/${segment}")
										// Apply authentication filter
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://template-service:8081"))

				// Route for LOGIN-SERVICE
				.route("login-service", r -> r.path("/login/**") // Path pattern for the route
						.filters(f -> f.rewritePath("./login/(?<segment>.*)", "/${segment}") // Rewrite the path
								.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
						.uri("lb://login-service:8082")) // Target URI for the route

				// Route for SMS-SERVICE
				.route("sms-service",
						r -> r.path("/sms/**")
								.filters(f -> f.rewritePath("./sms/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://sms-service:8083"))

				// Route for HLR-SMSC-SERVICE
				.route("hlrsmsc-service",
						r -> r.path("/hlr/**")
								.filters(f -> f.rewritePath("/hlr/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://hlrsmsc-service:8084"))

				// Route for SMSC-SERVICE
				.route("smsc-service",
						r -> r.path("/smsc/**")
								.filters(f -> f.rewritePath("/smsc/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://smsc-service:8085"))

				// Route for ROUTE-SERVICE
				.route("route-service",
						r -> r.path("/routes/**")
								.filters(f -> f.rewritePath("/routes/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://route-service:8086"))

				// Route for SALES-SERVICE
				.route("sales-service",
						r -> r.path("/sales/**")
								.filters(f -> f.rewritePath("./sales/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://sales-service:8087"))

				// Route for ADDRESS-BOOK-SERVICE
				.route("addressbook-service",
						r -> r.path("/addressbook/**")
								.filters(f -> f.rewritePath("./addressbook/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://addressbook-service:8088"))

				// Route for BSFM-SERVICE
				.route("bsfm-service",
						r -> r.path("/bsfm/**")
								.filters(f -> f.rewritePath("./bsfm/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://bsfm-service:8089"))

				// Route for TWOWAY-SERVICE
				.route("twoway-service",
						r -> r.path("/twoway/**")
								.filters(f -> f.rewritePath("./twoway/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://twoway-service:8090"))

				// Route for SUBSCRIPTION-SERVICE
				.route("subscription-service",
						r -> r.path("/subscription/**")
								.filters(f -> f.rewritePath("./subscription/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://subscription-service:8091"))

				// Route for DLT-SERVICE
				.route("dlt-service",
						r -> r.path("/dlt/**")
								.filters(f -> f.rewritePath("./dlt/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://dlt-service:8092"))

				// Route for NETWORK-SERVICE
				.route("network-service",
						r -> r.path("/network/**")
								.filters(f -> f.rewritePath("./network/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://network-service:8093"))

				// Route for REPORT_SERVICE
				.route("report-service",
						r -> r.path("/reports/**")
								.filters(f -> f.rewritePath("./reports/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://report-service:8094"))

				// Route for REPORT_SERVICE
				.route("downloads-service",
						r -> r.path("/download/**")
								.filters(f -> f.rewritePath("./download/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://downloads-service:8095"))

				// Route for REPORT_SERVICE
				.route("alert-service",
						r -> r.path("/alerts/**")
								.filters(f -> f.rewritePath("./alerts/(?<segment>.*)", "/${segment}")
										.filter(new AuthenticationFilter().apply(new AuthenticationFilter.Config())))
								.uri("lb://alert-service:8096"))
				.build(); // Build the routes
	}
}

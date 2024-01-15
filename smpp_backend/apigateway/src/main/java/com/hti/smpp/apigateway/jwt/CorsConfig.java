package com.hti.smpp.apigateway.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {
	 // Bean method to configure CorsWebFilter
	@Bean
	public CorsWebFilter corsWebFilter() {
		 // Create a CorsConfiguration instance
		CorsConfiguration corsConfig = new CorsConfiguration();
		 // Allow requests from "http://localhost:3000"
		corsConfig.addAllowedOrigin("http://localhost:3000");
		corsConfig.addAllowedOrigin("https://smpp.vercel.app");
		corsConfig.addAllowedOrigin("http://122.168.122.77:9090");
		// Allow all HTTP methods (GET, POST, PUT, DELETE, etc.)
		corsConfig.addAllowedMethod("*");
		// Allow all headers
		corsConfig.addAllowedHeader("*");
		// Create a UrlBasedCorsConfigurationSource
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		 // Register the CorsConfiguration for all paths (/**)
		source.registerCorsConfiguration("/**", corsConfig);
		 // Create and return a CorsWebFilter with the configured source
		return new CorsWebFilter(source);
	}
}

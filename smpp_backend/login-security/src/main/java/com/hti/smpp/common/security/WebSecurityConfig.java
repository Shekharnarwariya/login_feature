package com.hti.smpp.common.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.hti.smpp.common.security.jwt.AuthEntryPointJwt;
import com.hti.smpp.common.security.jwt.AuthTokenFilter;
import com.hti.smpp.common.security.services.UserDetailsServiceImpl;

@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Configuration
public class WebSecurityConfig {
	@Autowired
	UserDetailsServiceImpl userDetailsService;

	@Autowired
	private AuthEntryPointJwt unauthorizedHandler;
	
	@Autowired
	private AuthTokenFilter jwtAuthFilter;

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	private final String[] PUBLIC_URL = { "/swagger-ui/**", "/webjars/**", "/swagger-resources/**", "/v3/api-docs/**",
			"/auth/login" ,"/auth/signup"};

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.cors(Customizer.withDefaults()).csrf((csrf) -> csrf.disable())
				.authorizeHttpRequests(
						(auth) -> auth.requestMatchers(PUBLIC_URL).permitAll().anyRequest().authenticated())
				.exceptionHandling((e) -> e.authenticationEntryPoint(this.unauthorizedHandler))
				.sessionManagement((s) -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http.authenticationProvider(authenticationProvider());
		http.addFilterBefore(this.jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
		DefaultSecurityFilterChain defaultSecurityFilterChain = http.build();

		return defaultSecurityFilterChain;

	}

	@Bean
	public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(this.userDetailsService);
		daoAuthenticationProvider.setPasswordEncoder(bCryptPasswordEncoder());
		return daoAuthenticationProvider;
	}

}

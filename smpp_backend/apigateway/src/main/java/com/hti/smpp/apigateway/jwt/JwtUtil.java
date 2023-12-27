package com.hti.smpp.apigateway.jwt;

import java.security.Key;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	// Secret key used for signing and validating JWTs
	
	public static final String SECRET = "======================BezKoder=Spring===========================";
    // Method to validate a JWT token
	public void validateToken(final String token) {
		Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
	}
	// Use Jwts parserBuilder to parse and validate the claims of the token
	 // Method to retrieve the signing key for JWT
	private Key getSignKey() {
		byte[] keyBytes = Decoders.BASE64.decode(SECRET);
		return Keys.hmacShaKeyFor(keyBytes);
	}
	  // Decode the Base64 encoded secret key and create a signing key
	 // Method to extract the username from a JWT token
	// Use Jwts parserBuilder to parse the claims and get the subject (username) from the token
	public String getUserNameFromJwtToken(String token) {
		return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody().getSubject();
	}
	  // Method to get the signing key
	// Decode the Base64 encoded secret key and create a signing key
	private Key key() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
	}

}
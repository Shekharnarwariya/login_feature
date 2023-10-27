package com.hti.smpp.apigateway.excepation;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApplicationExceptionHandler

{

	@ExceptionHandler(UNAuthorizationExcepation.class)
	public ResponseEntity<?> nullValueException(UNAuthorizationExcepation e) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<ExceptionResponse>(new ExceptionResponse(e.getMessage(), UTC(current)),
				HttpStatus.NOT_FOUND);

	}

	public static LocalDateTime UTC(LocalDateTime date) {
		ZonedDateTime ldtZoned = date.atZone(ZoneId.systemDefault());

		ZonedDateTime utcZoned = ldtZoned.withZoneSameInstant(ZoneId.of("UTC"));
		return utcZoned.toLocalDateTime();
	}

}
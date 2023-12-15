package com.hti.smpp.common.exception;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApplicationExceptionHandler {

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ExceptionResponse> handleUnauthorizedException(UnauthorizedException exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase()), HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase()), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(InvalidOtpException.class)
	public ResponseEntity<?> InvalidOtpException(InvalidOtpException exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.GATEWAY_TIMEOUT.value(), HttpStatus.GATEWAY_TIMEOUT.getReasonPhrase()),
				HttpStatus.GATEWAY_TIMEOUT);

	}

	@ExceptionHandler(InvalidPasswordException.class)
	public ResponseEntity<?> InvalidPasswordException(InvalidPasswordException exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.BAD_GATEWAY.value(), HttpStatus.BAD_GATEWAY.getReasonPhrase()), HttpStatus.BAD_GATEWAY);

	}

	@ExceptionHandler(AuthenticationExceptionFailed.class)
	public ResponseEntity<ExceptionResponse> handleAuthenticationExceptionFailed(
			AuthenticationExceptionFailed exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.BAD_GATEWAY.value(), HttpStatus.BAD_GATEWAY.getReasonPhrase()), HttpStatus.BAD_GATEWAY);

	}

	@ExceptionHandler(InternalServerException.class)
	public ResponseEntity<ExceptionResponse> InternalServerException(InternalServerException exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.BAD_GATEWAY.value(), HttpStatus.BAD_GATEWAY.getReasonPhrase()), HttpStatus.BAD_GATEWAY);

	}

	@ExceptionHandler(DataAccessError.class)
	public ResponseEntity<ExceptionResponse> handleDataAccessException(DataAccessError exception) {
		String exceptionName = exception.getClass().getSimpleName(); // Getting the exception name
		String statusMessage = "Internal Server Error - Data Access Exception"; // Default status message

		if (exceptionName.equals("SomeOtherDataAccessException")) {
			statusMessage = "Some other data access exception occurred";
		} else if (exceptionName.equals("AnotherDataAccessException")) {
			statusMessage = "Another data access exception occurred";
		}
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.INTERNAL_SERVER_ERROR.value(), statusMessage), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(JsonProcessingError.class)
	public ResponseEntity<ExceptionResponse> JsonProcessingError(JsonProcessingError exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()), HttpStatus.INTERNAL_SERVER_ERROR);

	}
	
	@ExceptionHandler(WorkBookException.class)
	public ResponseEntity<ExceptionResponse> WorkBookException(WorkBookException exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()), HttpStatus.INTERNAL_SERVER_ERROR);

	}

	private LocalDateTime toUtc(LocalDateTime current) {
		return current.atOffset(ZoneOffset.UTC).toLocalDateTime();
	}
}

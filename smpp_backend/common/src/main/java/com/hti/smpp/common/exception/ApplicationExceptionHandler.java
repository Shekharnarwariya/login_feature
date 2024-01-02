package com.hti.smpp.common.exception;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
/**
 * Global exception handler for the Spring application. Catches various exceptions
 * and provides appropriate responses.
 */

@RestControllerAdvice
public class ApplicationExceptionHandler {
	 /**
     * Handles UnauthorizedException, returns a response with status UNAUTHORIZED.
     */
	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ExceptionResponse> handleUnauthorizedException(UnauthorizedException exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase()), HttpStatus.UNAUTHORIZED);
	}
	 /**
     * Handles NotFoundException, returns a response with status NOT_FOUND.
     */
	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase()), HttpStatus.NOT_FOUND);
	}
	 /**
     * Handles InvalidOtpException, returns a response with status GATEWAY_TIMEOUT.
     */
	@ExceptionHandler(InvalidOtpException.class)
	public ResponseEntity<?> InvalidOtpException(InvalidOtpException exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.GATEWAY_TIMEOUT.value(), HttpStatus.GATEWAY_TIMEOUT.getReasonPhrase()),
				HttpStatus.GATEWAY_TIMEOUT);

	}
	/**
     * Handles InvalidPasswordException, returns a response with status BAD_GATEWAY.
     */
	@ExceptionHandler(InvalidPasswordException.class)
	public ResponseEntity<?> InvalidPasswordException(InvalidPasswordException exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.BAD_GATEWAY.value(), HttpStatus.BAD_GATEWAY.getReasonPhrase()), HttpStatus.BAD_GATEWAY);

	}
	 /**
     * Handles AuthenticationExceptionFailed, returns a response with status BAD_GATEWAY.
     */
	@ExceptionHandler(AuthenticationExceptionFailed.class)
	public ResponseEntity<ExceptionResponse> handleAuthenticationExceptionFailed(
			AuthenticationExceptionFailed exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.BAD_GATEWAY.value(), HttpStatus.BAD_GATEWAY.getReasonPhrase()), HttpStatus.BAD_GATEWAY);

	}
	/**
     * Handles InternalServerException, returns a response with status INTERNAL_SERVER_ERROR.
     */
	@ExceptionHandler(InternalServerException.class)
	public ResponseEntity<ExceptionResponse> InternalServerException(InternalServerException exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()),
				HttpStatus.INTERNAL_SERVER_ERROR);

	}
	/**
     * Handles InsufficientBalanceException, returns a response with status BAD_REQUEST.
     */
	@ExceptionHandler(InsufficientBalanceException.class)
	public ResponseEntity<ExceptionResponse> InsufficientBalanceException(InsufficientBalanceException exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase()), HttpStatus.BAD_REQUEST);

	}

    /**
     * Handles ScheduledTimeException, returns a response with status BAD_REQUEST.
     */
	@ExceptionHandler(ScheduledTimeException.class)
	public ResponseEntity<ExceptionResponse> ScheduledTimeException(ScheduledTimeException exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase()), HttpStatus.BAD_REQUEST);

	}
	  /**
     * Handles validation errors for method arguments, returns a map of field errors.
     */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Map<String, String> handleInvalidArgument(MethodArgumentNotValidException ex) {
		Map<String, String> errormap = new HashMap<>();

		ex.getBindingResult().getFieldErrors().forEach(error -> {
			errormap.put(error.getField(), error.getDefaultMessage());
		});

		return errormap;

	}
	/**
     * Handles AccessDataException, returns a response with status BAD_GATEWAY.
     */
	@ExceptionHandler(AccessDataException.class)
	public ResponseEntity<ExceptionResponse> AccessDataException(AccessDataException exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.BAD_GATEWAY.value(), HttpStatus.BAD_GATEWAY.getReasonPhrase()), HttpStatus.BAD_GATEWAY);

	}
	/**
     * Handles JasperReportException, returns a response with status BAD_REQUEST.
     */
	@ExceptionHandler(JasperReportException.class)
	public ResponseEntity<ExceptionResponse> JasperReportException(JasperReportException exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase()), HttpStatus.BAD_REQUEST);

	}
	
	/**
     * Handles data access errors generically, providing a custom status message.
     */
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
	 /**
     * Handles JSON processing errors, returns a response with status INTERNAL_SERVER_ERROR.
     */
	@ExceptionHandler(JsonProcessingError.class)
	public ResponseEntity<ExceptionResponse> JsonProcessingError(JsonProcessingError exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()), HttpStatus.INTERNAL_SERVER_ERROR);

	}
	 /**
     * Handles workbook-related exceptions, returns a response with status INTERNAL_SERVER_ERROR.
     */
	@ExceptionHandler(WorkBookException.class)
	public ResponseEntity<ExceptionResponse> WorkBookException(WorkBookException exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()), HttpStatus.INTERNAL_SERVER_ERROR);

	}
	
	@ExceptionHandler(NumberFormatError.class)
	public ResponseEntity<ExceptionResponse> NumberFormatException(NumberFormatError exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase()), HttpStatus.BAD_REQUEST);

	}
	
	/**
     * Converts the current local time to UTC.
     */
	private LocalDateTime toUtc(LocalDateTime current) {
		return current.atOffset(ZoneOffset.UTC).toLocalDateTime();
	}
}

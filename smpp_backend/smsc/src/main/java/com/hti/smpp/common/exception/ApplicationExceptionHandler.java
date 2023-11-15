package com.hti.smpp.common.exception;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hti.smpp.common.response.ExceptionResponse;

@RestControllerAdvice
public class ApplicationExceptionHandler {

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Map<String, String> handleInvalidArgument(MethodArgumentNotValidException exception) {
		Map<String, String> errorMap = new HashMap<>();

		exception.getBindingResult().getFieldErrors().forEach((error) -> {
			errorMap.put(error.getField(), error.getDefaultMessage());
		});

		return errorMap;
	}

	@ExceptionHandler(SmscNotFoundException.class)
	public ResponseEntity<ExceptionResponse> handleNotFoundException(SmscNotFoundException exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase()), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(SmscInternalServerException.class)
	public ResponseEntity<ExceptionResponse> handleInternalServerException(SmscInternalServerException exception) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(SmscDataAccessException.class)
	public ResponseEntity<ExceptionResponse> handleDataAccessException(SmscDataAccessException exception) {
		String exceptionName = exception.getClass().getSimpleName(); // Getting the exception name
		String statusMessage = "Internal Server Error - Data Access Exception"; // Default status message

		if (exceptionName.equals("SomeOtherDataAccessException")) {
			statusMessage = "Some other data access exception occurred";
		} else if (exceptionName.equals("AnotherDataAccessException")) {
			statusMessage = "Another data access exception occurred";
		}
		// Add more cases as necessary for different exception types

		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<>(new ExceptionResponse(exception.getMessage(), toUtc(current),
				HttpStatus.INTERNAL_SERVER_ERROR.value(), statusMessage), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private LocalDateTime toUtc(LocalDateTime current) {
		return current.atOffset(ZoneOffset.UTC).toLocalDateTime();
	}
}

package com.hti.smpp.common.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hti.smpp.common.payload.response.ExceptionResponse;
import com.hti.smpp.common.util.TimeConverter;

@RestControllerAdvice
public class ApplicationExceptionHandler

{

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Map<String, String> handleInvalidArgument(MethodArgumentNotValidException ex) {
		Map<String, String> errormap = new HashMap<>();

		ex.getBindingResult().getFieldErrors().forEach(error -> {
			errormap.put(error.getField(), error.getDefaultMessage());
		});

		return errormap;

	}

	@ExceptionHandler(NullValueException.class)
	public ResponseEntity<?> nullValueException(NullValueException e) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<ExceptionResponse>(new ExceptionResponse(e.getMessage(), TimeConverter.UTC(current)),
				HttpStatus.NOT_FOUND);

	}

	@ExceptionHandler(InvalidOtpException.class)
	public ResponseEntity<?> InvalidOtpException(InvalidOtpException e) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<ExceptionResponse>(new ExceptionResponse(e.getMessage(), TimeConverter.UTC(current)),
				HttpStatus.GATEWAY_TIMEOUT);

	}

	@ExceptionHandler(InvalidPasswordException.class)
	public ResponseEntity<?> InvalidPasswordException(InvalidPasswordException e) {
		LocalDateTime current = LocalDateTime.now();
		return new ResponseEntity<ExceptionResponse>(new ExceptionResponse(e.getMessage(), TimeConverter.UTC(current)),
				HttpStatus.BAD_GATEWAY);

	}

}
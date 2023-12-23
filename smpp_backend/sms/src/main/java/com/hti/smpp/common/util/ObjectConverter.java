package com.hti.smpp.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hti.smpp.common.exception.JsonProcessingError;
import com.hti.smpp.common.request.BulkRequest;

public class ObjectConverter {

	public static BulkRequest jsonMapper(String request) {

		BulkRequest bulkRequest = null;
		try {
			bulkRequest = new ObjectMapper().readValue(request, BulkRequest.class);
		} catch (Exception e) {
			throw new JsonProcessingError("error:getting error in json parrsing " + e.getMessage());
		}
		return bulkRequest;
	}
}

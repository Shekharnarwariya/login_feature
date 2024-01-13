package com.hti.smpp.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hti.smpp.common.exception.JsonProcessingError;
import com.hti.smpp.common.request.BulkRequest;
import com.hti.smpp.common.request.BulkMmsRequest;

public class ObjectConverter {

	public static BulkRequest jsonMapperBulkRequest(String request) {

		BulkRequest bulkRequest = null;
		try {
			bulkRequest = new ObjectMapper().readValue(request, BulkRequest.class);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JsonProcessingError("error:getting error in json parrsing " + e.getMessage());
		}
		return bulkRequest;
	}

	public static BulkMmsRequest jsonMapperBulkMmsRequest(String request) {

		BulkMmsRequest bulkMmsRequest = null;
		try {
			bulkMmsRequest = new ObjectMapper().readValue(request, BulkMmsRequest.class);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JsonProcessingError("error:getting error in json parrsing " + e.getMessage());
		}
		return bulkMmsRequest;
	}

}

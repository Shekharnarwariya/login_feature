package com.hti.smpp.common.service;

import org.springframework.http.ResponseEntity;

import com.hti.smpp.common.request.SubscribeEntryForm;

public interface SubscribeService {

	public ResponseEntity<?> saveSubscribe(SubscribeEntryForm subscribeEntryForm, String username);

}

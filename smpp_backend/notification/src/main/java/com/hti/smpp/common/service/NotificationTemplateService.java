package com.hti.smpp.common.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.NotificationTemplateRequest;

@Service
public interface NotificationTemplateService {

	public ResponseEntity<?> getAllNotificationTemplate();

	public ResponseEntity<?> getNotificationTemplateByIdAndType(Long id, String eventType);

	public ResponseEntity<?> saveEvent(NotificationTemplateRequest notificationTemplateRequest);

	public ResponseEntity<?> updateEvent(Long id, NotificationTemplateRequest notificationTemplateRequest);

	public ResponseEntity<?> deleteEvent(Long id);

	public ResponseEntity<?> sendNotificationEmail(String email, Long eventId);
}

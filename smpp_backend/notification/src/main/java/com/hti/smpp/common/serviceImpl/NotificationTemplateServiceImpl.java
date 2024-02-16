package com.hti.smpp.common.serviceImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.entity.NotificationTemplate;
import com.hti.smpp.common.repository.NotificationTemplateRepository;
import com.hti.smpp.common.request.NotificationTemplateRequest;
import com.hti.smpp.common.service.NotificationTemplateService;

@Service
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

	@Autowired
	private NotificationTemplateRepository notificationTemplateRepository;

	@Override
	public ResponseEntity<?> getAllNotificationTemplate() {
		List<NotificationTemplate> templates = notificationTemplateRepository.findAll();
		return ResponseEntity.ok(templates);
	}

	@Override
	public ResponseEntity<?> getNotificationTemplateByIdAndType(Long id, String eventType) {
		Optional<NotificationTemplate> template = notificationTemplateRepository.findByIdAndTemplateType(id, eventType);
		return template.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<?> saveEvent(NotificationTemplateRequest notificationTemplateRequest) {
		NotificationTemplate template = new NotificationTemplate();
		template.setAdditionalSetting(notificationTemplateRequest.getAdditionalSetting());
		template.setEventName(notificationTemplateRequest.getEventName());
		template.setSubject(notificationTemplateRequest.getSubject());
		template.setTemplateContext(notificationTemplateRequest.getTemplateContext());
		template.setTemplateType(notificationTemplateRequest.getTemplateType());
		NotificationTemplate savedTemplate = notificationTemplateRepository.save(template);
		return new ResponseEntity<>(savedTemplate, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<?> updateEvent(Long id, NotificationTemplateRequest notificationTemplateRequest) {
		return notificationTemplateRepository.findById(id).map(template -> {
			template.setAdditionalSetting(notificationTemplateRequest.getAdditionalSetting());
			template.setEventName(notificationTemplateRequest.getEventName());
			template.setSubject(notificationTemplateRequest.getSubject());
			template.setTemplateContext(notificationTemplateRequest.getTemplateContext());
			template.setTemplateType(notificationTemplateRequest.getTemplateType());
			NotificationTemplate updatedTemplate = notificationTemplateRepository.save(template);
			return ResponseEntity.ok(updatedTemplate);
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@Override
	public ResponseEntity<?> deleteEvent(Long id) {
		if (notificationTemplateRepository.existsById(id)) {
			notificationTemplateRepository.deleteById(id);
			return ResponseEntity.ok().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@Override
	public ResponseEntity<?> sendNotificationEmail(String email, Long eventId) {
		return ResponseEntity.ok().build();
	}

}

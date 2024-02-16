package com.hti.smpp.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hti.smpp.common.request.NotificationTemplateRequest;
import com.hti.smpp.common.service.NotificationTemplateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/notifications")
public class NotificationTemplateController {

    @Autowired
    private NotificationTemplateService notificationTemplateService;

    @Operation(summary = "Get all notification templates", description = "Returns a list of all notification templates")
    @GetMapping("/templates/all")
    public ResponseEntity<?> getAllNotificationTemplates() {
        return notificationTemplateService.getAllNotificationTemplate();
    }

    @Operation(summary = "Get notification template by ID and type", description = "Returns a single notification template matching the ID and type")
    @GetMapping("/templates/{id}")
    public ResponseEntity<?> getNotificationTemplateByIdAndType(
            @Parameter(description = "ID of the notification template to be retrieved") @PathVariable Long id,
            @Parameter(description = "Type of the notification event") @RequestParam String eventType) {
        return notificationTemplateService.getNotificationTemplateByIdAndType(id, eventType);
    }

    @Operation(summary = "Create a new notification template", description = "Creates a new notification template and returns it")
    @PostMapping("/templates")
    public ResponseEntity<?> createNotificationTemplate(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Notification template request", required = true, content = @Content(schema = @Schema(implementation = NotificationTemplateRequest.class))) @RequestBody NotificationTemplateRequest notificationTemplateRequest) {
        return notificationTemplateService.saveEvent(notificationTemplateRequest);
    }

    @Operation(summary = "Update a notification template", description = "Updates an existing notification template by its ID")
    @PutMapping("/templates/{id}")
    public ResponseEntity<?> updateNotificationTemplate(
            @Parameter(description = "ID of the notification template to be updated") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated notification template request", required = true, content = @Content(schema = @Schema(implementation = NotificationTemplateRequest.class))) @RequestBody NotificationTemplateRequest notificationTemplateRequest) {
        return notificationTemplateService.updateEvent(id, notificationTemplateRequest);
    }

    @Operation(summary = "Delete a notification template", description = "Deletes a notification template by its ID")
    @DeleteMapping("/templates/{id}")
    public ResponseEntity<?> deleteNotificationTemplate(
            @Parameter(description = "ID of the notification template to be deleted") @PathVariable Long id) {
        return notificationTemplateService.deleteEvent(id);
    }

    @Operation(summary = "Send notification email", description = "Sends a notification email for a specific event")
    @GetMapping("/send-email")
    public ResponseEntity<?> sendNotificationEmail(
            @Parameter(description = "Email address to send the notification to") @RequestParam String email,
            @Parameter(description = "ID of the event for the notification") @RequestParam Long eventId) {
        return notificationTemplateService.sendNotificationEmail(email, eventId);
    }
}

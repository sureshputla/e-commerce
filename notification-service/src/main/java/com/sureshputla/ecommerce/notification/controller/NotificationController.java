package com.sureshputla.ecommerce.notification.controller;

import com.sureshputla.ecommerce.notification.model.Notification;
import com.sureshputla.ecommerce.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "View notification history")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get all notifications for the current user")
    public List<Notification> getNotifications() {
        return notificationService.getNotificationsForUser("user1");
    }
}


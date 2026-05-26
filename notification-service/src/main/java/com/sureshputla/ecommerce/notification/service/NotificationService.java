package com.sureshputla.ecommerce.notification.service;

import com.sureshputla.ecommerce.common.event.PaymentProcessedEvent;
import com.sureshputla.ecommerce.notification.model.Notification;
import com.sureshputla.ecommerce.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        String type = event.isSuccess() ? "PAYMENT_SUCCESS" : "PAYMENT_FAILED";

        Notification notification = Notification.builder()
                .id(UUID.randomUUID().toString())
                .userId(event.getUserId())
                .orderId(event.getOrderId())
                .paymentId(event.getPaymentId())
                .type(type)
                .message(event.getMessage())
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        // In production, replace with email/SMS integration
        if (event.isSuccess()) {
            log.info("📧 [NOTIFICATION] ORDER CONFIRMED – userId={} orderId={} amount=₹{} | {}",
                    event.getUserId(), event.getOrderId(), event.getAmount(), event.getMessage());
        } else {
            log.warn("📧 [NOTIFICATION] ORDER CANCELLED – userId={} orderId={} | {}",
                    event.getUserId(), event.getOrderId(), event.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForUser(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}


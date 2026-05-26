package com.sureshputla.ecommerce.payment.controller;

import com.sureshputla.ecommerce.payment.dto.PaymentDto;
import com.sureshputla.ecommerce.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment records and status")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by payment ID")
    public PaymentDto getPayment(@PathVariable String paymentId) {
        return paymentService.getPayment(paymentId);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment by order ID")
    public PaymentDto getPaymentByOrder(@PathVariable String orderId) {
        return paymentService.getPaymentByOrder(orderId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(IllegalArgumentException ex) {
        return Map.of("message", ex.getMessage());
    }
}


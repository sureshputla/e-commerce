package com.sureshputla.ecommerce.common.kafka;

/**
 * Central registry of all Kafka topic names used across microservices.
 */
public final class KafkaTopics {

    private KafkaTopics() {
        // utility class
    }

    /** Published by Order Service when an order is placed. */
    public static final String ORDER_CREATED = "order.created";

    /** Published by Payment Service after processing payment. */
    public static final String PAYMENT_PROCESSED = "payment.processed";
}


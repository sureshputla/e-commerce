package com.sureshputla.ecommerce.order.model;

/**
 * Lifecycle states of an order within the Choreography Saga.
 *
 * <pre>
 *  PENDING  ─────► CONFIRMED  (payment succeeded)
 *           └────► CANCELLED  (payment failed – compensating transaction)
 * </pre>
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    CANCELLED
}


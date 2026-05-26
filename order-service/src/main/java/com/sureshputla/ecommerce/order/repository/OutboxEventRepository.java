package com.sureshputla.ecommerce.order.repository;

import com.sureshputla.ecommerce.order.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    /** Find all events that have not yet been published to Kafka. */
    List<OutboxEvent> findByStatus(OutboxEvent.OutboxStatus status);
}


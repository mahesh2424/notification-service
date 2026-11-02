package com.mahesh.flashsale.notification;

import com.mahesh.flashsale.messaging.ReservationDomainEvent;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(name = "ticketing.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class ReservationNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(ReservationNotificationListener.class);

    private final ObjectMapper objectMapper;
    private final ProcessedMessageRepository processedMessageRepository;

    public ReservationNotificationListener(
            ObjectMapper objectMapper,
            ProcessedMessageRepository processedMessageRepository) {
        this.objectMapper = objectMapper;
        this.processedMessageRepository = processedMessageRepository;
    }

    @KafkaListener(
            topics = "${ticketing.kafka.topic:booking.reservation-events}",
            groupId = "notification-service")
    public void onReservationEvent(String payload) {
        ReservationDomainEvent event = objectMapper.readValue(payload, ReservationDomainEvent.class);
        if (event.messageId() == null) {
            throw new IllegalArgumentException("Reservation event is missing messageId");
        }
        boolean firstTime = processedMessageRepository.markProcessed(event.messageId(), Instant.now());
        if (!firstTime) {
            log.info("Skipping duplicate {} for reservation {}", event.type(), event.reservationId());
            return;
        }
        log.info(
                "Notify {} that reservation {} is {} (quantity={}, remaining={})",
                event.customerEmail(),
                event.reservationId(),
                event.type(),
                event.quantity(),
                event.availableTickets());
    }
}

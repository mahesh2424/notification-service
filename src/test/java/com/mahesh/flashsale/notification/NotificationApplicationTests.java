package com.mahesh.flashsale.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.mahesh.flashsale.messaging.ReservationDomainEvent;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
class NotificationApplicationTests {

    @Autowired
    ProcessedMessageRepository processedMessageRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void contextLoadsAndDeduplicatesMessages() {
        UUID messageId = UUID.randomUUID();
        ReservationDomainEvent event = new ReservationDomainEvent(
                messageId,
                "ReservationHeld",
                UUID.randomUUID(),
                UUID.randomUUID(),
                "fan@example.com",
                2,
                8,
                Instant.parse("2026-01-01T12:00:00Z"));
        objectMapper.writeValueAsString(event);

        assertThat(processedMessageRepository.markProcessed(messageId, Instant.now())).isTrue();
        assertThat(processedMessageRepository.markProcessed(messageId, Instant.now())).isFalse();
    }
}

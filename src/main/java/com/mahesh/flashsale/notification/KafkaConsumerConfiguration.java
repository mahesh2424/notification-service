package com.mahesh.flashsale.notification;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@ConditionalOnProperty(name = "ticketing.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConsumerConfiguration {

    @Bean
    NewTopic reservationEventsDlq(
            @Value("${ticketing.kafka.dlq-topic:booking.reservation-events.dlq}") String dlqTopic) {
        return TopicBuilder.name(dlqTopic).partitions(3).replicas(1).build();
    }

    @Bean
    DefaultErrorHandler kafkaErrorHandler(
            KafkaOperations<String, String> template,
            @Value("${ticketing.kafka.dlq-topic:booking.reservation-events.dlq}") String dlqTopic) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                template,
                (record, exception) -> new org.apache.kafka.common.TopicPartition(dlqTopic, record.partition()));
        return new DefaultErrorHandler(recoverer, new FixedBackOff(500L, 3L));
    }
}

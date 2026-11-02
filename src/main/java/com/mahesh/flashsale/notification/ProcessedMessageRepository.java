package com.mahesh.flashsale.notification;

import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProcessedMessageRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ProcessedMessageRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean markProcessed(UUID eventId, Instant processedAt) {
        int inserted = jdbcTemplate.update(
                """
                INSERT INTO processed_messages (event_id, processed_at)
                SELECT :eventId, :processedAt
                WHERE NOT EXISTS (
                    SELECT 1 FROM processed_messages WHERE event_id = :eventId
                )
                """,
                new MapSqlParameterSource()
                        .addValue("eventId", eventId)
                        .addValue("processedAt", processedAt));
        return inserted == 1;
    }
}

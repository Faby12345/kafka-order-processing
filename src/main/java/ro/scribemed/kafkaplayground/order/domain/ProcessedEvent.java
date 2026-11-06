package ro.scribemed.kafkaplayground.order.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_event")
public class ProcessedEvent {
    @Id
    private UUID eventId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)

    private Order order;

    @Column(nullable = false)
    private Instant processedAt;

    public ProcessedEvent(UUID eventId, Order order, Instant processedAt) {
        this.eventId = eventId;
        this.order = order;
        this.processedAt = processedAt;
    }

    public ProcessedEvent() {}
}

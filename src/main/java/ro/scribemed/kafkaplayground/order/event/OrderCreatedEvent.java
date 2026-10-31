package ro.scribemed.kafkaplayground.order.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        UUID orderId,
        UUID customerId,
        String product,
        int quantity,
        BigDecimal totalPrice,
        Instant createdAt
) {}
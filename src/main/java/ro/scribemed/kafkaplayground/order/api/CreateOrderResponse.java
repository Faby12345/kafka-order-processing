package ro.scribemed.kafkaplayground.order.api;

import java.util.UUID;

public record CreateOrderResponse(
        UUID orderId,
        String message
) {}
package ro.scribemed.kafkaplayground.order.api;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ro.scribemed.kafkaplayground.order.domain.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


public record CreateOrderRequest(
        @NotNull UUID customerId,

        @NotBlank String product,

        @Positive int quantity,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal totalPrice
) {

        public Order toEntity(){
                return new Order(
                        UUID.randomUUID(),
                        customerId,
                        product,
                        quantity,
                        totalPrice,
                        Instant.now()
                );
        }

}
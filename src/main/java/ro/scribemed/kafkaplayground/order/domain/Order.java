package ro.scribemed.kafkaplayground.order.domain;


import jakarta.persistence.*;
import ro.scribemed.kafkaplayground.order.api.CreateOrderResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private String product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private Instant createdAt;

    protected Order() {
        // Required by JPA.
    }

    public Order(
            UUID id,
            UUID customerId,
            String product,
            int quantity,
            BigDecimal totalPrice,
            Instant createdAt
    ) {
        this.id = id;
        this.customerId = customerId;
        this.product = product;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
    public CreateOrderResponse toResponse(){
        return new CreateOrderResponse(
                id,
                "Order created Successfully"
        );
    }
}
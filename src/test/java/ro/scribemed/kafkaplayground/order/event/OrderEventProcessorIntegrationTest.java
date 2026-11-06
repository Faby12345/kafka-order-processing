package ro.scribemed.kafkaplayground.order.event;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import ro.scribemed.kafkaplayground.order.domain.Order;
import ro.scribemed.kafkaplayground.order.repository.OrderRepository;
import ro.scribemed.kafkaplayground.order.repository.ProcessedEventRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Testcontainers
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import(OrderEventProcessor.class)
class OrderEventProcessorIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgreSQLContainer
            = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private OrderEventProcessor orderEventProcessor;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Test
    void shouldStoreOnlyOneProcessedEventWhenEventIsReceivedTwice() {
        UUID orderId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Order order = new Order(
                orderId,
                UUID.randomUUID(),
                "Laptop",
                1,
                new BigDecimal("1200.00"),
                Instant.now()
        );
        orderRepository.saveAndFlush(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                eventId,
                order.getId(),
                order.getCustomerId(),
                order.getProduct(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getCreatedAt()
        );
        orderEventProcessor.process(event);
        orderEventProcessor.process(event);

        assertThat(processedEventRepository.count()).isEqualTo(1);
        assertThat(processedEventRepository.existsById(eventId)).isTrue();
    }
    @Test
    void shouldNotStoreProcessedEventWhenOrderDoesNotExist(){
        UUID orderId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        OrderCreatedEvent event = new OrderCreatedEvent(
                eventId,
                orderId,
                UUID.randomUUID(),
                "Leptop",
                1,
                new BigDecimal("1200.00"),
                Instant.now()
        );

        assertThatThrownBy(() -> orderEventProcessor.process(event))
                .isInstanceOf(EntityNotFoundException.class);

        assertThat(processedEventRepository.existsById(eventId)).isFalse();
        assertThat(processedEventRepository.count()).isZero();
    }
}

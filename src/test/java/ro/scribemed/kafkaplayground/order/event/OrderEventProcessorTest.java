package ro.scribemed.kafkaplayground.order.event;


import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.scribemed.kafkaplayground.order.domain.Order;
import ro.scribemed.kafkaplayground.order.domain.ProcessedEvent;
import ro.scribemed.kafkaplayground.order.repository.OrderRepository;
import ro.scribemed.kafkaplayground.order.repository.ProcessedEventRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class OrderEventProcessorTest {

    private OrderRepository orderRepository;
    private ProcessedEventRepository processedEventRepository;
    private OrderEventProcessor orderEventProcessor;

    @BeforeEach
    void setup() {
        orderRepository = mock(OrderRepository.class);
        processedEventRepository = mock(ProcessedEventRepository.class);
        orderEventProcessor = new OrderEventProcessor(
                orderRepository,
                processedEventRepository
        );
    }

    @Test
    void shouldProcessTheSameEventOnlyOnce(){
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Order order = new Order(
                orderId,
                UUID.randomUUID(),
                "Leptop",
                1,
                new BigDecimal("1200.00"),
                Instant.now()
        );

        OrderCreatedEvent event = new OrderCreatedEvent(
                eventId,
                orderId,
                order.getCustomerId(),
                order.getProduct(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getCreatedAt()
        );

        when(processedEventRepository.existsById(eventId))
                .thenReturn(false, true);
        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        orderEventProcessor.process(event);
        orderEventProcessor.process(event);

        verify(processedEventRepository ,times(1))
                .save(any(ProcessedEvent.class));

        verify(orderRepository, times(1))
                .findById(orderId);
    }

    @Test
    void shouldSkipAnAlreadyProcessedEvent() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Order order = new Order(
                orderId,
                UUID.randomUUID(),
                "Leptop",
                1,
                new BigDecimal("1200.00"),
                Instant.now()
        );

        OrderCreatedEvent event = new OrderCreatedEvent(
                eventId,
                orderId,
                order.getCustomerId(),
                order.getProduct(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getCreatedAt()
        );

        when(processedEventRepository.existsById(eventId))
                .thenReturn(true);

        orderEventProcessor.process(event);

        verify(orderRepository, never())
                .findById(any());

        verify(processedEventRepository, never())
                .save(any(ProcessedEvent.class));

    }

    @Test
    void shouldNotMarkEventAsProcessedWhenOrderDoseNotExist() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();


        Order order = new Order(
                orderId,
                UUID.randomUUID(),
                "Leptop",
                1,
                new BigDecimal("1200.00"),
                Instant.now()
        );

        OrderCreatedEvent event = new OrderCreatedEvent(
                eventId,
                orderId,
                order.getCustomerId(),
                order.getProduct(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getCreatedAt()
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        when(processedEventRepository.existsById(eventId))
                .thenReturn(false);

        assertThrows(
                EntityNotFoundException.class,
                () -> orderEventProcessor.process(event)
        );




        verify(processedEventRepository, never())
                .save(any(ProcessedEvent.class));
    }

}
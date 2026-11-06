package ro.scribemed.kafkaplayground.order;

import org.springframework.stereotype.Service;
import ro.scribemed.kafkaplayground.order.api.CreateOrderRequest;
import ro.scribemed.kafkaplayground.order.api.CreateOrderResponse;
import ro.scribemed.kafkaplayground.order.domain.Order;
import ro.scribemed.kafkaplayground.order.event.OrderCreatedEvent;
import ro.scribemed.kafkaplayground.order.messaging.OrderProducer;
import ro.scribemed.kafkaplayground.order.repository.OrderRepository;
import java.time.Instant;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderProducer orderProducer;
    private final OrderRepository orderRepository;

    public OrderService(OrderProducer orderProducer, OrderRepository orderRepository) {
        this.orderProducer = orderProducer;
        this.orderRepository = orderRepository;
    }

    public CreateOrderResponse creatOrder(CreateOrderRequest request){


        Order createdOrder = orderRepository.save(request.toEntity());

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                createdOrder.getId(),
                request.customerId(),
                request.product(),
                request.quantity(),
                request.totalPrice(),
                Instant.now()
        );


        orderProducer.send(event).join();

        return createdOrder.toResponse();
    }
}

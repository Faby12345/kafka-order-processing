package ro.scribemed.kafkaplayground.order;

import org.springframework.stereotype.Service;
import ro.scribemed.kafkaplayground.order.api.CreateOrderRequest;
import ro.scribemed.kafkaplayground.order.api.CreateOrderResponse;
import ro.scribemed.kafkaplayground.order.event.OrderCreatedEvent;
import ro.scribemed.kafkaplayground.order.messaging.OrderProducer;

import java.time.Instant;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderProducer orderProducer;

    public OrderService(OrderProducer orderProducer) {
        this.orderProducer = orderProducer;
    }

    public CreateOrderResponse creatOrder(CreateOrderRequest request){
        UUID orderId = UUID.randomUUID();
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                orderId,
                request.customerId(),
                request.product(),
                request.quantity(),
                request.totalPrice(),
                Instant.now()
        );
        orderProducer.send(event).join();
        return  new CreateOrderResponse(
                orderId,
                "Order accepted for processing!"
        );
    }
}

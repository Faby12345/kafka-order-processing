package ro.scribemed.kafkaplayground.order.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ro.scribemed.kafkaplayground.order.config.KafkaTopicConfig;
import ro.scribemed.kafkaplayground.order.event.OrderCreatedEvent;

import java.util.concurrent.CompletableFuture;

@Component
public class OrderProducer {
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;


    private static final Logger log =
            LoggerFactory.getLogger(OrderConsumer.class);

    public OrderProducer(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;

    }

    public CompletableFuture<SendResult<String, OrderCreatedEvent>> send(
            OrderCreatedEvent event
    ) {
        return kafkaTemplate.send(
                KafkaTopicConfig.ORDERS_CREATED,
                event.customerId().toString(),
                event
        );
    }




}

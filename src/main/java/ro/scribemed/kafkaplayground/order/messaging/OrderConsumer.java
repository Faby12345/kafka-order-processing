package ro.scribemed.kafkaplayground.order.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.scribemed.kafkaplayground.order.config.KafkaTopicConfig;
import ro.scribemed.kafkaplayground.order.event.OrderCreatedEvent;

@Component
public class OrderConsumer {
    private static final Logger log =
            LoggerFactory.getLogger(OrderConsumer.class);



    @KafkaListener(
            topics = KafkaTopicConfig.ORDERS_CREATED
    )
    public void consume(ConsumerRecord<String, OrderCreatedEvent> record){
        OrderCreatedEvent event = record.value();

        log.info(
                "Attempting order: orderId={}, partition={}, offset={}",
                event.orderId(),
                record.partition(),
                record.offset()
        );

        if("FAIL".equalsIgnoreCase(event.product())){
            throw new IllegalStateException("Simulated order processing failure");
        }

        log.info(
                "Received order: orderId={}, customerId={}, product={}, " +
                        "quantity={}, totalPrice={}, partition={}, offset={}",
                event.orderId(),
                event.customerId(),
                event.product(),
                event.quantity(),
                event.totalPrice(),
                record.partition(),
                record.offset()
        );


    }
}

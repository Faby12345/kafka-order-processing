package ro.scribemed.kafkaplayground.order.event;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.scribemed.kafkaplayground.order.domain.Order;
import ro.scribemed.kafkaplayground.order.domain.ProcessedEvent;
import ro.scribemed.kafkaplayground.order.repository.OrderRepository;
import ro.scribemed.kafkaplayground.order.repository.ProcessedEventRepository;

import java.time.Instant;

@Service
public class OrderEventProcessor {
    private final OrderRepository orderRepository;
    private final ProcessedEventRepository processedEventRepository;

    private static final Logger log =
            LoggerFactory.getLogger(OrderEventProcessor.class);

    public OrderEventProcessor(OrderRepository orderRepository, ProcessedEventRepository processedEventRepository) {
        this.orderRepository = orderRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public void process(OrderCreatedEvent event) {

        if(processedEventRepository.existsById(event.eventId())){
            log.info("Skipping duplicaœte event: {}", event.eventId());
            return;
        }

       Order existingOrder = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found "));

       processedEventRepository.save(
               new ProcessedEvent(
                       event.eventId(),
                       existingOrder,
                       Instant.now()
               )
       );

    }


}

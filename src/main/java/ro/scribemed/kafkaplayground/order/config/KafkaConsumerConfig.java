package ro.scribemed.kafkaplayground.order.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.backoff.FixedBackOff;
import ro.scribemed.kafkaplayground.order.event.OrderCreatedEvent;

@Configuration
public class KafkaConsumerConfig {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaConsumerConfig.class);


    /**
     * MODIFY THE DEFAULT RETRY POLICY
     * CURRENT FLOW: TRY IF FAILS ->  (WAIT A SECOND AND TRY AGAIN) * 3
     * */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate
    ) {
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) -> {

                            log.warn(
                                    "Retries exhausted; sending to DLT: " +
                                            "topic={}, partition={}, offset={}, key={}, destination={}",
                                    record.topic(),
                                    record.partition(),
                                    record.offset(),
                                    record.key(),
                                    KafkaTopicConfig.ORDERS_CREATED_DLT,
                                    exception
                            );

                            return new TopicPartition(
                                    KafkaTopicConfig.ORDERS_CREATED_DLT,
                                    record.partition()
                            );
                        }
                );

        //Makes recovery fail if publishing to the DLT fails
        recoverer.setFailIfSendResultIsError(true);

        return new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(1000, 3L)
        );
    }
}

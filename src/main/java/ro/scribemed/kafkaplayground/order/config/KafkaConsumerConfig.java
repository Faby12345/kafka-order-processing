package ro.scribemed.kafkaplayground.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {


    /**
     * MODIFY THE DEFAULT RETRY POLICY
     * CURRENT FLOW: TRY IF FAILS ->  (WAIT A SECOND AND TRY AGAIN) * 3
     * */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        return new DefaultErrorHandler(
                new FixedBackOff(1000, 3L)
        );
    }
}

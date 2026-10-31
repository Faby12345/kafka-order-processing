package ro.scribemed.kafkaplayground.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    public static final String ORDERS_CREATED = "orders.created";

    @Bean
    public NewTopic ordersCreatedTopic(){
        return TopicBuilder.name(ORDERS_CREATED)
                .partitions(2)
                .replicas(1)
                .build();

    }
}

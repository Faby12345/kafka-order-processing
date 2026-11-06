package ro.scribemed.kafkaplayground.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.scribemed.kafkaplayground.order.domain.ProcessedEvent;

import java.util.UUID;

public  interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {

}

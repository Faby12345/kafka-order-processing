package ro.scribemed.kafkaplayground.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.scribemed.kafkaplayground.order.domain.Order;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}

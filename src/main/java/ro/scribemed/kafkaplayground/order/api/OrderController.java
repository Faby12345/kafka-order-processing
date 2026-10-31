package ro.scribemed.kafkaplayground.order.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.scribemed.kafkaplayground.order.OrderService;


@RestController
@RequestMapping("/orders")
public class OrderController {
    private  final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
          @Valid @RequestBody CreateOrderRequest request
    ){
        CreateOrderResponse response = orderService.creatOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


}

package com.david.ecommerce_api.order;

import com.david.ecommerce_api.auth.AuthenticatedUser;
import com.david.ecommerce_api.order.OrderDtos.CreateOrderRequest;
import com.david.ecommerce_api.order.OrderDtos.OrderResponse;
import com.david.ecommerce_api.order.OrderDtos.StatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return OrderResponse.from(orderService.create(user, request));
    }

    @GetMapping("/orders")
    public List<OrderResponse> mine(@AuthenticationPrincipal AuthenticatedUser user) {
        return orderService.listForUser(user.id()).stream().map(OrderResponse::from).toList();
    }

    @GetMapping("/orders/{id}")
    public OrderResponse one(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return OrderResponse.from(orderService.getVisibleTo(user, id));
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return OrderResponse.from(orderService.cancelOwn(user, id));
    }

    @GetMapping("/admin/orders")
    public List<OrderResponse> all(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q
    ) {
        OrderStatus filter = status == null || status.isBlank() ? null : OrderStatus.valueOf(status.toUpperCase(Locale.ROOT));

        return orderService.listAll(filter, q).stream().map(OrderResponse::from).toList();
    }

    @GetMapping("/admin/orders/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return OrderResponse.from(orderService.get(id));
    }

    @PatchMapping("/admin/orders/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return OrderResponse.from(orderService.updateStatus(id, OrderStatus.valueOf(request.status().toUpperCase(Locale.ROOT))));
    }
}

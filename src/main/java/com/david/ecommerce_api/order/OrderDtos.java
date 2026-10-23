package com.david.ecommerce_api.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record LineRequest(
            @NotNull Long productId,
            @Size(max = 80) String color,
            @Size(max = 80) String size,
            @Min(1) @Max(99) int quantity
    ) {
    }

    public record AddressDto(
            @NotBlank @Size(max = 120) String fullName,
            @NotBlank @Size(max = 60) String phone,
            @NotBlank @Size(max = 250) String street,
            @NotBlank @Size(max = 120) String city,
            @Size(max = 120) String state,
            @Size(max = 30) String postalCode,
            @NotBlank @Size(max = 120) String country
    ) {
        public static AddressDto from(ShippingAddress address) {
            return new AddressDto(
                    address.getFullName(),
                    address.getPhone(),
                    address.getStreet(),
                    address.getCity(),
                    address.getState(),
                    address.getPostalCode(),
                    address.getCountry()
            );
        }

        public ShippingAddress toEntity() {
            return new ShippingAddress(
                    fullName.trim(),
                    phone.trim(),
                    street.trim(),
                    city.trim(),
                    state == null ? "" : state.trim(),
                    postalCode == null ? "" : postalCode.trim(),
                    country.trim()
            );
        }
    }

    public record CreateOrderRequest(
            @Email @Size(max = 180) String email,
            @NotEmpty @Valid List<LineRequest> items,
            @NotNull @Valid AddressDto address,
            @NotBlank @Pattern(regexp = "^(cash_on_delivery|bank_transfer)$", message = "must be cash_on_delivery or bank_transfer") String paymentMethod,
            @Size(max = 1000) String notes,
            @Size(max = 40) String promoCode
    ) {
    }

    public record StatusRequest(
            @NotBlank @Pattern(regexp = "^(pending|paid|shipped|delivered|cancelled)$", message = "is not an order status") String status
    ) {
    }

    public record ItemResponse(
            Long productId,
            String name,
            String image,
            BigDecimal price,
            int quantity,
            String color,
            String size
    ) {
        public static ItemResponse from(OrderItem item) {
            return new ItemResponse(
                    item.getProductId(),
                    item.getName(),
                    item.getImage() == null ? "" : item.getImage(),
                    item.getUnitPrice(),
                    item.getQuantity(),
                    item.getColor() == null ? "" : item.getColor(),
                    item.getSize() == null ? "" : item.getSize()
            );
        }
    }

    public record OrderResponse(
            Long id,
            String number,
            Long userId,
            String email,
            List<ItemResponse> items,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal shipping,
            BigDecimal total,
            String status,
            String paymentMethod,
            AddressDto address,
            String notes,
            long createdAt
    ) {
        public static OrderResponse from(Order order) {
            return new OrderResponse(
                    order.getId(),
                    order.getNumber(),
                    order.getUserId(),
                    order.getEmail(),
                    order.getItems().stream().map(ItemResponse::from).toList(),
                    order.getSubtotal(),
                    order.getDiscount(),
                    order.getShipping(),
                    order.getTotal(),
                    order.getStatus().apiValue(),
                    order.getPaymentMethod().apiValue(),
                    AddressDto.from(order.getAddress()),
                    order.getNotes(),
                    order.getCreatedAt().toEpochMilli()
            );
        }
    }
}

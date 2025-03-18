package com.bacpham.ecommerce.order;

import com.bacpham.ecommerce.customer.CustomerClient;
import com.bacpham.ecommerce.exception.BusinessException;

import com.bacpham.ecommerce.kafka.OrderConfirmation;
import com.bacpham.ecommerce.kafka.OrderProducer;
import com.bacpham.ecommerce.orderline.OrderLineRequest;
import com.bacpham.ecommerce.orderline.OrderLineService;
import com.bacpham.ecommerce.payment.PaymentClient;
import com.bacpham.ecommerce.payment.PaymentRequest;
import com.bacpham.ecommerce.product.ProductClient;
import com.bacpham.ecommerce.product.PurchaseRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final CustomerClient customerClient;
    private final PaymentClient paymentClient;
    private final ProductClient productClient;
    private final OrderLineService orderLineService;
    private final OrderProducer orderProducer;

    @Transactional
    public Integer createOrder(OrderRequest request) {
        var customer = this.customerClient.findCustomerById(request.customerId())
                .orElseThrow(() -> new BusinessException("Cannot create order:: No customer exists with the provided ID"));

        var purchasedProducts = productClient.purchaseProducts(request.products());

        BigDecimal totalAmount = purchasedProducts.stream()
                .map(product -> product.price().multiply(BigDecimal.valueOf(product.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (request.amount().compareTo(totalAmount) != 0) {
            throw new BusinessException("Order amount does not match the total price of purchased products.");
        }

        var order = mapper.toOrder(request);
        order.setTotalAmount(totalAmount);
        order = this.repository.save(order);

        for (PurchaseRequest purchaseRequest : request.products()) {
            orderLineService.saveOrderLine(
                    new OrderLineRequest(
                            null,
                            order.getId(),
                            purchaseRequest.productId(),
                            purchaseRequest.quantity()
                    )
            );
        }

        var paymentRequest = new PaymentRequest(
                totalAmount,
                request.paymentMethod(),
                order.getId(),
                order.getReference(),
                customer
        );
        paymentClient.requestOrderPayment(paymentRequest);


        orderProducer.sendOrderConfirmation(
                new OrderConfirmation(
                        request.reference(),
                        totalAmount,
                        request.paymentMethod(),
                        customer,
                        purchasedProducts
                )
        );

        return order.getId();
    }



    public List<OrderResponse> findAllOrders() {
        return repository.findAll()
                .stream()
                .map(mapper::fromOrder)
                .collect(Collectors.toList());
    }

    public OrderResponse findOrderById(Integer orderId) {
        return repository.findById(orderId)
                .map(mapper::fromOrder)
                .orElseThrow(() -> new EntityNotFoundException(String.format("No order found with the provided ID: %d", orderId)));
    }
}
package com.bacpham.ecommerce.payment;

import com.bacpham.ecommerce.customer.CustomerResponse;
import com.bacpham.ecommerce.order.PaymentMethod;

import java.math.BigDecimal;

public record PaymentRequest(
        BigDecimal amount,
        PaymentMethod paymentMethod,
        Integer orderId,
        String orderReference,
        CustomerResponse customer
) {
}
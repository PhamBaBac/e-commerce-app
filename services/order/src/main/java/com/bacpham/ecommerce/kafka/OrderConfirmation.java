package com.bacpham.ecommerce.kafka;


import com.bacpham.ecommerce.customer.CustomerResponse;
import com.bacpham.ecommerce.order.PaymentMethod;
import com.bacpham.ecommerce.product.PurchaseResponse;

import java.math.BigDecimal;
import java.util.List;

public record OrderConfirmation (
        String orderReference,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        CustomerResponse customer,
        List<PurchaseResponse> products

) {
}
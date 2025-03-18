package com.bacpham.ecommerce.orderline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderLineService {

    private final OrderLineRepository repository;
    private final OrderLineMapper mapper;

    @Transactional
    public Integer saveOrderLine(OrderLineRequest request) {
        log.info("Saving OrderLine for order ID: {}", request.orderId());
        var orderLine = mapper.toOrderLine(request);
        var savedOrderLine = repository.save(orderLine);
        log.info("OrderLine saved with ID: {}", savedOrderLine.getId());
        return savedOrderLine.getId();
    }

    public List<OrderLineResponse> findAllByOrderId(Integer orderId) {
        log.info("Finding all OrderLines for order ID: {}", orderId);
        return repository.findAllByOrderId(orderId)
                .stream()
                .map(mapper::toOrderLineResponse)
                .collect(Collectors.toList());
    }
}
package com.bacpham.ecommerce.customer;

import com.bacpham.ecommerce.exception.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    public String createCustomer(CustomerRequest request){
        var customer = this.repository.save(mapper.toCustomer(request));
        return customer.getId();
    }

    public void updateCustomer(CustomerRequest request) {
        var customer = this.repository.findById(request.id())
                .orElseThrow(() -> new CustomerNotFoundException(
                        format("Cannot update customer :: No customer found with id %s", request.id())
                ));
        mergeCustomer(customer, request);
        repository.save(customer);
    }

    private void mergeCustomer(Customer customer,CustomerRequest request) {
        if(StringUtils.isNotBlank(request.firstname())) {
            customer.setFirstname(request.firstname());
        }
        if(StringUtils.isNotBlank(request.lastname())) {
            customer.setLastname(request.lastname());
        }
        if(StringUtils.isNotBlank(request.email())) {
            customer.setEmail(request.email());
        }
        if(request.address() != null) {
            customer.setAddress(request.address());
        }

    }

    public List<CustomerResponse> getCustomers() {
        return repository.findAll().stream()
                .map(mapper::toCustomerResponse)
                .toList();
    }

    public Boolean customerExists(String customerId) {
        return repository.findById(customerId).isPresent();
    }

    public CustomerResponse findById(String customerId) {
        return repository.findById(customerId)
                .map(mapper::toCustomerResponse)
                .orElseThrow(() -> new CustomerNotFoundException(
                        format("Cannot find customer :: No customer found with id %s", customerId)
                ));

    }

    public void deleteCustomer(String customerId) {
        repository.deleteById(customerId);
    }
}

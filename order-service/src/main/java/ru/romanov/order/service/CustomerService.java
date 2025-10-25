package ru.romanov.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.romanov.order.domain.entity.CustomerEntity;
import ru.romanov.order.domain.repository.CustomerRepository;
import ru.romanov.order.exception.CustomerNotFoundException;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerEntity validateCustomer(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Клиент не найден: " + email));
    }
}

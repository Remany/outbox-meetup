package ru.romanov.order.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.romanov.order.domain.entity.CustomerEntity;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {
    Optional<CustomerEntity> findByEmail(String email);
}

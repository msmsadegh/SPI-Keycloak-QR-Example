package com.msmsadegh.user.model.repository;

import org.mop.account.user.model.User;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByNationalCode(String nationalCode);
    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByNationalCodeAndRemovedAt(String userEmail, Instant removeAt);
}
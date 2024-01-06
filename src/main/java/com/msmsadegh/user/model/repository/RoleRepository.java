package com.msmsadegh.user.model.repository;

import org.mop.account.user.RoleType;
import org.mop.account.user.model.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends CrudRepository<Role, Long> {
    Optional<Role> findByEnumName(RoleType enumName);
    List<Role> findByEnNameContaining(String enName);
    List<Role> findByFaNameContaining(String faName);
}
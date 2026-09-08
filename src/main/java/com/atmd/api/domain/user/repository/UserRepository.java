package com.atmd.api.domain.user.repository;

import com.atmd.api.domain.user.entity.User;
import com.atmd.api.domain.user.entity.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findByProviderAndProviderIdAndIsDeletedFalse(Provider provider, String providerId);

    Optional<User> findByIdAndIsDeletedFalse(Long id);

    boolean existsByEmailAndIsDeletedFalse(String email);
}

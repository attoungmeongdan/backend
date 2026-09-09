package com.atmd.backend.domain.user.repository;

import com.atmd.backend.domain.user.entity.User;
import com.atmd.backend.domain.user.entity.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findByProviderAndProviderIdAndIsDeletedFalse(Provider provider, String providerId);

    Optional<User> findByIdAndIsDeletedFalse(Long id);

    boolean existsByEmailAndIsDeletedFalse(String email);
}
